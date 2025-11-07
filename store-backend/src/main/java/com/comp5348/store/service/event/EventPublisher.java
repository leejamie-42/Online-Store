package com.comp5348.store.service.event;

import com.comp5348.store.config.RabbitMQConfig;
import com.comp5348.store.dto.event.EmailEvent;
import com.comp5348.store.dto.event.InventoryRollbackEvent;
import com.comp5348.store.dto.event.PaymentRefundEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * Centralized service for publishing events to RabbitMQ.
 *
 * This service handles all asynchronous event publishing for:
 *
 * Email notifications (order confirmations, payment events, refunds)
 * Inventory rollback events (optional - sync gRPC preferred)
 *
 *
 * Error Handling Strategy:
 *
 * All publishing operations catch exceptions and log them without throwing.
 * This ensures that event publishing failures don't break the main business
 * flow.
 * Failed messages are automatically routed to Dead Letter Queues for manual
 * inspection.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Publish email notification event.
     *
     * This is a non-blocking operation. Failures are logged but don't break the
     * main flow.
     * The email queue has retry and DLQ configured for failed messages.
     *
     * @param event EmailEvent with type, recipient, template, and parameters
     */
    public void publishEmailEvent(EmailEvent event) {
        try {
            log.info(
                "Publishing email event: type={}, to={}",
                event.getType(),
                event.getTo()
            );

            String routingKey = "email." + event.getType().toLowerCase();

            rabbitTemplate.convertAndSend(
                RabbitMQConfig.TOPIC_EXCHANGE,
                routingKey,
                event
            );

            log.debug(
                "Successfully published email event with routing key: {}",
                routingKey
            );
        } catch (Exception e) {
            log.error("Failed to publish email event: {}", e.getMessage(), e);
            // Don't throw - email failure shouldn't break main transaction
        }
    }

    /**
     * Publish inventory rollback event.
     *
     * This is an optional async alternative to synchronous gRPC rollbackStock()
     * calls.
     * In most cases, synchronous gRPC is preferred for immediate consistency.
     * Use this only for non-critical rollback scenarios where eventual consistency
     * is acceptable.
     *
     * @param event InventoryRollbackEvent with order, warehouse, product, and
     *              reason
     */
    public void publishInventoryRollbackEvent(InventoryRollbackEvent event) {
        try {
            log.info(
                "Publishing rollback event: orderId={}, reason={}",
                event.getOrderId(),
                event.getReason()
            );

            rabbitTemplate.convertAndSend(
                RabbitMQConfig.TOPIC_EXCHANGE,
                "inventory.rollback.request",
                event
            );

            log.debug("Successfully published inventory rollback event");
        } catch (Exception e) {
            log.error(
                "Failed to publish rollback event: {}",
                e.getMessage(),
                e
            );
            // Don't throw - use sync gRPC as fallback
        }
    }

    public void publishPaymentRefundEvent(PaymentRefundEvent event) {
        try {
            log.info(
                "Publishing payment refund event: orderId={}, reason={}",
                event.getOrderId(),
                event.getReason()
            );

            rabbitTemplate.convertAndSend(
                RabbitMQConfig.TOPIC_EXCHANGE,
                "payment.refund.request",
                event
            );

            log.debug(
                "Successfully published payment refund event: ",
                event.getEventId()
            );
        } catch (Exception e) {
            log.error("Failed to publish email event: {}", e.getMessage(), e);
            // Don't throw - email failure shouldn't break main transaction
        }
    }
}
