package com.comp5348.warehouse.dto;

import com.comp5348.warehouse.model.Warehouse;
import com.comp5348.warehouse.model.Inventory;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class WarehouseDTO {

    private Long id;
    private String name;
    private String addressLine1;
    private String addressLine2;
    private String country;
    private String city;
    private String suburb;
    private String postcode;

    private Set<InventoryDTO> inventories = new HashSet<>();

    public WarehouseDTO(Warehouse entity) {
        this(entity, false);
    }

    public WarehouseDTO(Warehouse entity, boolean includeRelatedEntities) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.addressLine1 = entity.getAddressLine1();
        this.addressLine2 = entity.getAddressLine2();
        this.country = entity.getCountry();
        this.city = entity.getCity();
        this.suburb = entity.getSuburb();
        this.postcode = entity.getPostcode();
    }
}
