package org.pocketretail.core.deliverylayer.database.repo

import org.pocketretail.core.deliverylayer.database.entity.ClientRequest
import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Flux

interface ClientRequestRepository : R2dbcRepository<ClientRequest, Int> {
    fun findAllByClientId(clientId: String): Flux<ClientRequest>
}