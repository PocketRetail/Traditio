package org.pocketretail.core.deliverylayer.graphql.client;

import com.netflix.graphql.dgs.client.MonoGraphQLClient;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;


@Service
public class GraphQLWebFluxClient {

    public MonoGraphQLClient createGraphQLClient(String clientId) {
        return MonoGraphQLClient.createWithWebClient(
                WebClient.create("https://" + clientId + ".pocketretail.de/graphql"));
    }
}
