package org.pocketretail.core.deliverylayer.database.repo

import org.pocketretail.core.deliverylayer.database.entity.Client
import org.pocketretail.core.deliverylayer.database.entity.ClientRequest
import org.springframework.data.jpa.repository.JpaRepository

interface ClientRequestRepository: JpaRepository<ClientRequest, Int> {
    fun findAllByClientId(clientId: Client): List<ClientRequest>
}