package com.comp5348.store.exception;

/**
 * Exception thrown when there is insufficient stock to fulfill an order.
 *
 * <p>This exception is thrown when:
 * <ul>
 *   <li>Warehouse reports stock unavailability</li>
 *   <li>Stock reservation fails due to optimistic locking</li>
 *   <li>Requested quantity exceeds available inventory</li>
 * </ul>
 */
public class InsufficientStockException extends RuntimeException {

    private final Long productId;
    private final Integer requestedQuantity;
    private final Integer availableQuantity;

    /**
     * Constructor with product details.
     *
     * @param productId the product ID
     * @param requestedQuantity the requested quantity
     * @param availableQuantity the available quantity
     */
    public InsufficientStockException(
        Long productId,
        Integer requestedQuantity,
        Integer availableQuantity
    ) {
        super(
            String.format(
                "Insufficient stock for product %d. Requested: %d, Available: %d",
                productId,
                requestedQuantity,
                availableQuantity
            )
        );
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }

    /**
     * Constructor with custom message.
     *
     * @param message the custom error message
     */
    public InsufficientStockException(String message) {
        super(message);
        this.productId = null;
        this.requestedQuantity = null;
        this.availableQuantity = null;
    }

    public Long getProductId() {
        return productId;
    }

    public Integer getRequestedQuantity() {
        return requestedQuantity;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }
}
