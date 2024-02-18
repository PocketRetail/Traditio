package org.pocketretail.core.deliverylayer.database.repo

import org.pocketretail.core.deliverylayer.database.entity.PageClientRequestConfiguration
import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Flux

interface PageClientRequestConfigurationRepository :
    R2dbcRepository<PageClientRequestConfiguration, Int> {
    fun findPageClientRequestConfigurationsWithNeededParametersByPageId(pageId: Int): Flux<PageClientRequestConfiguration>

}