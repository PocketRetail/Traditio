package org.pocketretail.core.deliverylayer.ui

import org.pocketretail.core.deliverylayer.ui.handler.UIActionHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router

@Configuration
class UIActionRouter {
    @Bean
    fun routeUIAction(uiActionHandler: UIActionHandler): RouterFunction<ServerResponse> =
        router {
            POST("/deliverylayer/ui/action/refresh-client-requests", uiActionHandler::refreshClientRequests)
        }
}
