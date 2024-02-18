package org.pocketretail.core.deliverylayer.rest.handler

import org.pocketretail.core.deliverylayer.rest.constant.Application.Companion.getApplicationByName
import org.pocketretail.core.deliverylayer.rest.constant.DeliveryLayerHeader
import org.pocketretail.core.deliverylayer.rest.constant.Platform.Companion.getPlatformByName
import org.pocketretail.core.deliverylayer.rest.model.DeliveryLayerResponse
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.util.*

@Component
class DeliveryLayerRenderingHandler {

    fun loadPage(pageId: String, request: ServerRequest): Mono<ServerResponse> {
        val application = getApplicationByName(
            request.headers().firstHeader(DeliveryLayerHeader.APPLICATION_HEADER.headerName)
                ?: throw IllegalArgumentException("Application Header not found")
        )
        val platform = getPlatformByName(
            request.headers().firstHeader(DeliveryLayerHeader.PLATFORM_HEADER.headerName)
                ?: throw IllegalArgumentException("Platform Header not found")
        )

        val response = DeliveryLayerResponse(
            "Successfully loaded page $pageId",
            200,
            Calendar.getInstance().time
        )
        return ServerResponse.ok().bodyValue(response)
    }
}
