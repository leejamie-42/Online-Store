package com.comp5348.warehouse.repository;

import com.comp5348.warehouse.model.Inventory;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    // Find all inventories by warehouse ID
    List<Inventory> findByWarehouseId(long warehouseId);

    // Find all inventories by product ID
    List<Inventory> findByProductId(long productId);

    // Find specific inventory record by warehouse and product
    Optional<Inventory> findByWarehouseIdAndProductId(
        long warehouseId,
        Long productId
    );

    // Get total quantity of a product across all warehouses
    @Query(
        "SELECT COALESCE(SUM(i.quantity), 0) FROM Inventory i WHERE i.productId = :productId"
    )
    int sumQuantityByProductId(long productId);

    // ================================
    // Pessimistic Locking
    // Locks the inventory rows for writing to prevent concurrent updates.
    // Use this method in reserveStock() to prevent over-reservation under concurrent requests.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        "SELECT i FROM Inventory i WHERE i.productId = :productId ORDER BY i.warehouseId"
    )
    List<Inventory> findByProductIdOrderByWarehouseIdPessimistic(
        long productId
    );

    // ================================
    // Optimistic Locking
    // Requires @Version field in Inventory entity.
    // When saving, JPA checks the version to prevent lost updates in concurrent transactions.
    // Use this if you want to detect conflicts and retry rather than block.
    @Query(
        "SELECT i FROM Inventory i WHERE i.productId = :productId ORDER BY i.warehouseId"
    )
    List<Inventory> findByProductIdOrderByWarehouseIdOptimistic(long productId);
}
