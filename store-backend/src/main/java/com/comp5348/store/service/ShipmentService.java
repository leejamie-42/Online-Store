package com.comp5348.store.service;

import com.comp5348.store.dto.delivery.*;
import com.comp5348.store.dto.event.EmailEvent;
import com.comp5348.store.dto.event.InventoryRollbackEvent;
import com.comp5348.store.exception.*;
import com.comp5348.store.grpc.warehouse.DeliveryPackage;
import com.comp5348.store.model.order.Order;
import com.comp5348.store.model.order.OrderStatus;
import com.comp5348.store.model.shipment.*;
import com.comp5348.store.repository.*;
import com.comp5348.store.service.delivery.DeliveryCoClient;
import com.comp5348.store.service.event.EventPublisher;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;
    private final DeliveryCoClient deliveryCoClient;
    private final EventPublisher eventPublisher;

    /**
     * Create shipment request to DeliveryCo after payment success.
     */
    @Transactional
    public Shipment requestShipment(
        Order order,
        List<DeliveryPackage> deliveryPackages
    ) {
        // Validate order status
        if (order.getStatus() != OrderStatus.PROCESSING) {
            throw new IllegalStateException(
                "Cannot request shipment for order not in PROCESSING status"
            );
        }

        // Check if shipment already exists
        if (shipmentRepository.existsByOrderId(order.getId())) {
            throw new IllegalStateException(
                "Shipment already exists for order " + order.getId()
            );
        }

        // Build delivery request
        String deliveryAddress = formatDeliveryAddress(order);

        DeliveryRequest request = DeliveryRequest.builder()
            .orderId("ORD-" + order.getId())
            .deliveryPackages(
                deliveryPackages
                    .stream()
                    .map(deliveryPackage ->
                        DeliveryPackageDto.builder()
                            .productId(deliveryPackage.getProductId())
                            .quantity(deliveryPackage.getQuantity())
                            .warehouseAddress(
                                deliveryPackage.getWarehouseAddress()
                            )
                            .build()
                    )
                    .collect(Collectors.toList())
            )
            .deliveryAddress(deliveryAddress)
            .recipientName(order.getFirstName() + " " + order.getLastName())
            .recipientPhone(order.getMobileNumber())
            .recipientEmail(order.getEmail())
            .packageCount(deliveryPackages.size())
            .declaredValue(order.getTotalAmount())
            .build();

        // Call DeliveryCo API
        DeliveryResponse response = deliveryCoClient.requestShipment(request);

        // Create shipment entity
        Shipment shipment = Shipment.builder()
            .order(order)
            .shipmentId(response.getShipmentId())
            .status(ShipmentStatus.SHIPMENT_CREATED)
            .carrier(response.getCarrier())
            .trackingNumber(response.getTrackingNumber())
            .estimatedDelivery(response.getEstimatedDelivery())
            .deliveryAddress(deliveryAddress)
            .build();

        shipmentRepository.save(shipment);

        log.info(
            "Created shipment {} for order {}",
            shipment.getShipmentId(),
            order.getId()
        );

        return shipment;
    }

    /**
     * Handle delivery status webhook from DeliveryCo.
     */
    @Transactional
    public void handleDeliveryWebhook(DeliveryWebhookEvent event) {
        log.info(
            "Handling delivery webhook: shipmentId={}, event={}",
            event.getShipmentId(),
            event.getEvent()
        );

        // Find shipment
        Shipment shipment = shipmentRepository
            .findByShipmentId(event.getShipmentId())
            .orElseThrow(() ->
                new ShipmentNotFoundException(
                    "Shipment not found: " + event.getShipmentId()
                )
            );

        Order order = shipment.getOrder();

        // Map webhook event to shipment status
        ShipmentStatus newStatus = mapEventToStatus(event.getEvent());
        shipment.updateStatus(newStatus);
        shipmentRepository.save(shipment);

        // Update order status
        OrderStatus newOrderStatus = mapShipmentStatusToOrderStatus(newStatus);

        if (newOrderStatus != null) {
            order.setStatus(newOrderStatus);
            orderRepository.save(order);
        }

        if (newStatus == ShipmentStatus.LOST) {
            publishInventoryRollback(order);
        }

        sendDeliveryEmail(order, shipment, event.getEvent());
    }

    private ShipmentStatus mapEventToStatus(String event) {
        return switch (event) {
            case "SHIPMENT_CREATED" -> ShipmentStatus.SHIPMENT_CREATED;
            case "PROCESSING" -> ShipmentStatus.PROCESSING;
            case "PICKED_UP" -> ShipmentStatus.PICKED_UP;
            case "IN_TRANSIT" -> ShipmentStatus.IN_TRANSIT;
            case "DELIVERED" -> ShipmentStatus.DELIVERED;
            case "LOST" -> ShipmentStatus.LOST;
            default -> throw new IllegalArgumentException(
                "Unknown delivery event: " + event
            );
        };
    }

    private OrderStatus mapShipmentStatusToOrderStatus(
        ShipmentStatus shipmentStatus
    ) {
        return switch (shipmentStatus) {
            case PICKED_UP -> OrderStatus.PICKED_UP;
            case IN_TRANSIT -> OrderStatus.DELIVERING;
            case DELIVERED -> OrderStatus.DELIVERED;
            case LOST -> OrderStatus.CANCELLED;
            default -> null; // No order status change
        };
    }

    private void publishInventoryRollback(Order order) {
        eventPublisher.publishInventoryRollbackEvent(
            InventoryRollbackEvent.builder()
                .orderId(order.getId())
                .productId(order.getProduct().getId())
                .amount(order.getQuantity())
                .reason("Shipment lost")
                .build()
        );
    }

    private void sendDeliveryEmail(
        Order order,
        Shipment shipment,
        String event
    ) {
        // Skip email for SHIPMENT_CREATED events
        if ("SHIPMENT_CREATED".equals(event)) {
            return;
        }

        // All delivery events use generic DELIVERY_UPDATE type
        // Specific status is passed in params
        EmailEvent emailEvent = EmailEvent.builder()
            .type("DELIVERY_UPDATE")
            .to(order.getEmail())
            .template("delivery_update")
            .params(
                Map.of(
                    "orderId",
                    "ORD-" + order.getId(),
                    "shipmentId",
                    shipment.getShipmentId(),
                    "trackingNumber",
                    shipment.getTrackingNumber() != null
                        ? shipment.getTrackingNumber()
                        : "N/A",
                    "customerName",
                    order.getFirstName() + " " + order.getLastName(),
                    "estimatedDelivery",
                    shipment.getEstimatedDelivery() != null
                        ? shipment.getEstimatedDelivery().toString()
                        : "TBD",
                    "status",
                    event
                )
            )
            .eventId("evt-delivery-" + shipment.getId())
            .timestamp(LocalDateTime.now())
            .build();

        eventPublisher.publishEmailEvent(emailEvent);
    }

    private String formatDeliveryAddress(Order order) {
        return String.format(
            "%s, %s %s %s",
            order.getAddressLine1(),
            order.getCity(),
            order.getState(),
            order.getPostcode()
        );
    }
}
