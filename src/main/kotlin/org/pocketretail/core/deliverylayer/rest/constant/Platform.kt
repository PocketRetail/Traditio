package org.pocketretail.core.deliverylayer.rest.constant;

import lombok.Getter;

@Getter
public enum Platform {

    MOBILE("mobile"),
    WEB("web");

    Platform(String value) {
        this.value = value;
    }

    private String value;


}
