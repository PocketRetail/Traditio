package org.pocketretail.core.deliverylayer.ui.handler

import org.pocketretail.core.deliverylayer.database.entity.Client
import org.pocketretail.core.deliverylayer.database.entity.ClientRequest
import org.pocketretail.core.deliverylayer.database.repo.ClientRepository
import org.pocketretail.core.deliverylayer.database.repo.ClientRequestRepository
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class ClientsUIHandler(
    private val clientRepository: ClientRepository,
    private val clientRequestRepository: ClientRequestRepository
) {

    fun getAllClients(): Flux<Client> {
        return clientRepository.findAll()
    }

    fun getClient(clientId: String): Mono<Client> {
        return clientRepository.findById(clientId)
    }

    fun getClientRequests(client: Client): Flux<ClientRequest> {
        return clientRequestRepository.findAllByClientId(client.clientId)
    }
}