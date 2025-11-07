package com.comp5348.warehouse.dto;

import com.comp5348.warehouse.model.InventoryTransactionRecord;
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
public class InventoryTransactionRecordDTO {

    private Long id;
    private Long warehouseId;
    private Long orderId;
    private int amount;
    private LocalDateTime createdAt;

    public InventoryTransactionRecordDTO(InventoryTransactionRecord entity) {
        this(entity, false);
    }

    public InventoryTransactionRecordDTO(InventoryTransactionRecord entity, boolean includeRelatedEntities) {
        this.id = entity.getId();
        this.amount = entity.getAmount();
        this.orderId = entity.getOrderId();
        this.createdAt = entity.getCreatedAt();
        this.warehouseId = entity.getWarehouseId();
    }
}