package com.comp5348.warehouse.listener;

import com.comp5348.warehouse.config.RabbitMQConfig;
import com.comp5348.warehouse.dto.event.InventoryRollbackEvent;
import com.comp5348.warehouse.dto.event.ProductUpdateEvent;
import com.comp5348.warehouse.model.Inventory;
import com.comp5348.warehouse.model.Reservation;
import com.comp5348.warehouse.model.WarehouseProduct;
import com.comp5348.warehouse.repository.InventoryRepository;
import com.comp5348.warehouse.repository.ReservationRepository;
import com.comp5348.warehouse.repository.WarehouseProductRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consumer for inventory rollback events from Store-Backend.
 *
 * This service listens to inventory.rollback.queue and performs rollback operations
 * when orders are cancelled, payments fail, or shipments are lost.
 *
 * Rollback Process:
 * 1. Find all reservations for the order
 * 2. Restore inventory quantities
 * 3. Delete reservations
 * 4. Publish product update event to sync stock with store-backend
 *
 * Error Handling:
 * - Failed rollbacks are rejected and sent to DLQ for manual inspection
 * - Idempotent: If no reservations exist, logs warning and returns
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryRollbackConsumer {

    private final ReservationRepository reservationRepository;
    private final InventoryRepository inventoryRepository;
    private final WarehouseProductRepository warehouseProductRepository;
    private final RabbitTemplate rabbitTemplate;

    /**
     * Handle inventory rollback event from store-backend.
     *
     * @param event InventoryRollbackEvent with order ID, product ID, and reason
     * @throws ListenerExecutionFailedException if rollback fails (message goes to DLQ)
     */
    @RabbitListener(queues = RabbitMQConfig.INVENTORY_ROLLBACK_QUEUE)
    @Transactional
    public void handleRollback(InventoryRollbackEvent event) {
        log.info(
            "Received inventory rollback request: orderId={}, productId={}, amount={}, reason={}",
            event.getOrderId(),
            event.getProductId(),
            event.getAmount(),
            event.getReason()
        );

        try {
            // Find all reservations for this order
            List<Reservation> reservations =
                reservationRepository.findByOrderId(event.getOrderId());

            if (reservations.isEmpty()) {
                log.warn(
                    "No reservations found for order: {} (may have been already rolled back)",
                    event.getOrderId()
                );
                return; // Idempotent - already rolled back or no reservation existed
            }

            // Rollback each reservation
            for (Reservation reservation : reservations) {
                rollbackReservation(reservation);
            }

            // Publish product update to sync stock levels with store-backend
            publishProductUpdate(event.getProductId());

            log.info(
                "Successfully completed rollback for order: {} (reason: {})",
                event.getOrderId(),
                event.getReason()
            );
        } catch (Exception e) {
            log.error(
                "Failed to rollback inventory for order {}: {}",
                event.getOrderId(),
                e.getMessage(),
                e
            );
            // Throw exception to trigger retry via RabbitMQ (or send to DLQ after max retries)
            throw new ListenerExecutionFailedException(
                "Inventory rollback failed for order: " + event.getOrderId(),
                e
            );
        }
    }

    /**
     * Rollback a single reservation by restoring inventory quantity.
     *
     * @param reservation Reservation to rollback
     */
    private void rollbackReservation(Reservation reservation) {
        // Find the inventory record using warehouse and product IDs from reservation
        Inventory inventory = inventoryRepository
            .findByWarehouseIdAndProductId(
                reservation.getWarehouseId(),
                reservation.getProductId()
            )
            .orElseThrow(() ->
                new RuntimeException(
                    "Inventory not found for warehouse " +
                        reservation.getWarehouseId() +
                        " and product " +
                        reservation.getProductId()
                )
            );

        // Restore quantity to inventory
        int restoredQuantity = reservation.getQuantity();
        inventory.setQuantity(inventory.getQuantity() + restoredQuantity);
        inventoryRepository.save(inventory);

        log.info(
            "Restored inventory: warehouseId={}, productId={}, quantity={}, newTotal={}",
            reservation.getWarehouseId(),
            reservation.getProductId(),
            restoredQuantity,
            inventory.getQuantity()
        );

        // Delete the reservation
        reservationRepository.delete(reservation);
        log.debug(
            "Deleted reservation: id={}, orderId={}",
            reservation.getId(),
            reservation.getOrderId()
        );
    }

    /**
     * Publish product update event to sync stock levels with store-backend.
     *
     * @param productId Product ID to publish update for
     */
    private void publishProductUpdate(Long productId) {
        // Calculate total stock across all warehouses
        int totalStock = inventoryRepository.sumQuantityByProductId(productId);

        // Get product details
        WarehouseProduct product = warehouseProductRepository
            .findById(productId)
            .orElse(null);

        if (product == null) {
            log.warn(
                "Product not found for stock update: productId={}",
                productId
            );
            return;
        }

        // Build product update event
        ProductUpdateEvent event = ProductUpdateEvent.builder()
            .productId(productId)
            .name(product.getName())
            .price(product.getPrice())
            .stock(totalStock)
            .published(product.isPublished())
            .imageUrl(product.getImageUrl())
            .timestamp(LocalDateTime.now())
            .build();

        // Publish to store-backend
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.TOPIC_EXCHANGE,
            RabbitMQConfig.PRODUCT_ROUTING_KEY,
            event
        );

        log.info(
            "Published product stock update: productId={}, totalStock={}",
            productId,
            totalStock
        );
    }
}
