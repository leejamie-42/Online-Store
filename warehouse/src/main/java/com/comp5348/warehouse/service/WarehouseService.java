package com.comp5348.warehouse.service;

import com.comp5348.warehouse.dto.InventoryDTO;
import com.comp5348.warehouse.dto.WarehouseDTO;
import com.comp5348.warehouse.dto.WarehouseProductDTO;
import com.comp5348.warehouse.model.Inventory;
import com.comp5348.warehouse.model.Warehouse;
import com.comp5348.warehouse.model.WarehouseProduct;
import com.comp5348.warehouse.repository.InventoryRepository;
import com.comp5348.warehouse.repository.WarehouseProductRepository;
import com.comp5348.warehouse.repository.WarehouseRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Service layer for Warehouse operations.
 * Handles business logic for stock management and warehouse lookups.
 */
@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final WarehouseProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    /**
     * Returns all warehouse products with aggregated stock quantities.
     */
    public List<WarehouseProductDTO> getAllProductsWithStock() {
        List<WarehouseProduct> products = productRepository.findAll();
        return products.stream()
                .map(product -> {
                    WarehouseProductDTO dto = new WarehouseProductDTO(product, false);

                    int totalStock = inventoryRepository.sumQuantityByProductId(product.getId());
                    dto.setTotalStock(totalStock);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a warehouse and its inventory list.
     */
    public WarehouseDTO getWarehouseById(Long id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Warehouse not found"));

        WarehouseDTO dto = new WarehouseDTO(warehouse, false);

        List<Inventory> inventories = inventoryRepository.findByWarehouseId(id);
        for (Inventory inventory : inventories) {
            dto.getInventories().add(new InventoryDTO(inventory));
        }

        return dto;
    }

    /**
     * Checks if any warehouse(s) can fulfill the requested quantity for a product.
     */
    public boolean isStockAvailable(Long productId, int requiredQuantity) {
        List<Inventory> inventories = inventoryRepository.findByProductId(productId);
        if (inventories.isEmpty()) {
            throw new NoSuchElementException("Product not found");
        }

        return inventories.stream()
                .anyMatch(i -> i.getQuantity() >= requiredQuantity);
    }

    /**
     * Updates (reserves or releases) stock for a given warehouse/product combination.
     * Used during order creation, cancellation, and refund.
     */
    @Transactional
    public InventoryDTO updateStock(InventoryDTO dto) {
        Warehouse warehouse = warehouseRepository.findById(dto.getWarehouseId())
                .orElseThrow(() -> new NoSuchElementException("Warehouse not found"));
        WarehouseProduct product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new NoSuchElementException("Product not found"));

        Inventory inventory = inventoryRepository.findByWarehouseIdAndProductId(
                        dto.getWarehouseId(), dto.getProductId())
                .orElseThrow(() -> new NoSuchElementException("Inventory record not found"));

        if (dto.getQuantity() < 0 && Math.abs(dto.getQuantity()) > inventory.getQuantity()) {
            throw new IllegalArgumentException("Not enough stock available");
        }

        inventory.setQuantity(inventory.getQuantity() + dto.getQuantity());
        inventoryRepository.save(inventory);

        return new InventoryDTO(inventory);
    }
}
