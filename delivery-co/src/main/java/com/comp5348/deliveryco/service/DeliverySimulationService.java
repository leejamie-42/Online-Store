package com.comp5348.deliveryco.service;

import com.comp5348.deliveryco.dto.DeliveryWebhookEvent;
import com.comp5348.deliveryco.entity.DeliveryRequest;
import com.comp5348.deliveryco.entity.Shipment;
import com.comp5348.deliveryco.repository.DeliveryRepository;
import com.comp5348.deliveryco.repository.ShipmentRepository;
import com.comp5348.deliveryco.repository.WebhookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

// Simulates shipment progress automatically
// Runs every 5 seconds and updates each shipment's status
// Now works at shipment level, not delivery level
@Service
public class DeliverySimulationService {

  private static final Logger log = LoggerFactory.getLogger(DeliverySimulationService.class);

  private final DeliveryRepository deliveryRepository;
  private final ShipmentRepository shipmentRepository;
  private final WebhookRepository webhookRepository;
  private final RestTemplate restTemplate;
  private final Random random;

  @Value("${delivery.simulation.loss-rate-percent:5}")
  private int lossRatePercent;

  public DeliverySimulationService(DeliveryRepository deliveryRepository,
                                   ShipmentRepository shipmentRepository,
                                   WebhookRepository webhookRepository,
                                   RestTemplate restTemplate) {
    this.deliveryRepository = deliveryRepository;
    this.shipmentRepository = shipmentRepository;
    this.webhookRepository = webhookRepository;
    this.restTemplate = restTemplate;
    this.random = new Random();
  }

  // Runs every 5 seconds (configured in application.yml)
  // Processes up to 20 pending/in-transit shipments each time
  // Changed from processing deliveries to processing shipments
  @Scheduled(fixedRateString = "${delivery.simulation.interval-seconds:5}000")
  @Transactional
  public void simulateShipments() {
    // Process PENDING shipments first
    processPendingShipments();

    // Then update IN_TRANSIT shipments
    processInTransitShipments();

    // Update overall delivery status based on shipments
    updateDeliveryStatuses();
  }

  // Start shipments that are still pending
  // Each shipment has 5% chance of failure
  private void processPendingShipments() {
    List<Shipment> pending = shipmentRepository
        .findTop20ByStatusOrderByCreatedAtAsc("PENDING");

    for (Shipment shipment : pending) {
      // Simulate 5% package loss rate per shipment
      if (random.nextInt(100) < lossRatePercent) {
        shipment.setStatus("LOST");
        shipment.setProgress(0);
        shipmentRepository.save(shipment);

        // Log lost shipment with product details
        String address = shipment.getWarehouseAddress() != null ? shipment.getWarehouseAddress() : "Unknown";
        String productId = shipment.getProductId() != null ? shipment.getProductId() : "Unknown";
        Integer quantity = shipment.getQuantity() != null ? shipment.getQuantity() : 0;
        Long orderId = shipment.getDeliveryRequest().getOrderId();
        log.info("Carrier go warehouse {} to pick up product {} with quantity {} in which order ORD-{} but shipment lost",
            address, productId, quantity, orderId);

        // Notify Store via webhook
        notifyStore(shipment, "LOST");
      } else {
        // Start the shipment - picked up from warehouse
        shipment.setStatus("PICKED_UP");
        shipment.setProgress(20);  // Start at 20%
        shipmentRepository.save(shipment);

        // Log pickup with product details
        String address = shipment.getWarehouseAddress() != null ? shipment.getWarehouseAddress() : "Unknown";
        String productId = shipment.getProductId() != null ? shipment.getProductId() : "Unknown";
        Integer quantity = shipment.getQuantity() != null ? shipment.getQuantity() : 0;
        Long orderId = shipment.getDeliveryRequest().getOrderId();
        log.info("Carrier go warehouse {} to pick up product {} with quantity {} in which order ORD-{}",
            address, productId, quantity, orderId);

        // Notify Store via webhook
        notifyStore(shipment, "PICKED_UP");
      }
    }
  }

  // Update shipments that are picked up
  // Progress increases by 20% each tick until 100%
  // Status changes: PICKED_UP -> IN_TRANSIT -> DELIVERED
  private void processInTransitShipments() {
    // Get both PICKED_UP and IN_TRANSIT shipments
    List<Shipment> pickedUp = shipmentRepository
        .findTop20ByStatusOrderByCreatedAtAsc("PICKED_UP");
    List<Shipment> inTransit = shipmentRepository
        .findTop20ByStatusOrderByCreatedAtAsc("IN_TRANSIT");

    // Combine both lists
    pickedUp.addAll(inTransit);

    for (Shipment shipment : pickedUp) {
      int currentProgress = shipment.getProgress();

      if (currentProgress >= 100) {
        // Shipment delivered
        shipment.setStatus("DELIVERED");
        shipment.setProgress(100);
        shipmentRepository.save(shipment);

        // Log delivery with product details
        String address = shipment.getWarehouseAddress() != null ? shipment.getWarehouseAddress() : "Unknown";
        String productId = shipment.getProductId() != null ? shipment.getProductId() : "Unknown";
        Integer quantity = shipment.getQuantity() != null ? shipment.getQuantity() : 0;
        Long orderId = shipment.getDeliveryRequest().getOrderId();
        log.info("Carrier delivered product {} with quantity {} from warehouse {} for order ORD-{}",
            productId, quantity, address, orderId);

        // Notify Store via webhook
        notifyStore(shipment, "DELIVERED");
      } else {
        // Increase progress by 20%
        int newProgress = Math.min(currentProgress + 20, 100);
        shipment.setProgress(newProgress);

        // Change status to IN_TRANSIT after first update
        if (shipment.getStatus().equals("PICKED_UP") && newProgress > 20) {
          shipment.setStatus("IN_TRANSIT");
          shipmentRepository.save(shipment);

          // Log when shipment goes in transit with product details
          String address = shipment.getWarehouseAddress() != null ? shipment.getWarehouseAddress() : "Unknown";
          String productId = shipment.getProductId() != null ? shipment.getProductId() : "Unknown";
          Integer quantity = shipment.getQuantity() != null ? shipment.getQuantity() : 0;
          Long orderId = shipment.getDeliveryRequest().getOrderId();
          log.info("Carrier departed from warehouse {} with product {} quantity {} for order ORD-{}, now in transit",
              address, productId, quantity, orderId);

          // Notify Store when status changes to IN_TRANSIT
          notifyStore(shipment, "IN_TRANSIT");
        } else {
          shipmentRepository.save(shipment);
        }
      }
    }
  }

  // Update overall delivery status based on shipments
  // Delivery is complete only when ALL shipments are delivered
  private void updateDeliveryStatuses() {
    // Get all deliveries that have shipments
    List<DeliveryRequest> allDeliveries = deliveryRepository.findAll();

    for (DeliveryRequest delivery : allDeliveries) {
      if (delivery.getShipments() == null || delivery.getShipments().isEmpty()) {
        continue;
      }

      boolean allDelivered = delivery.getShipments().stream()
          .allMatch(s -> "DELIVERED".equals(s.getStatus()));
      boolean anyLost = delivery.getShipments().stream()
          .anyMatch(s -> "LOST".equals(s.getStatus()));
      boolean anyInProgress = delivery.getShipments().stream()
          .anyMatch(s -> "IN_TRANSIT".equals(s.getStatus()) || "PICKED_UP".equals(s.getStatus()));

      String oldStatus = delivery.getStatus();

      if (allDelivered) {
        delivery.setStatus("DELIVERED");
        delivery.setProgress(100);
        if (!"DELIVERED".equals(oldStatus)) {
          log.info("Delivery {} completed - all shipments delivered", delivery.getId());
        }
      } else if (anyLost && !anyInProgress) {
        // All shipments either lost or delivered, but at least one lost
        delivery.setStatus("LOST");
        if (!"LOST".equals(oldStatus)) {
          log.info("Delivery {} lost - some shipments lost", delivery.getId());
        }
      } else if (anyInProgress) {
        delivery.setStatus("IN_TRANSIT");
        // Calculate average progress across all shipments
        int avgProgress = (int) delivery.getShipments().stream()
            .mapToInt(Shipment::getProgress)
            .average()
            .orElse(0);
        delivery.setProgress(avgProgress);

        if (!"IN_TRANSIT".equals(oldStatus)) {
          log.info("Delivery {} started - shipments in progress", delivery.getId());
        }
      }

      deliveryRepository.save(delivery);
    }
  }

  // Notify Store Backend via webhook when shipment status changes
  // Store will handle sending emails to customers
  private void notifyStore(Shipment shipment, String event) {
    try {
      // Look up Store's webhook URL from database
      webhookRepository.findByEvent("SHIPMENT_STATUS_UPDATE")
          .ifPresent(webhook -> {
            // Build webhook payload matching Store's expected format
            DeliveryWebhookEvent payload = DeliveryWebhookEvent.builder()
                .shipmentId(String.valueOf(shipment.getId()))
                .event(event)
                .timestamp(LocalDateTime.now())
                .build();

            try {
              // Send HTTP POST to Store's webhook endpoint
              restTemplate.postForEntity(webhook.getCallbackUrl(), payload, Void.class);
              log.info("Notified Store: shipment {} status changed to {}",
                  shipment.getId(), event);
            } catch (Exception e) {
              log.error("Failed to notify Store for shipment {}: {}",
                  shipment.getId(), e.getMessage());
            }
          });
    } catch (Exception e) {
      log.error("Error looking up webhook for shipment {}: {}",
          shipment.getId(), e.getMessage());
    }
  }
}
