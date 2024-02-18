package org.pocketretail.core.deliverylayer.rest.controller.rendering

import org.pocketretail.core.deliverylayer.rest.handler.DeliveryLayerRenderingHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router

@Configuration
class DeliveryLayerRenderingRouter {

    @Bean
    fun deliveryLayerRenderingRoute(deliveryLayerRenderingHandler: DeliveryLayerRenderingHandler): RouterFunction<ServerResponse> =
        router {
            "/deliverylayer/api/v1/rendering".nest {
                GET("/load/{pageId}") { req ->
                    val pageId = req.pathVariable("pageId")
                    deliveryLayerRenderingHandler.loadPage(pageId, req)
                }
            }
        }
}
