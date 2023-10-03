package org.pocketretail.core.deliverylayer.rest.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public abstract class DeliveryLayerCommonResponse {

    private String msg;
    private Integer status;
    private Date timestamp;
}
