package com.comp5348.store.dto.event;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Inventory rollback event for RabbitMQ messaging.
 *
 * This event is published when inventory needs to be rolled back
 * due to order cancellations or refunds. It's an optional async alternative
 * to synchronous gRPC rollback calls.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
   * Reason for rollback (e.g., "Order cancelled", "Payment failed").
   */
  private String reason;

  /**
   * Unique event identifier for tracking.
   */
  private String eventId;

  /**
   * Event timestamp.
   */
  private LocalDateTime timestamp;
}
