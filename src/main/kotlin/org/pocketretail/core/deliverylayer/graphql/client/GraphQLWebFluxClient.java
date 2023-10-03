package org.pocketretail.core.deliverylayer.graphql.client;

import com.netflix.graphql.dgs.client.MonoGraphQLClient;

import org.springframework.web.reactive.function.client.WebClient;


public class GraphQLWebFluxClient {

    public static MonoGraphQLClient createGraphQLClient(String clientId) {
        return MonoGraphQLClient.createWithWebClient(
                WebClient.create("https://" + clientId + ".pocketretail.de/graphql"));
    }
}
