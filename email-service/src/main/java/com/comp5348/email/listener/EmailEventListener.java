package com.comp5348.email.listener;

import com.comp5348.email.config.RabbitMQConfig;
import com.comp5348.email.dto.EmailEventDto;
import com.comp5348.email.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Listens for email events from RabbitMQ email.queue
 * Sends email notifications for order lifecycle events
 *
 * Handles event types:
 * - ORDER_CONFIRMATION: Order placed successfully
 * - PAYMENT_FAILED: Payment processing failed
 * - REFUND_CONFIRMATION: Refund processed
 * - ORDER_CANCELLED: Order cancelled
 * - ORDER_UPDATED: Order status changed
 * - DELIVERY_UPDATE: Shipment status changed
 */
@Component
public class EmailEventListener {

    private static final Logger log = LoggerFactory.getLogger(
        EmailEventListener.class
    );

    private final EmailService emailService;

    public EmailEventListener(EmailService emailService) {
        this.emailService = emailService;
    }

    /**
     * Listen to email queue for email events from store-backend
     * Uses JSON message converter from RabbitMQ config
     */
    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
    public void handleEmailEvent(
        @Payload EmailEventDto event,
        @Header(value = "amqp_messageId", required = false) String messageId
    ) {
        log.info(
            "Received email event: type={}, eventId={}, to={}",
            event.type,
            event.eventId,
            event.to
        );

        try {
            // Extract order data from params
            Long orderId = extractLong(event.params, "orderId");
            String customerName = extractString(event.params, "customerName");

            // Generate email content based on event type
            String subject = generateSubject(event, orderId);
            String body = generateEmailBody(event, customerName);

            // Send/print email notification
            emailService.sendEmail(
                orderId,
                event.type,
                event.to,
                subject,
                body,
                event.eventId
            );

            log.info(
                "Successfully processed email event: eventId={}",
                event.eventId
            );
        } catch (Exception e) {
            // Log error but don't throw - we don't want to retry endlessly
            // Failed emails are logged by EmailService
            log.error(
                "Error processing email event: eventId={}, error={}",
                event.eventId,
                e.getMessage(),
                e
            );
        }
    }

    /**
     * Generate email subject based on event type
     */
    private String generateSubject(EmailEventDto event, Long orderId) {
        String orderRef = orderId != null ? " - Order #" + orderId : "";

        switch (event.type) {
            case "ORDER_CONFIRMATION":
                return "Order Confirmation" + orderRef;
            case "PAYMENT_FAILED":
                return "Payment Failed" + orderRef;
            case "REFUND_CONFIRMATION":
                return "Refund Processed" + orderRef;
            case "ORDER_CANCELLED":
                return "Order Cancelled" + orderRef;
            case "ORDER_UPDATED":
                return "Order Status Update" + orderRef;
            case "DELIVERY_UPDATE":
                return "Delivery Update" + orderRef;
            default:
                return "Notification" + orderRef;
        }
    }

    /**
     * Generate email body with event details
     */
    private String generateEmailBody(EmailEventDto event, String customerName) {
        StringBuilder body = new StringBuilder();
        body
            .append("Dear ")
            .append(customerName != null ? customerName : "Customer")
            .append(",\n\n");

        switch (event.type) {
            case "ORDER_CONFIRMATION":
                body.append("Thank you for your order!\n\n");
                body.append("Order Details:\n");
                appendOrderDetails(body, event.params);
                body.append("\nWe'll notify you when your order ships.\n");
                break;
            case "PAYMENT_FAILED":
                body.append("We were unable to process your payment.\n\n");
                body.append("Order Details:\n");
                appendOrderDetails(body, event.params);
                String reason = extractString(event.params, "reason");
                if (reason != null) {
                    body.append("\nReason: ").append(reason).append("\n");
                }
                body.append(
                    "\nPlease update your payment method and try again.\n"
                );
                break;
            case "REFUND_CONFIRMATION":
                body.append("Your refund has been processed.\n\n");
                Double refundAmount = extractDouble(event.params, "amount");
                if (refundAmount != null) {
                    body
                        .append("Refund Amount: $")
                        .append(String.format("%.2f", refundAmount))
                        .append("\n");
                }
                body.append(
                    "\nPlease allow 3-5 business days for the refund to appear in your account.\n"
                );
                break;
            case "ORDER_CANCELLED":
                body.append("Your order has been cancelled.\n\n");
                appendOrderDetails(body, event.params);
                String cancelReason = extractString(event.params, "reason");
                if (cancelReason != null) {
                    body.append("\nReason: ").append(cancelReason).append("\n");
                }
                body.append(
                    "\nIf you have any questions, please contact our support team.\n"
                );
                break;
            case "ORDER_UPDATED":
                body.append("Your order status has been updated.\n\n");
                appendOrderDetails(body, event.params);
                String newStatus = extractString(event.params, "status");
                if (newStatus != null) {
                    body
                        .append("\nNew Status: ")
                        .append(newStatus)
                        .append("\n");
                }
                break;
            case "DELIVERY_UPDATE":
                body.append("Your delivery status has been updated.\n\n");
                appendOrderDetails(body, event.params);
                String deliveryStatus = extractString(event.params, "status");
                if (deliveryStatus != null) {
                    body
                        .append("\nDelivery Status: ")
                        .append(deliveryStatus)
                        .append("\n");
                }
                break;
            default:
                body.append("This is a notification about your order.\n\n");
                appendOrderDetails(body, event.params);
        }

        body.append("\nBest regards,\nThe Store Team");

        return body.toString();
    }

    /**
     * Append common order details to email body
     */
    private void appendOrderDetails(
        StringBuilder body,
        java.util.Map<String, Object> params
    ) {
        Long orderId = extractLong(params, "orderId");
        if (orderId != null) {
            body.append("- Order ID: ").append(orderId).append("\n");
        }

        String productName = extractString(params, "productName");
        if (productName != null) {
            body.append("- Product: ").append(productName).append("\n");
        }

        Integer quantity = extractInteger(params, "quantity");
        if (quantity != null) {
            body.append("- Quantity: ").append(quantity).append("\n");
        }

        Double amount = extractDouble(params, "amount");
        if (amount != null) {
            body
                .append("- Amount: $")
                .append(String.format("%.2f", amount))
                .append("\n");
        }
    }

    /**
     * Helper: Extract Long from params map
     */
    private Long extractLong(java.util.Map<String, Object> params, String key) {
        if (params == null || !params.containsKey(key)) {
            return null;
        }
        Object value = params.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return null;
    }

    /**
     * Helper: Extract Double from params map
     */
    private Double extractDouble(
        java.util.Map<String, Object> params,
        String key
    ) {
        if (params == null || !params.containsKey(key)) {
            return null;
        }
        Object value = params.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return null;
    }

    /**
     * Helper: Extract Integer from params map
     */
    private Integer extractInteger(
        java.util.Map<String, Object> params,
        String key
    ) {
        if (params == null || !params.containsKey(key)) {
            return null;
        }
        Object value = params.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;
    }

    /**
     * Helper: Extract String from params map
     */
    private String extractString(
        java.util.Map<String, Object> params,
        String key
    ) {
        if (params == null || !params.containsKey(key)) {
            return null;
        }
        Object value = params.get(key);
        return value != null ? value.toString() : null;
    }
}
