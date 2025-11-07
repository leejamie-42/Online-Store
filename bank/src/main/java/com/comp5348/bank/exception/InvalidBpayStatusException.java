package com.comp5348.bank.exception;

public class InvalidBpayStatusException extends RuntimeException {
    public InvalidBpayStatusException(String message) {
        super(message);
    }
}
