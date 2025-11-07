package com.comp5348.store.exception;

/**
 * Exception thrown when an order is not found or user doesn't have access.
 *
 * <p>This exception is thrown when:
 * <ul>
 *   <li>Order ID doesn't exist in the database</li>
 *   <li>User tries to access an order they don't own</li>
 * </ul>
 *
 * <p>Note: Access denial is handled separately by AccessDeniedException
 * for security reasons. This exception is only for "not found" cases.
 */
public class OrderNotFoundException extends RuntimeException {

    private final Long orderId;

    /**
     * Constructor with order ID.
     *
     * @param orderId the order ID that was not found
     */
    public OrderNotFoundException(Long orderId) {
        super("Order not found with id: " + orderId);
        this.orderId = orderId;
    }

    /**
     * Constructor with custom message.
     *
     * @param message the custom error message
     */
    public OrderNotFoundException(String message) {
        super(message);
        this.orderId = null;
    }

    public Long getOrderId() {
        return orderId;
    }
}
