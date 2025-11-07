package com.comp5348.deliveryco.service;

import com.comp5348.deliveryco.common.repository.ProcessedMessageRepository;
import com.comp5348.deliveryco.dto.DeliveryRequestDto;
import com.comp5348.deliveryco.dto.DeliveryResponseDto;
import com.comp5348.deliveryco.dto.DeliveryStatusDto;
import com.comp5348.deliveryco.entity.DeliveryRequest;
import com.comp5348.deliveryco.entity.Shipment;
import com.comp5348.deliveryco.repository.DeliveryRepository;
import com.comp5348.deliveryco.repository.ShipmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

// Main service for handling deliveries
// Create new deliveries, update status, check progress
// Updated to match William's Store Backend interface
@Service
public class DeliveryService {

  private static final Logger log = LoggerFactory.getLogger(DeliveryService.class);

  private final DeliveryRepository deliveryRepository;
  private final ShipmentRepository shipmentRepository;
  private final ProcessedMessageRepository processedMessageRepository;

  public DeliveryService(DeliveryRepository deliveryRepository,
                         ShipmentRepository shipmentRepository,
                         ProcessedMessageRepository processedMessageRepository) {
    this.deliveryRepository = deliveryRepository;
    this.shipmentRepository = shipmentRepository;
    this.processedMessageRepository = processedMessageRepository;
  }

  /**
   * Create a new delivery request
   * Updated to support multiple warehouse pickups
   *
   * Incoming format:
   * - orderId: String ("ORD-123")
   * - deliveryPackages: List<DeliveryPackageDto> (multiple warehouses)
   * - deliveryAddress: String
   * - recipientName, recipientPhone, recipientEmail
   * - packageCount, declaredValue
   *
   * Returns: DeliveryResponseDto with shipmentId, trackingNumber, carrier, etc.
   */
  @Transactional
  public DeliveryResponseDto createDelivery(DeliveryRequestDto dto) {
    // Parse orderId from "ORD-123" to Long 123
    Long orderIdLong = dto.parseOrderId();

    // Check if we already have a delivery for this order
    Optional<DeliveryRequest> existing = deliveryRepository.findByOrderId(orderIdLong);
    if (existing.isPresent()) {
      log.info("Delivery already exists for order {}", dto.orderId);
      DeliveryRequest existingDelivery = existing.get();
      return buildDeliveryResponse(existingDelivery);
    }

    // Create new delivery with PENDING status
    DeliveryRequest delivery = new DeliveryRequest(
        orderIdLong,
        dto.recipientName,
        dto.recipientEmail,
        dto.deliveryAddress
    );

    // Create one shipment per deliveryPackage
    // Each package represents one product from one warehouse
    if (dto.deliveryPackages != null && !dto.deliveryPackages.isEmpty()) {
      log.info("Creating {} shipments for order {}", dto.deliveryPackages.size(), dto.orderId);

      // Track warehouse addresses to assign unique IDs
      var warehouseMap = new java.util.HashMap<String, Long>();
      int warehouseCounter = 1;

      for (var pkg : dto.deliveryPackages) {
        String address = pkg.getWarehouseAddress();

        // Assign warehouse ID if we haven't seen this address before
        if (!warehouseMap.containsKey(address)) {
          warehouseMap.put(address, (long) warehouseCounter++);
        }

        Long warehouseId = warehouseMap.get(address);

        // Create one shipment per package (one product per shipment)
        Shipment shipment = new Shipment(delivery, warehouseId, address,
            pkg.getProductId(), pkg.getQuantity());
        delivery.addShipment(shipment);
      }
    } else {
      // Fallback: create default shipment if no packages provided
      log.info("No packages found, creating default shipment for order {}", dto.orderId);
      Shipment defaultShipment = new Shipment(delivery, 1L, "Default Warehouse");
      delivery.addShipment(defaultShipment);
    }

    DeliveryRequest saved = deliveryRepository.save(delivery);
    log.info("Delivery created for order {} with {} shipments", saved.getOrderId(), saved.getShipments().size());

    return buildDeliveryResponse(saved);
  }

  /**
   * Build DeliveryResponseDto for Store Backend
   * Includes shipmentId, trackingNumber, carrier, estimatedDelivery
   */
  private DeliveryResponseDto buildDeliveryResponse(DeliveryRequest delivery) {
    // Generate shipmentId (composite of all shipment IDs)
    String shipmentId = "SHIP-" + delivery.getOrderId();
    if (!delivery.getShipments().isEmpty()) {
      shipmentId = "SHIP-" + delivery.getShipments().get(0).getId();
    }

    // Generate tracking number
    String trackingNumber = "TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

    // Carrier name
    String carrier = "DeliveryCo Express";

    // Estimated delivery (3-5 business days from now)
    LocalDateTime estimatedDelivery = LocalDateTime.now().plusDays(4);

    // Status
    String status = delivery.getStatus();

    return new DeliveryResponseDto(
        shipmentId,
        trackingNumber,
        carrier,
        estimatedDelivery,
        status
    );
  }


  // Get delivery status by order ID
  // Frontend uses this to show progress
  // Loads shipments too so we can show individual warehouse progress
  public DeliveryStatusDto getDeliveryStatus(Long orderId) {
    DeliveryRequest delivery = deliveryRepository.findWithShipmentsByOrderId(orderId)
        .orElseThrow(() -> new RuntimeException("Delivery not found for order: " + orderId));

    return new DeliveryStatusDto(delivery);
  }

  // Update delivery progress
  // Called by simulation service every 5 seconds
  @Transactional
  public void updateDeliveryProgress(Long deliveryId, String status, Integer progress) {
    DeliveryRequest delivery = deliveryRepository.findById(deliveryId)
        .orElseThrow(() -> new RuntimeException("Delivery not found: " + deliveryId));

    delivery.setStatus(status);
    delivery.setProgress(progress);
    deliveryRepository.save(delivery);
  }
}
