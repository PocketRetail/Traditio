package org.pocketretail.core.deliverylayer.rest.controller.registration;

import org.pocketretail.core.deliverylayer.rest.handler.DeliveryLayerRegistrationHandler;
import org.pocketretail.core.deliverylayer.rest.model.DeliveryLayerCommonResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/deliverylayer/api/v1/registration")
public class DeliveryLayerRegistrationController {

        private final DeliveryLayerRegistrationHandler deliveryLayerRegistrationHandler;

        public DeliveryLayerRegistrationController(
                DeliveryLayerRegistrationHandler deliveryLayerRegistrationHandler) {
                this.deliveryLayerRegistrationHandler = deliveryLayerRegistrationHandler;

        }


        @PostMapping("/{clientId}")
        public ResponseEntity<DeliveryLayerCommonResponse> registerClient(
                @PathVariable String clientId) {
                return ResponseEntity.ok(deliveryLayerRegistrationHandler.registerClient(clientId));
        }

}



