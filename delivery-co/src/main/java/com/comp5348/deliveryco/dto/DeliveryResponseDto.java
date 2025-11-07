package com.comp5348.deliveryco.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * Response DTO sent back to Store Backend after creating a delivery
 * Matches William's expected DeliveryResponse format
 *
 * Response fields:
 * - shipmentId: String (unique identifier for this shipment)
 * - trackingNumber: String (for customer tracking)
 * - carrier: String (delivery company name)
 * - estimatedDelivery: LocalDateTime
 * - status: String (current delivery status)
 */
public class DeliveryResponseDto {

  @JsonProperty("shipmentId")
  private String shipmentId;

  @JsonProperty("trackingNumber")
  private String trackingNumber;

  @JsonProperty("carrier")
  private String carrier;

  @JsonProperty("estimatedDelivery")
  private LocalDateTime estimatedDelivery;

  @JsonProperty("status")
  private String status;

  // Constructors
  public DeliveryResponseDto() {
  }

  public DeliveryResponseDto(String shipmentId, String trackingNumber, String carrier,
                             LocalDateTime estimatedDelivery, String status) {
    this.shipmentId = shipmentId;
    this.trackingNumber = trackingNumber;
    this.carrier = carrier;
    this.estimatedDelivery = estimatedDelivery;
    this.status = status;
  }

  // Getters and Setters
  public String getShipmentId() {
    return shipmentId;
  }

  public void setShipmentId(String shipmentId) {
    this.shipmentId = shipmentId;
  }

  public String getTrackingNumber() {
    return trackingNumber;
  }

  public void setTrackingNumber(String trackingNumber) {
    this.trackingNumber = trackingNumber;
  }

  public String getCarrier() {
    return carrier;
  }

  public void setCarrier(String carrier) {
    this.carrier = carrier;
  }

  public LocalDateTime getEstimatedDelivery() {
    return estimatedDelivery;
  }

  public void setEstimatedDelivery(LocalDateTime estimatedDelivery) {
    this.estimatedDelivery = estimatedDelivery;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  @Override
  public String toString() {
    return "DeliveryResponseDto{" +
        "shipmentId='" + shipmentId + '\'' +
        ", trackingNumber='" + trackingNumber + '\'' +
        ", carrier='" + carrier + '\'' +
        ", estimatedDelivery=" + estimatedDelivery +
        ", status='" + status + '\'' +
        '}';
  }
}
