package com.comp5348.deliveryco.dto;

import com.comp5348.deliveryco.entity.DeliveryRequest;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO for delivery requests
 * Updated to support multiple warehouse pickups
 *
 * Incoming format (from Store):
 * - orderId: String (e.g., "ORD-123")
 * - deliveryPackages: List<DeliveryPackageDto> (multiple warehouses supported)
 * - deliveryAddress: String
 * - recipientName: String
 * - recipientPhone: String
 * - recipientEmail: String
 * - packageCount: Integer
 * - declaredValue: BigDecimal
 *
 * Response format (to Store):
 * - shipmentId: String (generated)
 * - trackingNumber: String
 * - carrier: String
 * - estimatedDelivery: LocalDateTime
 * - status: String
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeliveryRequestDto {

  // === Incoming Request Fields (from Store) ===

  @JsonProperty("orderId")
  public String orderId;  // String format: "ORD-123"

  // List of packages from different warehouses (supports multiple warehouse pickups)
  @JsonProperty("deliveryPackages")
  public List<DeliveryPackageDto> deliveryPackages;

  @JsonProperty("deliveryAddress")
  public String deliveryAddress;

  @JsonProperty("recipientName")
  public String recipientName;

  @JsonProperty("recipientPhone")
  public String recipientPhone;

  @JsonProperty("recipientEmail")
  public String recipientEmail;

  @JsonProperty("packageCount")
  public Integer packageCount;

  @JsonProperty("declaredValue")
  public BigDecimal declaredValue;

  // === Internal Fields (for response/status) ===

  public String status;
  public Integer progress;
  public List<Long> warehouseIds;  // Extracted from deliveryPackages
  public List<ShipmentDto> shipments;

  // Constructors
  public DeliveryRequestDto() {
  }

  /**
   * Constructor from entity (for responses)
   */
  public DeliveryRequestDto(DeliveryRequest deliveryRequest) {
    // Convert Long orderId back to String format
    this.orderId = "ORD-" + deliveryRequest.getOrderId();
    this.recipientName = deliveryRequest.getCustomerName();
    this.recipientEmail = deliveryRequest.getCustomerEmail();
    this.deliveryAddress = deliveryRequest.getAddress();
    this.status = deliveryRequest.getStatus();
    this.progress = deliveryRequest.getProgress();

    // Include shipment info if available
    if (deliveryRequest.getShipments() != null && !deliveryRequest.getShipments().isEmpty()) {
      this.shipments = deliveryRequest.getShipments().stream()
          .map(ShipmentDto::new)
          .collect(Collectors.toList());

      this.warehouseIds = deliveryRequest.getShipments().stream()
          .map(s -> s.getWarehouseId())
          .collect(Collectors.toList());
    }
  }

  /**
   * Parse orderId from String format "ORD-123" to Long 123
   */
  public Long parseOrderId() {
    if (orderId == null) {
      throw new IllegalArgumentException("Order ID is required");
    }
    // Handle both "ORD-123" and "123" formats
    String numericPart = orderId.replaceAll("[^0-9]", "");
    if (numericPart.isEmpty()) {
      throw new IllegalArgumentException("Invalid order ID format: " + orderId);
    }
    return Long.parseLong(numericPart);
  }
}
