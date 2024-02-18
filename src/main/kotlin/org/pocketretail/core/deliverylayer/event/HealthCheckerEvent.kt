package org.pocketretail.core.deliverylayer.event;

import org.pocketretail.core.deliverylayer.rest.client.HealthCheckWebClient;
import org.pocketretail.core.deliverylayer.rest.client.exception.ApplicationNotUpException;
import org.pocketretail.core.deliverylayer.database.repo.ClientRepository;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class HealthCheckerEvent {

    private final ClientRepository clientRepository;


    public HealthCheckerEvent(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }


    //Event which triggers every 5 Minutes to check if all Clients still up
    @Scheduled(fixedRate = 300000)
    public void checkIfClientsAreUp() {
        //Check all Clients with HealthCheckWebClient, if it fails try again (max 5 times)
        clientRepository.findAll().forEach(client -> {
            try {
                HealthCheckWebClient.checkHealth(client.getClientURI());
            } catch (ApplicationNotUpException e) {
                client.setActive(false);
                clientRepository.save(client);
                //TODO: Add Alert Mail Sending
            }
        });

    }

}
