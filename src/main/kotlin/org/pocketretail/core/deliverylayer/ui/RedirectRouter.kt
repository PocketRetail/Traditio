package org.pocketretail.core.deliverylayer.ui

import org.pocketretail.core.deliverylayer.ui.handler.RedirectHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router

@Configuration
class RedirectRouter {

    @Bean
    fun redirectRoute(redirectHandler: RedirectHandler): RouterFunction<ServerResponse> =
        router {
            // Verwendung einer Route mit einer Handler-Funktion
            GET("/deliverylayer/ui") { redirectHandler.redirectFromUIRoot() }
            GET("/deliverylayer/ui/") { redirectHandler.redirectFromUIRoot() }
        }
}
