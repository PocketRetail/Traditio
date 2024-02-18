package org.pocketretail.core.deliverylayer.ui.handler

import org.pocketretail.core.deliverylayer.common.handler.ClientRequestsHandler
import org.pocketretail.core.deliverylayer.rest.handler.DeliveryLayerRegistrationHandler
import org.pocketretail.core.deliverylayer.ui.response.ClientResponseObject
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

@Component
class UIActionHandler(
    private val clientUIHandler: ClientsUIHandler,
    private val clientRequestsHandler: ClientRequestsHandler,
    private val deliveryLayerRegistrationHandler: DeliveryLayerRegistrationHandler
) {
    fun refreshClientRequests(serverRequest: ServerRequest): Mono<ServerResponse> {
        val clientId = serverRequest.queryParam("clientId").orElseThrow()

        return clientUIHandler.getClient(clientId)
            .flatMap { client ->
                clientRequestsHandler.getGraphQLSchema(clientId)
                    .flatMap { graphQLSchemaResponse ->
                        clientUIHandler.getClientRequests(client).collectList().flatMap { clientRequest ->
                            deliveryLayerRegistrationHandler.updateOldClientRequestsAndCreateNew(
                                client,
                                graphQLSchemaResponse,
                                clientRequest
                            )
                        }
                    }
            }
            .then(
                ServerResponse.ok().bodyValue(
                    ClientResponseObject(
                        "OK",
                        200,
                        "Client requests refreshed successfully"
                    )
                )
            )
    }
}
