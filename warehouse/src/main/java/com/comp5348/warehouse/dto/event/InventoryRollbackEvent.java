package com.comp5348.warehouse.dto.event;

import lombok.*;
import java.time.LocalDateTime;

/**
 * Inventory rollback event consumed from store-backend.
 *
 * This event is published by store-backend when inventory needs to be rolled back
 * due to order cancellations, payment failures, or shipment losses.
 *
 * The warehouse service consumes these events and performs the rollback operation:
 * 1. Find reservations by order ID
 * 2. Restore inventory quantities
 * 3. Delete reservations
 * 4. Publish product update to sync stock with store-backend
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryRollbackEvent {
    /**
     * Order ID for which stock should be rolled back.
     */
    private Long orderId;

    /**
     * Product ID to rollback.
     */
    private Long productId;

    /**
     * Quantity to restore.
     */
    private Integer amount;

    /**
     * Reason for rollback (e.g., "Order cancelled", "Payment failed", "Shipment lost").
     */
    private String reason;

    /**
     * Unique event identifier for tracking and idempotency.
     */
    private String eventId;

    /**
     * Event timestamp.
     */
    private LocalDateTime timestamp;
}
