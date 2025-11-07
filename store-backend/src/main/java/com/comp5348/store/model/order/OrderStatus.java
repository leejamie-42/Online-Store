package com.comp5348.store.model.order;

/**
 * Order status enum representing the lifecycle of an order.
 *
 * <p>Status Transitions:</p>
 * <ul>
 *   <li>PENDING → PROCESSING (after payment confirmation)</li>
 *   <li>PROCESSING → PICKED_UP (warehouse fulfillment)</li>
 *   <li>PICKED_UP → DELIVERING (shipment initiated)</li>
 *   <li>DELIVERING → DELIVERED (delivery confirmed)</li>
 *   <li>Any status → CANCELLED (user/admin cancellation)</li>
 * </ul>
 */
public enum OrderStatus {
    /**
     * Initial status when order is created.
     * Awaiting payment confirmation.
     */
    PENDING,

    /**
     * Payment confirmed, order being prepared.
     * Warehouse stock reservation confirmed.
     */
    PROCESSING,

    /**
     * Order picked up from warehouse.
     * Ready for delivery handoff.
     */
    PICKED_UP,

    /**
     * Order in transit to customer.
     * Shipment tracking active.
     */
    DELIVERING,

    /**
     * Order successfully delivered to customer.
     * Terminal status (success).
     */
    DELIVERED,

    /**
     * Order cancelled by user or system.
     * Requires inventory rollback and refund processing.
     * Terminal status (failure).
     */
    CANCELLED;

    /**
     * Check if this status represents a terminal state.
     *
     * @return true if status is DELIVERED or CANCELLED
     */
    public boolean isTerminal() {
        return this == DELIVERED || this == CANCELLED;
    }

    /**
     * Check if order can be cancelled from this status.
     *
     * @return true if status is PENDING or PROCESSING
     */
    public boolean isCancellable() {
        return this == PENDING || this == PROCESSING;
    }

    /**
     * Get the next valid status in the normal order flow.
     *
     * @return next status, or null if terminal
     */
    public OrderStatus getNextStatus() {
        return switch (this) {
            case PENDING -> PROCESSING;
            case PROCESSING -> PICKED_UP;
            case PICKED_UP -> DELIVERING;
            case DELIVERING -> DELIVERED;
            case DELIVERED, CANCELLED -> null;
        };
    }
}
