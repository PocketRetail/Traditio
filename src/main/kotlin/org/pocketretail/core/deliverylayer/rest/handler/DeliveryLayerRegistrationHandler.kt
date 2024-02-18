package org.pocketretail.core.deliverylayer.rest.handler

import org.pocketretail.core.deliverylayer.common.handler.ClientRequestsHandler
import org.pocketretail.core.deliverylayer.database.constant.ClientRequestType
import org.pocketretail.core.deliverylayer.database.constant.ParameterType
import org.pocketretail.core.deliverylayer.database.entity.Client
import org.pocketretail.core.deliverylayer.database.entity.ClientRequest
import org.pocketretail.core.deliverylayer.database.entity.ClientRequestParameter
import org.pocketretail.core.deliverylayer.database.repo.ClientRepository
import org.pocketretail.core.deliverylayer.database.repo.ClientRequestParameterRepository
import org.pocketretail.core.deliverylayer.database.repo.ClientRequestRepository
import org.pocketretail.core.deliverylayer.graphql.response.GraphQLSchemaResponse
import org.pocketretail.core.deliverylayer.rest.client.HealthCheckWebClient
import org.pocketretail.core.deliverylayer.rest.client.exception.ApplicationNotUpException
import org.pocketretail.core.deliverylayer.rest.model.DeliveryLayerCommonResponse
import org.pocketretail.core.deliverylayer.rest.model.DeliveryLayerErrorResponse
import org.pocketretail.core.deliverylayer.rest.model.DeliveryLayerResponse
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@Service
class DeliveryLayerRegistrationHandler(
    private val clientRequestsHandler: ClientRequestsHandler,
    private val clientRepository: ClientRepository,
    private val clientRequestRepository: ClientRequestRepository,
    private val clientRequestParameterRepository: ClientRequestParameterRepository
) {

    fun registerClient(clientId: String): Mono<out DeliveryLayerCommonResponse> {
        return clientRepository.findClientByClientId(clientId)
            .switchIfEmpty(initNewClient(clientId))
            .flatMap { client ->
                if (!client.active) {
                    updateIsActive(client).thenReturn(client)
                } else {
                    Mono.just(client)
                }
            }
            .flatMap { client ->
                createOrUpdateRequests(client).then(
                    Mono.just<DeliveryLayerCommonResponse>(
                        DeliveryLayerResponse(
                            "Client with id $clientId was successfully registered",
                            200,
                            Calendar.getInstance().time
                        )
                    )
                )
            }
            .onErrorResume { e ->
                Mono.just<DeliveryLayerCommonResponse>(
                    DeliveryLayerErrorResponse(
                        "An error occurred while registering client with id $clientId",
                        503,
                        Calendar.getInstance().time,
                        e.stackTraceToString()
                    )
                )
            }
    }

    private fun updateIsActive(client: Client): Mono<Void> {
        client.active = true
        return clientRepository.save(client).then()
    }

    @Throws(ApplicationNotUpException::class)
    fun initNewClient(clientId: String): Mono<Client> {
        val client = Client(
            clientId = clientId,
            clientURI = "https://$clientId.pocketretail.de",
            active = true
        )
        return HealthCheckWebClient.checkHealth(client.clientURI)
            .then(clientRepository.save(client))
    }

    private fun createOrUpdateRequests(client: Client): Mono<Void> {
        return clientRequestsHandler.getGraphQLSchema(client.clientId)
            .flatMap { graphQLSchemaResponse ->
                clientRequestRepository.findAllByClientId(client.clientId)
                    .collectList()
                    .flatMap { clientRequests ->
                        if (clientRequests.isEmpty()) {
                            createNewClientRequests(client, graphQLSchemaResponse)
                        } else {
                            updateOldClientRequestsAndCreateNew(
                                client,
                                graphQLSchemaResponse,
                                clientRequests
                            )
                        }
                    }
            }
    }

    fun updateOldClientRequestsAndCreateNew(
        client: Client,
        graphQLSchemaResponse: GraphQLSchemaResponse,
        clientRequests: List<ClientRequest>
    ): Mono<Void> {
        val copyOfGraphQLSchemaResponse = graphQLSchemaResponse.copy()

        val allQueries = graphQLSchemaResponse.data.schema.types
            .filter { it.name == "Query" }
            .flatMap { it.fields }
            .filterNot { it.name == "_service" }.toMutableList()

        val updateExistingRequests = Flux.fromIterable(clientRequests).flatMap { clientRequest ->
            clientRequestParameterRepository.findAllByClientRequestIdAndParentClientRequestParameterIdIsNull(
                clientRequest.clientRequestId!!
            ).collectList().flatMap { levelZeroParameters ->
                val queryOfRequest =
                    allQueries.firstOrNull { query -> levelZeroParameters.any { it.clientRequestParameterName == query.name } }
                if (queryOfRequest == null) {
                    clientRequestParameterRepository.findAllByClientRequestId(
                        clientRequest.clientRequestId
                    ).collectList().flatMap { parameters ->
                        Flux.fromIterable(parameters)
                            .flatMap { parameter -> deleteParameterAndChildren(parameter) }
                            .then(clientRequestRepository.delete(clientRequest))
                            .then(Mono.empty<List<String>>())
                    }
                } else {
                    deleteAllNotNeededParameters(
                        copyOfGraphQLSchemaResponse,
                        null,
                        levelZeroParameters
                    )
                        .then(
                            clientRequestParameterRepository.findAllByClientRequestIdAndParentClientRequestParameterIdIsNull(
                                clientRequest.clientRequestId
                            ).collectList().flatMap {
                                addNewParametersToClientRequest(
                                    clientRequest,
                                    copyOfGraphQLSchemaResponse,
                                    it
                                )
                            }.then()
                        ).then(Mono.just(listOf(queryOfRequest.name)))
                }
            }
        }.collectList().map { it.flatten() }

        return updateExistingRequests.flatMap { queryNames ->
            Flux.fromIterable(allQueries).filter { query -> !queryNames.contains(query.name) }
                .flatMap { filteredQuery ->
                    val clientRequest = ClientRequest(
                        clientId = client.clientId,
                        clientRequestType = ClientRequestType.GRAPHQL,
                        clientRequestURI = client.clientURI,
                        clientRequestName = null
                    )
                    clientRequestRepository.save(clientRequest).flatMap {
                        createClientRequestParameter(
                            filteredQuery.name!!,
                            filteredQuery.type!!,
                            it,
                            graphQLSchemaResponse,
                            ParameterType.OUTPUT
                        )
                    }
                }.then()
        }
    }

    private fun addNewParametersToClientRequest(
        clientRequest: ClientRequest,
        graphQLSchemaResponse: GraphQLSchemaResponse,
        parameters: List<ClientRequestParameter>
    ): Mono<Void> {
        val query = graphQLSchemaResponse.data.schema.types
            .filter { dataSchemaType -> dataSchemaType.name == "Query" }
            .flatMap { it.fields }
            .firstOrNull { query -> query.name == clientRequest.clientRequestName }
            ?: throw IllegalArgumentException("Query not found")

        val createOutputParamsMono = Flux.fromIterable(parameters)
            .filter { it.clientRequestParameterType == ParameterType.OUTPUT }
            .collectList()
            .flatMap { outputParameters ->
                createIfNeededNewParameter(
                    outputParameters,
                    clientRequest,
                    graphQLSchemaResponse,
                    query
                )
            }

        val createInputParamsMono = Flux.fromIterable(query.args!!)
            .flatMap { arg ->
                checkIfParameterExistsAndGet(parameters, arg.name!!, arg.type!!)
                    .switchIfEmpty(
                        createClientRequestParameter(
                            arg.name!!,
                            arg.type!!,
                            clientRequest,
                            graphQLSchemaResponse,
                            ParameterType.INPUT
                        ).then(Mono.empty())
                    )
                    .flatMap { searchedParameter ->
                        if (arg.type?.kind == "LIST" && (arg.type?.ofType?.kind == "OBJECT" || arg.type?.ofType?.kind == "INPUT_OBJECT") || arg.type?.kind == "OBJECT") {
                            createIfNeededNewChildParameter(
                                clientRequest,
                                graphQLSchemaResponse,
                                arg.type!!,
                                searchedParameter,
                                ParameterType.INPUT
                            )
                        } else {
                            Mono.empty()
                        }
                    }
            }.then()

        return Mono.`when`(createOutputParamsMono, createInputParamsMono)
    }

    private fun createIfNeededNewParameter(
        parameters: List<ClientRequestParameter>,
        clientRequest: ClientRequest,
        graphQLSchemaResponse: GraphQLSchemaResponse,
        field: GraphQLSchemaResponse.SchemaResponseData.SchemaResponseDataSchema.DataSchemaType.Field,
        parameterType: ParameterType = ParameterType.OUTPUT,
    ): Mono<Void> {
        return checkIfParameterExistsAndGet(parameters, field.name!!, field.type!!)
            .switchIfEmpty(
                createClientRequestParameter(
                    field.name!!,
                    field.type!!,
                    clientRequest,
                    graphQLSchemaResponse,
                    parameterType
                ).then(Mono.empty())
            )
            .flatMap { parameter ->
                if (parameter.clientRequestParameterDataType == "LIST" && (parameter.clientRequestParameterOfTypeDataType == "OBJECT" || parameter.clientRequestParameterDataType == "INPUT_OBJECT") || parameter.clientRequestParameterDataType == "OBJECT") {
                    createIfNeededNewChildParameter(
                        clientRequest,
                        graphQLSchemaResponse,
                        field.type!!,
                        parameter,
                        parameterType
                    )
                } else {
                    Mono.empty()
                }
            }.then()
    }

    private fun createIfNeededNewChildParameter(
        clientRequest: ClientRequest,
        graphQLSchemaResponse: GraphQLSchemaResponse,
        type: GraphQLSchemaResponse.SchemaResponseData.SchemaResponseDataSchema.DataSchemaType.Field.Type,
        parameter: ClientRequestParameter,
        parameterType: ParameterType
    ): Mono<Void> {
        // Erzeuge einen Flux aus den Feldern, die verarbeitet werden sollen.
        return Flux.fromIterable(getFields(graphQLSchemaResponse, type, parameterType))
            .flatMap { field ->
                // Finde alle untergeordneten Parameter für das aktuelle Feld.
                clientRequestParameterRepository.findAllByParentClientRequestParameterId(parameter.clientRequestParameterId!!)
                    .collectList()
                    .flatMap { childParameters ->
                        // Erzeuge einen neuen Parameter, falls nötig.
                        createIfNeededNewParameter(
                            childParameters,
                            clientRequest,
                            graphQLSchemaResponse,
                            field,
                            parameterType
                        )
                    }
            }
            .then() // Führt alle obigen Operationen aus und gibt Mono<Void> zurück, um den Abschluss zu signalisieren.
    }

    fun getFields(
        graphQLSchemaResponse: GraphQLSchemaResponse,
        type: GraphQLSchemaResponse.SchemaResponseData.SchemaResponseDataSchema.DataSchemaType.Field.Type,
        parameterType: ParameterType
    ): List<GraphQLSchemaResponse.SchemaResponseData.SchemaResponseDataSchema.DataSchemaType.Field> {
        val types = graphQLSchemaResponse.data.schema.types
        val searchedType = when (type.kind) {
            "LIST" -> {
                types.stream().filter { dataSchemaType -> dataSchemaType.name == type.ofType?.name }
                    .findFirst().orElseThrow()
            }
            "OBJECT", "INPUT_OBJECT" -> {
                types.stream().filter { dataSchemaType -> dataSchemaType.name == type.name }
                    .findFirst().orElseThrow()
            }
            else -> {
                null
            }
        } ?: return emptyList()

        return if (parameterType == ParameterType.INPUT) {
            searchedType.inputFields
        } else {
            searchedType.fields
        }
    }

    private fun checkIfParameterExistsAndGet(
        parameters: List<ClientRequestParameter>,
        name: String,
        type: GraphQLSchemaResponse.SchemaResponseData.SchemaResponseDataSchema.DataSchemaType.Field.Type
    ): Mono<ClientRequestParameter> {
        return Flux.fromIterable(parameters).filter { parameter ->
            checkIfThisIsTheSearchedParameter(parameter, name, type)
        }.next()
    }

    private fun checkIfThisIsTheSearchedParameter(
        parameter: ClientRequestParameter,
        name: String?,
        type: GraphQLSchemaResponse.SchemaResponseData.SchemaResponseDataSchema.DataSchemaType.Field.Type,
    ) = parameter.clientRequestParameterName == name &&
        parameter.clientRequestParameterDataTypeName == type.name &&
        parameter.clientRequestParameterDataType == type.kind &&
        parameter.clientRequestParameterOfTypeDataTypeName == type.ofType?.name &&
        parameter.clientRequestParameterOfTypeDataType == type.ofType?.kind

    private fun deleteAllNotNeededParameters(
        graphQLSchemaResponse: GraphQLSchemaResponse,
        parentParameter: ClientRequestParameter?,
        parameters: List<ClientRequestParameter>
    ): Mono<Void> {
        return Flux.fromIterable(parameters)
            .flatMap { parameter ->
                deleteParameterIfNotNeeded(
                    parentParameter,
                    parameter,
                    graphQLSchemaResponse
                )
            }.then()
    }


    private fun deleteParameterIfNotNeeded(
        parentParameter: ClientRequestParameter?,
        parameter: ClientRequestParameter,
        graphQLSchemaResponse: GraphQLSchemaResponse,
    ): Mono<Void> {
        val allQueries = graphQLSchemaResponse.data.schema.types
            .filter { dataSchemaType -> dataSchemaType.name == "Query" }
            .flatMap { it.fields }
            .filterNot { it.name == "_service" }
        val allTypes = graphQLSchemaResponse.data.schema.types

        return if (parentParameter != null) {
            handleDeleteIfNeededUseCaseParentIsNotNull(
                parentParameter,
                allTypes,
                parameter,
                graphQLSchemaResponse
            )
        } else if (parameter.clientRequestParameterType == ParameterType.INPUT) {
            handleDeleteIfNeededUseCaseParentIsNullAndParameterTypeIsInput(allQueries, parameter)
        } else {
            handleDeleteIfNeededUseCaseParentIsNullAndParameterTypeIsOutput(
                allQueries,
                parameter,
                graphQLSchemaResponse
            )
        }
    }

    private fun handleDeleteIfNeededUseCaseParentIsNullAndParameterTypeIsOutput(
        allQueries: List<GraphQLSchemaResponse.SchemaResponseData.SchemaResponseDataSchema.DataSchemaType.Field>,
        parameter: ClientRequestParameter,
        graphQLSchemaResponse: GraphQLSchemaResponse
    ): Mono<Void> {
        // Direkte Zuweisung ohne Stream, da wir eine Liste haben
        val query = allQueries.firstOrNull { it.name == parameter.clientRequestParameterName }
            ?: return Mono.error(IllegalStateException("Query not found"))

        return if (parameter.clientRequestParameterDataType == "LIST") {
            if (query.type?.ofType?.kind == parameter.clientRequestParameterOfTypeDataType && query.type?.ofType?.name == parameter.clientRequestParameterOfTypeDataTypeName) {
                clientRequestParameterRepository.findAllByParentClientRequestParameterId(parameter.clientRequestParameterId!!)
                    .collectList()
                    .flatMap { children ->
                        deleteAllNotNeededParameters(graphQLSchemaResponse, parameter, children)
                    }
            } else {
                deleteParameterAndChildren(parameter)
            }
        } else if (parameter.clientRequestParameterDataType == "OBJECT") {
            if (query.type?.kind == parameter.clientRequestParameterDataType && query.type?.name == parameter.clientRequestParameterDataTypeName) {
                clientRequestParameterRepository.findAllByParentClientRequestParameterId(parameter.clientRequestParameterId!!)
                    .collectList()
                    .flatMap { children ->
                        deleteAllNotNeededParameters(graphQLSchemaResponse, parameter, children)
                    }
            } else {
                deleteParameterAndChildren(parameter)
            }
        } else {
            Mono.empty()
        }
    }


    private fun handleDeleteIfNeededUseCaseParentIsNullAndParameterTypeIsInput(
        allQueries: List<GraphQLSchemaResponse.SchemaResponseData.SchemaResponseDataSchema.DataSchemaType.Field>,
        parameter: ClientRequestParameter
    ): Mono<Void> {
        return clientRequestRepository.findById(parameter.clientRequestId)
            .flatMap { clientRequest ->
                val query = allQueries.firstOrNull { it.name == clientRequest.clientRequestName }
                if (query == null) {
                    Mono.error(IllegalStateException("Query not found"))
                } else {
                    val argExists = query.args?.any { arg ->
                        arg.name == parameter.clientRequestParameterName &&
                            arg.type.kind == parameter.clientRequestParameterDataType &&
                            arg.type.name == parameter.clientRequestParameterDataTypeName &&
                            arg.type.ofType?.kind == parameter.clientRequestParameterOfTypeDataType &&
                            arg.type.ofType?.name == parameter.clientRequestParameterOfTypeDataTypeName
                    }
                    if (argExists == false) {
                        deleteParameterAndChildren(parameter)
                    } else {
                        Mono.empty()
                    }
                }
            }
    }

    private fun handleDeleteIfNeededUseCaseParentIsNotNull(
        parentParameter: ClientRequestParameter,
        allTypes: List<GraphQLSchemaResponse.SchemaResponseData.SchemaResponseDataSchema.DataSchemaType>,
        parameter: ClientRequestParameter,
        graphQLSchemaResponse: GraphQLSchemaResponse
    ): Mono<Void> {
        // Bestimme den Parent-Typ basierend auf der Bedingung.
        val parentTypeName = if (parentParameter.clientRequestParameterDataType == "LIST") {
            parentParameter.clientRequestParameterOfTypeDataTypeName
        } else {
            parentParameter.clientRequestParameterDataTypeName
        }

        return Mono.justOrEmpty(allTypes.find { it.name == parentTypeName })
            .flatMap { parentType ->
                // Entscheide basierend auf dem Parameter-Typ, welche Felder zu verwenden sind.
                val fields = when (parameter.clientRequestParameterType) {
                    ParameterType.INPUT -> parentType.inputFields
                    else -> parentType.fields
                }

                // Suche nach dem gesuchten Parameter in den Feldern.
                val searchedParameter = fields.firstOrNull { field ->
                    checkIfThisIsTheSearchedParameter(parameter, field.name, field.type!!)
                }

                if (searchedParameter == null) {
                    // Wenn der gesuchte Parameter nicht gefunden wurde, lösche den Parameter und seine Kinder.
                    deleteParameterAndChildren(parameter)
                        .then(clientRequestParameterRepository.delete(parameter))
                } else if (parameter.clientRequestParameterDataType == "LIST" &&
                    (parameter.clientRequestParameterOfTypeDataType?.contains("OBJECT") == true ||
                        parameter.clientRequestParameterDataType.contains("OBJECT"))
                ) {
                    // Wenn der Parameter existiert und bestimmte Bedingungen erfüllt, bearbeite ihn weiter.
                    clientRequestParameterRepository.findAllByParentClientRequestParameterId(
                        parameter.clientRequestParameterId!!
                    )
                        .collectList()
                        .flatMap { children ->
                            deleteAllNotNeededParameters(graphQLSchemaResponse, parameter, children)
                        }
                } else {
                    Mono.empty()
                }
            }
    }

    private fun deleteParameterAndChildren(parameter: ClientRequestParameter): Mono<Void> {
        return clientRequestParameterRepository.findAllByParentClientRequestParameterId(parameter.clientRequestParameterId!!)
            .flatMap { childParameter ->
                deleteParameterAndChildren(childParameter)
            }
            .then(clientRequestParameterRepository.deleteById(parameter.clientRequestParameterId))
    }


    private fun createNewClientRequests(
        client: Client, graphQLSchemaResponse: GraphQLSchemaResponse
    ): Mono<Void> {
        return Flux.fromIterable(graphQLSchemaResponse.data.schema.types.first { type -> type.name == "Query" }.fields)
            .filter { field -> field.name != "_service" }
            .flatMap { field ->
                // Erstellen eines neuen ClientRequest
                val clientRequest = ClientRequest(
                    clientRequestType = ClientRequestType.GRAPHQL,
                    clientRequestURI = client.clientURI,
                    clientId = client.clientId,
                    clientRequestName = field.name
                )
                // Speichern des ClientRequest und dann Verarbeiten der Argumente
                clientRequestRepository.save(clientRequest).flatMap {
                    // Verarbeitung der Argumente für den ClientRequest
                    val parametersFlux = Flux.fromIterable(field.args!!)
                        .flatMap { arg ->
                            createClientRequestParameter(
                                arg.name,
                                arg.type,
                                it,
                                graphQLSchemaResponse,
                                ParameterType.INPUT
                            )
                        }
                    // Kombinieren der ParameterErstellung mit einem abschließenden Schritt
                    parametersFlux.then(
                        createClientRequestParameter(
                            field.name,
                            field.type,
                            it,
                            graphQLSchemaResponse,
                            ParameterType.OUTPUT
                        )
                    )
                }.then()
            }.then()
    }

    private fun createChildClientRequestParameter(
        type: GraphQLSchemaResponse.SchemaResponseData.SchemaResponseDataSchema.DataSchemaType,
        graphQLSchemaResponse: GraphQLSchemaResponse,
        parameterType: ParameterType,
        clientRequestParameter: ClientRequestParameter,
        clientRequest: ClientRequest
    ): Mono<Void> {
        val fields = if (parameterType == ParameterType.INPUT) {
            type.inputFields
        } else {
            type.fields
        }
        return Flux.fromIterable(fields).filter { field -> field.name != "_service" }
            .flatMap { field ->
            createClientRequestParameter(
                field.name!!,
                field.type!!,
                clientRequest,
                graphQLSchemaResponse,
                parameterType,
                clientRequestParameter
            ).then()
            }.then()
    }

    private fun createClientRequestParameter(
        fieldName: String,
        type: GraphQLSchemaResponse.SchemaResponseData.SchemaResponseDataSchema.DataSchemaType.Field.Type,
        clientRequest: ClientRequest,
        graphQLSchemaResponse: GraphQLSchemaResponse,
        parameterType: ParameterType,
        parentClientRequestParameter: ClientRequestParameter? = null
    ): Mono<Void> {
        val clientRequestParameter = ClientRequestParameter(
            clientRequestId = clientRequest.clientRequestId
                ?: throw IllegalArgumentException("clientRequestId is null"),
            clientRequestParameterType = parameterType,
            clientRequestParameterName = fieldName,
            clientRequestParameterDataType = type.kind,
            clientRequestParameterDataTypeName = type.name
        ).also { param ->
            parentClientRequestParameter?.let {
                param.parentClientRequestParameterId = it.clientRequestParameterId
                    ?: throw IllegalArgumentException("parentClientRequestParameterId is null")
            }
        }
        return clientRequestParameterRepository.save(clientRequestParameter)
            .flatMap { savedClientRequestParameter ->
                when (type.kind) {
                    "LIST" -> {
                        savedClientRequestParameter.clientRequestParameterOfTypeDataType =
                            type.ofType?.kind
                        savedClientRequestParameter.clientRequestParameterOfTypeDataTypeName =
                            type.ofType?.name
                        clientRequestParameterRepository.save(savedClientRequestParameter)
                            .flatMap {
                                createIfNeededChildClientRequestParameter(
                                    type.ofType?.name!!,
                                    graphQLSchemaResponse,
                                    parameterType,
                                    savedClientRequestParameter,
                                    clientRequest
                                )
                            }
                    }
                    "OBJECT", "INPUT_OBJECT" -> {
                        clientRequestParameterRepository.save(savedClientRequestParameter)
                            .flatMap {
                                createIfNeededChildClientRequestParameter(
                                    type.name!!,
                                    graphQLSchemaResponse,
                                    parameterType,
                                    savedClientRequestParameter,
                                    clientRequest
                                )
                            }
                    }
                    else -> clientRequestParameterRepository.save(savedClientRequestParameter)
                        .flatMap { Mono.empty() }
                }
            }
    }

    private fun createIfNeededChildClientRequestParameter(
        fieldTypeName: String,
        graphQLSchemaResponse: GraphQLSchemaResponse,
        parameterType: ParameterType,
        clientRequestParameter: ClientRequestParameter,
        clientRequest: ClientRequest
    ): Mono<Void> {
        return Flux.fromIterable(graphQLSchemaResponse.data.schema.types)
            .filter { type -> type.name == fieldTypeName && (type.fields != null && type.fields.isNotEmpty() || type.inputFields.isNotEmpty()) }
            .flatMap { type ->
                createChildClientRequestParameter(
                    type,
                    graphQLSchemaResponse,
                    parameterType,
                    clientRequestParameter,
                    clientRequest
                ).then()
            }
            .then() // Konvertiert das Ergebnis der gesamten Flux-Pipeline in Mono<Void>
    }
}