package com.comp5348.store.exception;

public class DuplicateRefundException extends RuntimeException {
    public DuplicateRefundException(String message) {
        super(message);
    }
}
