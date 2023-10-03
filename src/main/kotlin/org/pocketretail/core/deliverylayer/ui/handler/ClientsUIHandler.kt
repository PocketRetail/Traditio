package org.pocketretail.core.deliverylayer.ui.handler

import org.pocketretail.core.deliverylayer.database.entity.Client
import org.pocketretail.core.deliverylayer.database.repo.ClientRepository
import org.springframework.stereotype.Component

@Component
class ClientsUIHandler(
    private val clientRepository: ClientRepository
) {

    fun getAllClients():List<Client>{
        return clientRepository.findAll()
    }
}