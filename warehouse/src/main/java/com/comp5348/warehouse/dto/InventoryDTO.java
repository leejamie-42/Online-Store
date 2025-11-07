package com.comp5348.warehouse.dto;

import com.comp5348.warehouse.model.Inventory;
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
public class InventoryDTO {

    private Long id;
    private Long warehouseId;
    private Long productId;
    private int quantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Entity-based constructor
     */
    public InventoryDTO(Inventory entity) {
        this(entity, false);
    }

    public InventoryDTO(Inventory entity, boolean includeRelatedEntities) {
        this.id = entity.getId();
        this.warehouseId = entity.getWarehouseId();
        this.productId = entity.getProductId();
        this.quantity = entity.getQuantity();
        this.createdAt = entity.getCreatedAt();
        this.updatedAt = entity.getUpdatedAt();
    }

}
