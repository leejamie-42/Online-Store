package com.comp5348.warehouse.dto;

import com.comp5348.warehouse.model.Reservation;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ReservationDTO {

    private Long id;
    private Long orderId;
    private Long productId;
    private Long warehouseId;
    private Integer quantity;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Entity-based constructor
     */
    public ReservationDTO(Reservation entity) {
        this(entity, false);
    }

    public ReservationDTO(Reservation entity, boolean includeRelatedEntities) {
        this.id = entity.getId();
        this.orderId = entity.getOrderId();
        this.productId = entity.getProductId();
        this.warehouseId = entity.getWarehouseId();
        this.quantity = entity.getQuantity();
        this.status = entity.getStatus().name();
        this.createdAt = entity.getCreatedAt();
        this.updatedAt = entity.getUpdatedAt();
    }
}