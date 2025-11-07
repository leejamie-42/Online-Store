package com.comp5348.store.dto.delivery;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryPackageDto {
  private String warehouseAddress;
  private String productId;
  private int quantity;
}
