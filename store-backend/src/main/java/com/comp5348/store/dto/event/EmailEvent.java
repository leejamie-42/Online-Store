package com.comp5348.store.dto.event;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.*;

/**
 * Email event for RabbitMQ messaging.
 *
 * Event Types:
 *
 * ORDER_CONFIRMATION - Sent after successful payment and stock commitment
 * PAYMENT_FAILED - Sent after failed payment attempt
 * REFUND_CONFIRMATION - Sent when refund is processed and completed
 * ORDER_CANCELLED - Sent when order is cancelled by user or system
 * DELIVERY_UPDATE - Sent when delivery status changes (picked up, in transit, delivered, lost)
 * ORDER_UPDATED - Sent when order status is updated (future implementation)
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailEvent {

    /**
     * Type of email event (ORDER_CONFIRMATION, PAYMENT_FAILED,
     * REFUND_CONFIRMATION, REFUND_SUCCESS).
     */
    private String type;

    /**
     * Recipient email address.
     */
    private String to;

    /**
     * Email template name to use.
     */
    private String template;

    /**
     * Template parameters (e.g., orderId, amount, customerName).
     */
    private Map<String, Object> params;

    /**
     * Unique event identifier for tracking.
     */
    private String eventId;

    /**
     * Event timestamp.
     */
    private LocalDateTime timestamp;
}
