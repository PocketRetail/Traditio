package org.pocketretail.core.deliverylayer.rest.client;


import org.pocketretail.core.deliverylayer.rest.client.exception.ApplicationNotUpException;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class HealthCheckWebClient {

    private HealthCheckWebClient() {
    }

    public static Mono<Void> checkHealth(String url) {
        WebClient webClient = createWebClient(url);
        return webClient.get()
                        .retrieve()
                        .bodyToMono(HealthResponse.class)
                        .flatMap(test -> {
                            if (test == null || !"UP".equals(test.getStatus())) {
                                return Mono.error(new ApplicationNotUpException());
                            }
                            return Mono.empty();
                        });
    }

    private static WebClient createWebClient(String url) {
        return WebClient.create(url + "/actuator/health");
    }


    public static class HealthResponse{
        private String status;

        public HealthResponse(String status) {
            this.status = status;
        }

        public HealthResponse() {
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
