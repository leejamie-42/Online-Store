package com.comp5348.bank.exception;

public class DuplicateBpayException extends RuntimeException {
    public DuplicateBpayException(String message) {
        super(message);
    }
}
