package org.pocketretail.core.deliverylayer.rest.model;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeliveryLayerErrorResponse extends DeliveryLayerCommonResponse{


    private String stackTrace;

    public DeliveryLayerErrorResponse(String msg, Integer status, Date timestamp,
                                      String stackTrace) {
        super(msg, status, timestamp);
        this.stackTrace = stackTrace;
    }
}
