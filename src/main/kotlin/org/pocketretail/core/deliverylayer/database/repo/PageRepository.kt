package org.pocketretail.core.deliverylayer.database.repo

import org.pocketretail.core.deliverylayer.database.entity.Page
import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Mono

interface PageRepository: R2dbcRepository<Page, Int>{
    fun findPageByPageId(pageId: Int): Mono<Page>
}