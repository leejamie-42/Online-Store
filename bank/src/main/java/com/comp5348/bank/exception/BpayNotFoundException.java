package com.comp5348.bank.exception;

public class BpayNotFoundException extends RuntimeException {
    public BpayNotFoundException(String message) {
        super(message);
    }
}
