package com.comp5348.deliveryco.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO for individual delivery packages from different warehouses
// Matches Store Backend's DeliveryPackageDto structure
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryPackageDto {

  // Warehouse address where this package should be picked up
  @JsonProperty("warehouseAddress")
  private String warehouseAddress;

  // Product ID for this package
  @JsonProperty("productId")
  private String productId;

  // Quantity of items in this package
  @JsonProperty("quantity")
  private int quantity;
}
