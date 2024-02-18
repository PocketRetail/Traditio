package org.pocketretail.core.deliverylayer.common.handler

import org.pocketretail.core.deliverylayer.common.util.ClientRequestUtil.Companion.readGetSchemasGraphQLFile
import org.pocketretail.core.deliverylayer.graphql.client.GraphQLWebFluxClient
import org.pocketretail.core.deliverylayer.graphql.response.GraphQLSchemaResponse
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class ClientRequestsHandler(private  val graphQLWebFluxClient: GraphQLWebFluxClient) {

    fun getGraphQLSchema(clientId: String): Mono<GraphQLSchemaResponse> {
        return graphQLWebFluxClient.createGraphQLClient(clientId)
            .reactiveExecuteQuery(readGetSchemasGraphQLFile()).flatMap { response ->
                if (response != null) {
                    Mono.just(
                        GraphQLSchemaResponse(
                            GraphQLSchemaResponse.fromGraphQLResponse(
                                response
                            )
                        )
                    )
                } else {
                    Mono.error(RuntimeException("GraphQL response is null"))
                }
            }
    }
}