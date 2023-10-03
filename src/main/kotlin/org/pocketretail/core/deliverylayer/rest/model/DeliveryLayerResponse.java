package org.pocketretail.core.deliverylayer.rest.model;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeliveryLayerResponse extends DeliveryLayerCommonResponse {

    private Object data;

    public DeliveryLayerResponse(String msg, Integer status, Date timestamp) {
        super(msg, status, timestamp);
    }

    public DeliveryLayerResponse(String msg, Integer status, Date timestamp, Object data) {
        super(msg, status, timestamp);
        this.data = data;
    }
}
