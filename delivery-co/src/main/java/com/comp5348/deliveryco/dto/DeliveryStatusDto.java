package com.comp5348.deliveryco.dto;

import com.comp5348.deliveryco.entity.DeliveryRequest;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

// DTO for delivery status responses
// Frontend needs shipment info to show progress for each warehouse
// Also needs customer address to display on tracking page
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeliveryStatusDto {

  public Long id;
  public Long orderId;
  public String status;
  public Integer progress;
  public String customerName;
  public String customerEmail;
  public String address;
  public List<ShipmentDto> shipments;
  public LocalDateTime createdAt;
  public LocalDateTime updatedAt;

  public DeliveryStatusDto() {
  }

  public DeliveryStatusDto(DeliveryRequest deliveryRequest) {
    this.id = deliveryRequest.getId();
    this.orderId = deliveryRequest.getOrderId();
    this.status = deliveryRequest.getStatus();
    this.progress = deliveryRequest.getProgress();
    this.customerName = deliveryRequest.getCustomerName();
    this.customerEmail = deliveryRequest.getCustomerEmail();
    this.address = deliveryRequest.getAddress();
    this.createdAt = deliveryRequest.getCreatedAt();
    this.updatedAt = deliveryRequest.getUpdatedAt();

    // Convert shipments to DTOs
    if (deliveryRequest.getShipments() != null) {
      this.shipments = deliveryRequest.getShipments().stream()
          .map(ShipmentDto::new)
          .collect(Collectors.toList());
    }
  }

}
