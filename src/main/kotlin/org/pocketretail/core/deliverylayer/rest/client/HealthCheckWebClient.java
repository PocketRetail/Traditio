package org.pocketretail.core.deliverylayer.rest.client;


import org.pocketretail.core.deliverylayer.rest.client.exception.ApplicationNotUpException;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class HealthCheckWebClient {

    private HealthCheckWebClient() {
    }

    public static void checkHealth(String url) throws ApplicationNotUpException {
        WebClient webClient = createWebClient(url);
        HealthResponse test = webClient.get().retrieve().bodyToMono(HealthResponse.class).block();
        if (test == null || !test.getStatus().equals("UP")) throw new ApplicationNotUpException();
    }

    private static WebClient createWebClient(String url) {
        return WebClient.create(url + "/actuator/health");
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class HealthResponse{
        private String status;
    }
}
