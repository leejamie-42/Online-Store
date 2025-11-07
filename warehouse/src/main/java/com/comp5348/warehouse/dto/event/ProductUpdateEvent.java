package com.comp5348.warehouse.dto.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

/**
 * Product update event published to store-backend.
 *
 * This event is published when product data changes in the warehouse
 * (stock levels, prices, availability). The Store Backend subscribes to these
 * events to keep its product catalog synchronized with the warehouse (source of truth).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
     * Product price (using BigDecimal for precision).
     */
    private Double price;

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
