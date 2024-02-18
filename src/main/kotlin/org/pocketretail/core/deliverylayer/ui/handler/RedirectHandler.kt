package org.pocketretail.core.deliverylayer.ui.handler

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.net.URI

@Component
class RedirectHandler {

    fun redirectFromUIRoot(): Mono<ServerResponse> {
        return ServerResponse.temporaryRedirect(URI.create("/deliverylayer/ui/home")).build()
    }
}
