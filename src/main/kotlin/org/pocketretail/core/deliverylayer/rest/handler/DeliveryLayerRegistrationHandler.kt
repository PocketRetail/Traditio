package org.pocketretail.core.deliverylayer.rest.handler

import com.netflix.graphql.dgs.client.GraphQLResponse
import org.pocketretail.core.deliverylayer.database.constant.ClientRequestType
import org.pocketretail.core.deliverylayer.database.constant.ParameterType
import org.pocketretail.core.deliverylayer.database.entity.Client
import org.pocketretail.core.deliverylayer.database.entity.ClientRequest
import org.pocketretail.core.deliverylayer.database.entity.ClientRequestParameter
import org.pocketretail.core.deliverylayer.database.repo.ClientRepository
import org.pocketretail.core.deliverylayer.database.repo.ClientRequestParameterRepository
import org.pocketretail.core.deliverylayer.database.repo.ClientRequestRepository
import org.pocketretail.core.deliverylayer.graphql.client.GraphQLWebFluxClient
import org.pocketretail.core.deliverylayer.graphql.response.GraphQLSchemaResponse
import org.pocketretail.core.deliverylayer.graphql.response.GraphQLSchemaResponse.SchemaResponseData.SchemaResponseDataSchema.DataSchemaType.Field
import org.pocketretail.core.deliverylayer.graphql.response.GraphQLSchemaResponse.SchemaResponseData.SchemaResponseDataSchema.DataSchemaType.Field.Type
import org.pocketretail.core.deliverylayer.rest.client.HealthCheckWebClient
import org.pocketretail.core.deliverylayer.rest.client.exception.ApplicationNotUpException
import org.pocketretail.core.deliverylayer.rest.model.DeliveryLayerCommonResponse
import org.pocketretail.core.deliverylayer.rest.model.DeliveryLayerErrorResponse
import org.pocketretail.core.deliverylayer.rest.model.DeliveryLayerResponse
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.stream.Collectors

@Service
class DeliveryLayerRegistrationHandler(
    private val clientRepository: ClientRepository,
    private val clientRequestRepository: ClientRequestRepository,
    private val clientRequestParameterRepository: ClientRequestParameterRepository
) {

    fun registerClient(clientId: String): DeliveryLayerCommonResponse {

        val client = try {
            clientRepository.findClientByClientId(clientId) ?: initNewClient(clientId)
        } catch (e: ApplicationNotUpException) {
            return DeliveryLayerErrorResponse(
                "Client with id $clientId is not up",
                503,
                Calendar.getInstance().time,
                e.stackTraceToString()
            )
        }
        if (!client.isActive) {
            updateIsActive(client)
        }

        createOrUpdateRequests(client)
        return DeliveryLayerResponse(
            "Client with id $clientId was successfully registered", 200, Calendar.getInstance().time
        )
    }

    private fun updateIsActive(client: Client) {
        client.isActive = true
        clientRepository.save(client)
    }

    @Throws(ApplicationNotUpException::class)
    private fun initNewClient(clientId: String): Client {
        val client = Client()
        client.clientId = clientId
        client.clientURI = "https://$clientId.pocketretail.de"
        HealthCheckWebClient.checkHealth(client.clientURI)
        client.isActive = true
        clientRepository.save(client)
        return client
    }

    private fun createOrUpdateRequests(client: Client) {
        val graphQLClient = GraphQLWebFluxClient.createGraphQLClient(client.clientId)
        val monoGraphQLResponse: Mono<GraphQLResponse> =
            graphQLClient.reactiveExecuteQuery(readGetSchemasGraphQLFile())
        val graphQLSchemaResponse = monoGraphQLResponse.map { response: GraphQLResponse ->
            GraphQLSchemaResponse(GraphQLSchemaResponse.fromGraphQLResponse(response))
        }.block() ?: throw RuntimeException("GraphQL response is null")

        val clientRequests = clientRequestRepository.findAllByClientId(client)
        if (clientRequests.isEmpty()) {
            createNewClientRequests(client, graphQLSchemaResponse)
            return
        } else {
            updateOldClientRequestsAndCreateNew(client, graphQLSchemaResponse, clientRequests)
        }
    }

    private fun updateOldClientRequestsAndCreateNew(
        client: Client,
        graphQLSchemaResponse: GraphQLSchemaResponse,
        clientRequests: List<ClientRequest>
    ) {
        val copyOfGraphQLSchemaResponse = graphQLSchemaResponse.copy()
        val allQueries = graphQLSchemaResponse.data.__schema.types.stream()
            .filter { dataSchemaType -> dataSchemaType.name == "Query" }
            .collect(Collectors.toList()).stream().findAny().get().fields.stream().filter { field ->
                !field.name.equals("_service")
            }.collect(Collectors.toList())
        for (clientRequest in clientRequests) {
            val levelZeroParameters =
                clientRequestParameterRepository.findAllByClientRequestIdAndParentClientRequestParameterIdIsNull(
                    clientRequest
                )

            val queryOfRequest = allQueries.stream().filter { query ->
                query.name.equals(
                    levelZeroParameters.stream().findAny().get().clientRequestParameterName
                )
            }.findFirst().orElse(null)
            if (queryOfRequest == null) {
                for (parameter in clientRequestParameterRepository.findAllByClientRequestId(
                    clientRequest
                )) {
                    deleteParameterAndChildren(parameter)
                }
                clientRequestRepository.delete(clientRequest)
                continue
            }
            deleteAllNotNeededParameters(
                copyOfGraphQLSchemaResponse, null, levelZeroParameters
            )
            addNewParametersToClientRequest(
                clientRequest,
                copyOfGraphQLSchemaResponse,
                clientRequestParameterRepository.findAllByClientRequestIdAndParentClientRequestParameterIdIsNull(
                    clientRequest
                )
            )
            allQueries.remove(queryOfRequest)
        }

        for (allQuery in allQueries) {
            val clientRequest = ClientRequest()
            clientRequest.clientId = client
            clientRequest.clientRequestType = ClientRequestType.GRAPHQL
            clientRequest.clientRequestURI = client.clientURI
            clientRequestRepository.save(clientRequest)
            createClientRequestParameter(
                allQuery.name,
                allQuery.type,
                clientRequest,
                graphQLSchemaResponse,
                ParameterType.OUTPUT
            )
        }
    }

    private fun addNewParametersToClientRequest(
        clientRequest: ClientRequest,
        graphQLSchemaResponse: GraphQLSchemaResponse,
        parameters: List<ClientRequestParameter>
    ) {
        val query = graphQLSchemaResponse.data.__schema.types.stream()
            .filter { dataSchemaType -> dataSchemaType.name == "Query" }
            .collect(Collectors.toList()).stream().findAny().get().fields.stream()
            .filter { query -> query.name == clientRequest.clientRequestName }
            .findFirst().orElseThrow()

        createIfNeededNewParameter(
            parameters.stream().filter { it.clientRequestParameterType == ParameterType.OUTPUT }
                .collect(
                    Collectors.toList()
                ),
            clientRequest,
            graphQLSchemaResponse,
            query
        )


        for (arg in query.args) {
            val searchedParameter = checkIfParameterExistsAndGet(parameters, arg.name, arg.type)
            if (searchedParameter == null) {
                createClientRequestParameter(
                    arg.name,
                    arg.type,
                    clientRequest,
                    graphQLSchemaResponse,
                    ParameterType.INPUT
                )
            } else if (arg.type.kind == "LIST" && (arg.type.ofType?.kind == "OBJECT" || arg.type.ofType.kind == "INPUT_OBJECT") || arg.type.kind == "OBJECT") {
                createIfNeededNewChildParameter(
                    clientRequest,
                    graphQLSchemaResponse,
                    arg.type,
                    searchedParameter,
                    ParameterType.INPUT
                )
            }
        }
    }

    private fun createIfNeededNewParameter(
        parameters: List<ClientRequestParameter>,
        clientRequest: ClientRequest,
        graphQLSchemaResponse: GraphQLSchemaResponse,
        field: Field,
        parameterType: ParameterType = ParameterType.OUTPUT,
    ) {
        val parameter = checkIfParameterExistsAndGet(parameters, field.name, field.type)
        if (parameter == null) {
            createClientRequestParameter(
                field.name,
                field.type,
                clientRequest,
                graphQLSchemaResponse,
                parameterType
            )
            return
        }
        if (parameter.clientRequestParameterDataType == "LIST" && (parameter.clientRequestParameterOfTypeDataType == "OBJECT" || parameter.clientRequestParameterDataType == "INPUT_OBJECT") || parameter.clientRequestParameterDataType == "OBJECT") {
            createIfNeededNewChildParameter(
                clientRequest,
                graphQLSchemaResponse,
                field.type,
                parameter,
                parameterType
            )
        }
    }

    private fun createIfNeededNewChildParameter(
        clientRequest: ClientRequest,
        graphQLSchemaResponse: GraphQLSchemaResponse,
        type: Type,
        parameter: ClientRequestParameter,
        parameterType: ParameterType
    ) {

        for (field in getFields(graphQLSchemaResponse, type, parameterType)) {
            createIfNeededNewParameter(
                clientRequestParameterRepository.findAllByParentClientRequestParameterId(parameter),
                clientRequest,
                graphQLSchemaResponse,
                field,
                parameterType
            )
        }
    }

    fun getFields(
        graphQLSchemaResponse: GraphQLSchemaResponse,
        type: Type,
        parameterType: ParameterType
    ): List<Field> {
        val types = graphQLSchemaResponse.data.__schema.types
        val searchedType = when (type.kind) {
            "LIST" -> {
                types.stream().filter { dataSchemaType -> dataSchemaType.name == type.ofType.name }
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
        type: Type
    ): ClientRequestParameter? {
        for (parameter in parameters) {
            if (checkIfThisIsTheSearchedParameter(parameter, name, type)) {
                return parameter
            }
        }
        return null
    }

    private fun checkIfThisIsTheSearchedParameter(
        parameter: ClientRequestParameter,
        name: String?,
        type: Type,
    ) = parameter.clientRequestParameterName == name &&
        parameter.clientRequestParameterDataTypeName == type.name &&
        parameter.clientRequestParameterDataType == type.kind &&
        parameter.clientRequestParameterOfTypeDataTypeName == type.ofType?.name &&
        parameter.clientRequestParameterOfTypeDataType == type.ofType?.kind

    private fun deleteAllNotNeededParameters(
        graphQLSchemaResponse: GraphQLSchemaResponse,
        parentParameter: ClientRequestParameter?,
        parameters: List<ClientRequestParameter>,
    ) {
        for (parameter in parameters) {
            deleteParameterIfNotNeeded(
                parentParameter,
                parameter,
                graphQLSchemaResponse,
            )
        }
    }

    /*
        * hole Alle Requests aus GraphQLSchemaResponse
        * wenn es ein parent gibt, dann prüfe auf den namen des parent parameters in dem type welches vom Parent ausgeht
        * wenn parent wiederum null ist, dann holen die aus dem Query Type den Request mit dem Namen des Parameters (weil im Parameternamen wird der request gespeichert beim L0 Parameter)
        * wenn der pranent wiederum null ist und es ein INPUT ist, dann muss man dennoch in den types suchen, da sich das handling zwischen input und output hier unterscheidet
        * wenn der parameter nicht gefunden wurde, dann lösche ihn und alle children
        * wenn er gefunden wird und es ein Object oder eine Liste mit einem Object ist (also nicht List<String> sondern List<Object1>) dann prüfe ob es children gibt und wenn ja dann prüfe ob diese noch benötigt werden
    */
    private fun deleteParameterIfNotNeeded(
        parentParameter: ClientRequestParameter?,
        parameter: ClientRequestParameter,
        graphQLSchemaResponse: GraphQLSchemaResponse,
    ) {
        val allQueries = graphQLSchemaResponse.data.__schema.types.stream()
            .filter { dataSchemaType -> dataSchemaType.name == "Query" }
            .collect(Collectors.toList()).stream().findAny().get().fields
        val allTypes = graphQLSchemaResponse.data.__schema.types
        if (parentParameter != null) {
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
        allQueries: MutableList<Field>,
        parameter: ClientRequestParameter,
        graphQLSchemaResponse: GraphQLSchemaResponse
    ) {
        val query =
            allQueries.stream().filter { it.name == parameter.clientRequestParameterName }
                .findFirst().get()
        if (parameter.clientRequestParameterDataType == "LIST") {
            if (query.type.ofType.kind == parameter.clientRequestParameterOfTypeDataType && query.type.ofType.name == parameter.clientRequestParameterOfTypeDataTypeName) {
                deleteAllNotNeededParameters(
                    graphQLSchemaResponse,
                    parameter,
                    clientRequestParameterRepository.findAllByParentClientRequestParameterId(
                        parameter
                    )
                )
            } else {
                deleteParameterAndChildren(parameter)
            }
        } else if (parameter.clientRequestParameterDataType.equals("OBJECT")) {
            if (query.type.kind == parameter.clientRequestParameterDataType && query.type.name == parameter.clientRequestParameterDataTypeName) {
                deleteAllNotNeededParameters(
                    graphQLSchemaResponse,
                    parameter,
                    clientRequestParameterRepository.findAllByParentClientRequestParameterId(
                        parameter
                    )
                )
            } else {
                deleteParameterAndChildren(parameter)
            }
        }
    }

    private fun handleDeleteIfNeededUseCaseParentIsNullAndParameterTypeIsInput(
        allQueries: MutableList<Field>,
        parameter: ClientRequestParameter
    ) {
        val query =
            allQueries.stream()
                .filter {
                    it.name == clientRequestRepository.findById(parameter.clientRequestId.clientRequestId)
                        .get().clientRequestName
                }
                .findFirst().get()

        query.args.stream().filter {
            it.name == parameter.clientRequestParameterName && it.type.kind == parameter.clientRequestParameterDataType &&
                it.type.name == parameter.clientRequestParameterDataTypeName && it.type.ofType?.kind == parameter.clientRequestParameterOfTypeDataType &&
                it.type.ofType?.name == parameter.clientRequestParameterOfTypeDataTypeName
        }.findFirst().orElse(null) ?: run {
            deleteParameterAndChildren(parameter)
        }
    }

    private fun handleDeleteIfNeededUseCaseParentIsNotNull(
        parentParameter: ClientRequestParameter,
        allTypes: MutableList<GraphQLSchemaResponse.SchemaResponseData.SchemaResponseDataSchema.DataSchemaType>,
        parameter: ClientRequestParameter,
        graphQLSchemaResponse: GraphQLSchemaResponse
    ) {
        val parentType = if (parentParameter.clientRequestParameterDataType == "LIST") {
            allTypes.stream()
                .filter { dataSchemaType -> dataSchemaType.name == parentParameter.clientRequestParameterOfTypeDataTypeName }
                .collect(Collectors.toList()).stream().findAny().get()
        } else {
            allTypes.stream()
                .filter { dataSchemaType -> dataSchemaType.name == parentParameter.clientRequestParameterDataTypeName }
                .collect(Collectors.toList()).stream().findAny().get()
        }
        val fields = if (parameter.clientRequestParameterType == ParameterType.INPUT) {
            parentType.inputFields
        } else {
            parentType.fields
        }
        val searchedParameter = fields.stream()
            .filter { checkIfThisIsTheSearchedParameter(parameter, it.name, it.type) }
            .findFirst().orElse(null)
        if (searchedParameter == null) {
            deleteParameterAndChildren(parameter)
            clientRequestParameterRepository.delete(parameter)
        } else {
            if (parameter.clientRequestParameterDataType == "LIST" && parameter.clientRequestParameterOfTypeDataType.contains(
                    "OBJECT"
                ) || parameter.clientRequestParameterDataType.contains("OBJECT")
            ) {
                deleteAllNotNeededParameters(
                    graphQLSchemaResponse,
                    parameter,
                    clientRequestParameterRepository.findAllByParentClientRequestParameterId(
                        parameter
                    )
                )
            }
        }
    }

    private fun deleteParameterAndChildren(parameter: ClientRequestParameter) {
        val childrenParameters =
            clientRequestParameterRepository.findAllByParentClientRequestParameterId(parameter)
        for (childrenParameter in childrenParameters) {
            deleteParameterAndChildren(childrenParameter)
            clientRequestParameterRepository.delete(childrenParameter)
        }
        clientRequestParameterRepository.deleteById(parameter.clientRequestParameterId)
    }

    private fun createNewClientRequests(
        client: Client, graphQLSchemaResponse: GraphQLSchemaResponse
    ) {

        for (field in graphQLSchemaResponse.data.__schema.types.first { type -> type.name == "Query" }.fields) {
            if (field.name.equals("_service")) {
                continue
            }
            val clientRequest = ClientRequest()
            clientRequest.clientId = client
            clientRequest.clientRequestType = ClientRequestType.GRAPHQL
            clientRequest.clientRequestURI = client.clientURI
            clientRequest.clientRequestName = field.name
            clientRequestRepository.save(clientRequest)
            createClientRequestParameter(
                field.name,
                field.type,
                clientRequest,
                graphQLSchemaResponse,
                ParameterType.OUTPUT
            )
            for (arg in field.args) {
                createClientRequestParameter(
                    arg.name,
                    arg.type,
                    clientRequest,
                    graphQLSchemaResponse,
                    ParameterType.INPUT
                )
            }
        }
    }

    private fun createChildClientRequestParameter(
        type: GraphQLSchemaResponse.SchemaResponseData.SchemaResponseDataSchema.DataSchemaType,
        graphQLSchemaResponse: GraphQLSchemaResponse,
        parameterType: ParameterType,
        clientRequestParameter: ClientRequestParameter,
        clientRequest: ClientRequest
    ) {
        val fields = if (parameterType == ParameterType.INPUT) {
            type.inputFields
        } else {
            type.fields
        }

        for (field in fields) {
            if (field.name.equals("_service")) {
                continue
            }
            createClientRequestParameter(
                field.name,
                field.type,
                clientRequest,
                graphQLSchemaResponse,
                parameterType,
                clientRequestParameter
            )
        }
    }

    private fun createClientRequestParameter(
        fieldName: String,
        type: Type,
        clientRequest: ClientRequest,
        graphQLSchemaResponse: GraphQLSchemaResponse,
        parameterType: ParameterType,
        parentClientRequestParameter: ClientRequestParameter? = null
    ) {
        val clientRequestParameter = ClientRequestParameter()
        clientRequestParameter.clientRequestParameterType = parameterType
        clientRequestParameter.clientRequestParameterName = fieldName
        clientRequestParameter.clientRequestParameterDataType = type.kind
        clientRequestParameter.clientRequestParameterDataTypeName = type.name
        clientRequestParameter.clientRequestId = clientRequest
        if (parentClientRequestParameter != null) {
            clientRequestParameter.parentClientRequestParameterId = parentClientRequestParameter
        }
        when (type.kind) {
            "LIST" -> {
                clientRequestParameter.clientRequestParameterOfTypeDataType = type.ofType.kind
                clientRequestParameter.clientRequestParameterOfTypeDataTypeName =
                    type.ofType.name
                clientRequestParameterRepository.save(clientRequestParameter)
                createIfNeededChildClientRequestParameter(
                    type.ofType.name,
                    graphQLSchemaResponse,
                    parameterType,
                    clientRequestParameter,
                    clientRequest
                )
                return
            }
            "OBJECT", "INPUT_OBJECT" -> {
                clientRequestParameterRepository.save(clientRequestParameter)
                createIfNeededChildClientRequestParameter(
                    type.name,
                    graphQLSchemaResponse,
                    parameterType,
                    clientRequestParameter,
                    clientRequest
                )
                return
            }
        }
        clientRequestParameterRepository.save(clientRequestParameter)
    }

    private fun createIfNeededChildClientRequestParameter(
        fieldTypeName: String,
        graphQLSchemaResponse: GraphQLSchemaResponse,
        parameterType: ParameterType,
        clientRequestParameter: ClientRequestParameter,
        clientRequest: ClientRequest
    ) {
        graphQLSchemaResponse.data.__schema.types.forEach { type ->
            if (type.name == fieldTypeName && (type.fields != null && type.fields.isNotEmpty() || type.inputFields != null && type.inputFields.isNotEmpty())) {
                createChildClientRequestParameter(
                    type,
                    graphQLSchemaResponse,
                    parameterType,
                    clientRequestParameter,
                    clientRequest
                )
            }
        }
    }

    @Throws(IOException::class)
    fun readGetSchemasGraphQLFile(): String {
        val uri =
            javaClass.getClassLoader().getResource("graphql/get_schemas.graphql")!!.toURI()
        return String(Files.readAllBytes(Path.of(uri)))
    }
}