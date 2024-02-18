package org.pocketretail.core.deliverylayer.rest.helper

import org.pocketretail.core.deliverylayer.database.entity.PageClientRequestConfiguration
import org.pocketretail.core.deliverylayer.database.repo.ClientRequestParameterRepository
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture

@Component
class PageRenderingHelper(
    private val clientRequestParameterRepository: ClientRequestParameterRepository
) {

}