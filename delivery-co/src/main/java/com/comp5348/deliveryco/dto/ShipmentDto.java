package com.comp5348.deliveryco.dto;

import com.comp5348.deliveryco.entity.Shipment;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

// DTO for shipment info
// Shows which warehouse the pickup is from and its status
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShipmentDto {

  public Long id;
  public Long deliveryRequestId;
  public Long warehouseId;
  public String status;
  public Integer progress;
  public LocalDateTime createdAt;
  public LocalDateTime updatedAt;

  public ShipmentDto() {
  }

  public ShipmentDto(Shipment shipment) {
    this.id = shipment.getId();
    this.deliveryRequestId = shipment.getDeliveryRequest() != null
        ? shipment.getDeliveryRequest().getId()
        : null;
    this.warehouseId = shipment.getWarehouseId();
    this.status = shipment.getStatus();
    this.progress = shipment.getProgress();
    this.createdAt = shipment.getCreatedAt();
    this.updatedAt = shipment.getUpdatedAt();
  }

}
