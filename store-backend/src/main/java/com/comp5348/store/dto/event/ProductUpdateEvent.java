package com.comp5348.store.dto.event;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Product update event from Warehouse service.
 *
 * This event is published by the Warehouse service when product data changes
 * (stock levels, prices, availability). The Store Backend subscribes to these
 * events to keep its product catalog synchronized with the source of truth.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateEvent {
  /**
   * Product ID (matches Store Backend product ID).
   */
  private Long productId;

  /**
   * Product name.
   */
  private String name;

  /**
   * Product price.
   */
  private BigDecimal price;

  /**
   * Current stock level across all warehouses.
   */
  private Integer stock;

  /**
   * Whether product is published/visible.
   */
  private Boolean published;

  /**
   * Product image URL.
   */
  private String imageUrl;

  /**
   * Event timestamp.
   */
  private LocalDateTime timestamp;
}
