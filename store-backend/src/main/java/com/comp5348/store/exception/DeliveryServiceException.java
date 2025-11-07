package com.comp5348.store.exception;

public class DeliveryServiceException extends RuntimeException {
    public DeliveryServiceException(String message) {
        super(message);
    }

    public DeliveryServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}

