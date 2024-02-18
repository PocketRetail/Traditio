package org.pocketretail.core.deliverylayer.rest.controller.registration

import org.pocketretail.core.deliverylayer.rest.handler.DeliveryLayerRegistrationHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router

@Configuration
class DeliveryLayerRegistrationRouter {

    @Bean
    fun deliveryLayerRegistrationRoute(deliveryLayerRegistrationHandler: DeliveryLayerRegistrationHandler): RouterFunction<ServerResponse> =
        router {
            "/deliverylayer/api/v1/registration".nest {
                POST("/{clientId}") { req ->
                    val clientId = req.pathVariable("clientId")
                    deliveryLayerRegistrationHandler.registerClient(clientId)
                        .flatMap { response ->
                            ServerResponse.ok().bodyValue(response)
                        }
                }
            }
        }
}
