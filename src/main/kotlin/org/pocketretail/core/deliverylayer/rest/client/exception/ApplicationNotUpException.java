package org.pocketretail.core.deliverylayer.rest.client.exception;

public class ApplicationNotUpException extends Exception {

    public ApplicationNotUpException() {
        super("Application is not up");
    }
}
