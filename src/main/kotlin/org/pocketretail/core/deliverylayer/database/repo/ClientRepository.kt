package org.pocketretail.core.deliverylayer.database.repo

import org.pocketretail.core.deliverylayer.database.entity.Client
import org.springframework.data.jpa.repository.JpaRepository

interface ClientRepository : JpaRepository<Client, String> {

    fun findClientByClientId(clientId: String): Client?
}