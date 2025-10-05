package org.pocketretail.core.deliverylayer.ui

import org.pocketretail.core.deliverylayer.ui.handler.ClientsUIHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.web.reactive.function.server.RequestPredicates.GET
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions.route
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

@Configuration
class UIRouter(private val clientUIHandler: ClientsUIHandler) {

    @Bean
    fun route(): RouterFunction<ServerResponse> {
        return route(GET("/deliverylayer/ui/home"), this::home)
            .andRoute(GET("/deliverylayer/ui/clients"), this::clients)
            .andRoute(GET("/deliverylayer/ui/clients/client/{clientId}"), this::client)
    }

    private fun home(request: ServerRequest): Mono<ServerResponse> {
        return ReactiveSecurityContextHolder.getContext()
            .map { it.authentication.principal as OAuth2User }
            .flatMap { user ->
                ServerResponse.ok().render("home", mapOf("user" to user.attributes))
            }
    }

    private fun clients(request: ServerRequest): Mono<ServerResponse> {
        return ReactiveSecurityContextHolder.getContext()
            .map { it.authentication.principal as OAuth2User }
            .flatMap { user ->
                clientUIHandler.getAllClients()
                    .collectList()
                    .flatMap { clients ->
                        ServerResponse.ok().render(
                            "clients/clients",
                            mapOf("clients" to clients, "user" to user.attributes)
                        )
                    }
            }
    }

    private fun client(request: ServerRequest): Mono<ServerResponse> {
        val clientId = request.pathVariable("clientId")
        return ReactiveSecurityContextHolder.getContext()
            .map { it.authentication.principal as OAuth2User }
            .flatMap { user ->
                clientUIHandler.getClient(clientId)
                    .flatMap { client ->
                        clientUIHandler.getClientRequests(client)
                            .collectList()
                            .flatMap { requests ->
                                ServerResponse.ok().render(
                                    "clients/client",
                                    mapOf(
                                        "client" to client,
                                        "requests" to requests,
                                        "user" to user.attributes
                                    )
                                )
                            }
                    }
            }
    }
}