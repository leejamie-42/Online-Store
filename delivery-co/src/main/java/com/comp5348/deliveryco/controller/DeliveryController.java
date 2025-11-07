package com.comp5348.deliveryco.controller;

import com.comp5348.deliveryco.dto.DeliveryRequestDto;
import com.comp5348.deliveryco.dto.DeliveryResponseDto;
import com.comp5348.deliveryco.dto.DeliveryStatusDto;
import com.comp5348.deliveryco.service.DeliveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// REST API for deliveries
// Store backend calls us when payment is done
// Frontend calls us to check delivery progress
// Updated to match William's Store Backend expectations:
// - Endpoint: POST /deliveryCo/api/shipments (was /api/deliveries)
// - Response: DeliveryResponseDto with shipmentId, trackingNumber, carrier, etc.
@RestController
@CrossOrigin(origins = "*")
public class DeliveryController {

  private static final Logger log = LoggerFactory.getLogger(DeliveryController.class);

  private final DeliveryService deliveryService;

  public DeliveryController(DeliveryService deliveryService) {
    this.deliveryService = deliveryService;
  }

  // Create new delivery - UPDATED endpoint path to match Store Backend
  // Store calls this after payment goes through
  // POST /deliveryCo/api/shipments
  @PostMapping("/deliveryCo/api/shipments")
  public ResponseEntity<DeliveryResponseDto> createDelivery(@RequestBody DeliveryRequestDto request) {
    log.info("Creating delivery for order {}", request.orderId);

    try {
      DeliveryResponseDto created = deliveryService.createDelivery(request);
      log.info("Delivery created for order {}, shipmentId: {}", request.orderId, created.getShipmentId());
      return ResponseEntity.status(HttpStatus.CREATED).body(created);
    } catch (Exception e) {
      log.error("Failed to create delivery for order {}: {}", request.orderId, e.getMessage());
      throw e;
    }
  }

  // Get delivery status - Frontend endpoint (Sue's OrderTracking page)
  // Frontend uses this to show tracking page
  // Keep this endpoint for Sue's OrderTracking.tsx
  @GetMapping("/api/deliveries/{orderId}/status")
  public ResponseEntity<DeliveryStatusDto> getDeliveryStatus(@PathVariable Long orderId) {
    log.debug("Getting delivery status for order {}", orderId);

    try {
      DeliveryStatusDto status = deliveryService.getDeliveryStatus(orderId);
      return ResponseEntity.ok(status);
    } catch (RuntimeException e) {
      log.error("Delivery not found for order {}", orderId);
      return ResponseEntity.notFound().build();
    }
  }

  // Quick health check
  @GetMapping("/deliveryCo/api/health")
  public ResponseEntity<String> health() {
    return ResponseEntity.ok("DeliveryCo service is running");
  }
}
