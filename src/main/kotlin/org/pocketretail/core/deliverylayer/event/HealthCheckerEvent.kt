package org.pocketretail.core.deliverylayer.event

import org.pocketretail.core.deliverylayer.database.repo.ClientRepository
import org.pocketretail.core.deliverylayer.rest.client.HealthCheckWebClient
import org.pocketretail.core.deliverylayer.rest.client.exception.ApplicationNotUpException
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.util.retry.Retry
import java.time.Duration

@Component
class HealthCheckerEvent(
    private val clientRepository: ClientRepository,
) {

    @Scheduled(fixedRate = 300000)
    fun checkIfClientsAreUp() {
        clientRepository.findAll()
            .flatMap { client ->
                HealthCheckWebClient.checkHealth(client.clientURI)
                    .retryWhen(Retry.fixedDelay(5, Duration.ofSeconds(5)))
                    .onErrorResume { e: Throwable ->
                        if (e is ApplicationNotUpException) {
                            client.active = false
                            clientRepository.save(client).then()
                        } else {
                            Mono.error(e)
                        }
                    }
            }
            .subscribe(
                { /* Erfolgreiche Verarbeitung hier, falls nötig */ },
                { e -> println("Fehler beim Prüfen der Client-Health: ${e.message}") }
            )
    }
}
