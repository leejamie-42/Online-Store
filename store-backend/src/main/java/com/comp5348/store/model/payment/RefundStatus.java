package com.comp5348.store.model.payment;

public enum RefundStatus {
    PENDING,       // Refund requested
    PROCESSING,    // Refund being processed
    COMPLETED,     // Refund completed
    FAILED;        // Refund failed

    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED;
    }
}
