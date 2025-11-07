package com.comp5348.email.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Email event DTO matching store-backend EmailEvent structure
 * Consumed from RabbitMQ email.queue
 *
 * Event types:
 * - ORDER_CONFIRMATION: Order placed successfully
 * - PAYMENT_FAILED: Payment processing failed
 * - REFUND_CONFIRMATION: Refund processed
 * - ORDER_CANCELLED: Order cancelled by user/system
 * - ORDER_UPDATED: Order status changed
 * - DELIVERY_UPDATE: Shipment status changed
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmailEventDto {

    /**
     * Event type (ORDER_CONFIRMATION, PAYMENT_FAILED, etc.)
     */
    public String type;

    /**
     * Recipient email address
     */
    public String to;

    /**
     * Email template name (for future template engine integration)
     */
    public String template;

    /**
     * Template parameters - contains order data:
     * - orderId (Long): Order ID
     * - amount (Double): Order/payment amount
     * - customerName (String): Customer full name
     * - productName (String): Product name
     * - quantity (Integer): Order quantity
     * - reason (String): Failure/cancellation reason
     * - status (String): Order/delivery status
     */
    public Map<String, Object> params;

    /**
     * Unique event ID for idempotency checking
     */
    public String eventId;

    /**
     * Event timestamp
     */
    public LocalDateTime timestamp;

    // Default constructor for Jackson deserialization
    public EmailEventDto() {}

    // Getters and setters for Jackson
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }

    public String getTemplate() { return template; }
    public void setTemplate(String template) { this.template = template; }

    public Map<String, Object> getParams() { return params; }
    public void setParams(Map<String, Object> params) { this.params = params; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
