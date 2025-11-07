package com.comp5348.store.model.payment;

public enum PaymentStatus {
    PENDING,       // Payment initiated, awaiting confirmation
    PROCESSING,    // Payment being processed by Bank
    COMPLETED,     // Payment successfully completed
    FAILED,        // Payment failed
    REFUNDED;      // Payment refunded

    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED || this == REFUNDED;
    }

    public boolean canTransitionTo(PaymentStatus newStatus) {
        return switch (this) {
            case PENDING -> newStatus == PROCESSING || newStatus == FAILED;
            case PROCESSING -> newStatus == COMPLETED || newStatus == FAILED;
            case COMPLETED -> newStatus == REFUNDED;
            case FAILED, REFUNDED -> false;
        };
    }
}
