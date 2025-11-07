package com.comp5348.warehouse.dto;

import com.comp5348.warehouse.model.WarehouseProduct;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class WarehouseProductDTO {

    private Long id;
    private String name;
    private String description;
    private double price;
    private String imageUrl;
    private boolean published;

    // get total stock for a product given an ID
    private Integer totalStock;

    /**
     * Entity-based constructor.
     */
    public WarehouseProductDTO(WarehouseProduct entity) {
        this(entity, false);
    }

    public WarehouseProductDTO(
        WarehouseProduct entity,
        boolean includeRelatedEntities
    ) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.description = entity.getDescription();
        this.price = entity.getPrice();
        this.imageUrl = entity.getImageUrl();
        this.published = entity.isPublished();
    }
}
