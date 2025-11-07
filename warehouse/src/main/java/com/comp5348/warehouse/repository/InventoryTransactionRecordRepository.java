package com.comp5348.warehouse.repository;

import com.comp5348.warehouse.model.InventoryTransactionRecord;
import com.comp5348.warehouse.model.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryTransactionRecordRepository extends JpaRepository<InventoryTransactionRecord, Long> {
    List<InventoryTransactionRecord> findByWarehouseId(long warehouseId);
    List<InventoryTransactionRecord> findByOrderId(long orderId);
}
