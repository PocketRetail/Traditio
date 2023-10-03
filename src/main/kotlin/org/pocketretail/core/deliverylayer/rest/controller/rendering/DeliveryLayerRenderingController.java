package org.pocketretail.core.deliverylayer.rest.controller.rendering;

import org.pocketretail.core.deliverylayer.rest.model.DeliveryLayerCommonResponse;
import org.pocketretail.core.deliverylayer.rest.model.DeliveryLayerResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Calendar;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/deliverylayer/api/v1/rendering")
public class DeliveryLayerRenderingController {


    @GetMapping("/load/{pageId}")
    public ResponseEntity<DeliveryLayerCommonResponse> loadPage(@PathVariable String pageId) {
        return ResponseEntity.ok(new DeliveryLayerResponse("Successfully loaded page " + pageId, 200,
                                                   Calendar.getInstance().getTime()));
    }
}
