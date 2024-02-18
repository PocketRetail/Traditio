package org.pocketretail.core.deliverylayer.database.repo

import org.pocketretail.core.deliverylayer.database.entity.Client
import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Mono

interface ClientRepository : R2dbcRepository<Client, String> {

    fun findClientByClientId(clientId: String): Mono<Client>
}