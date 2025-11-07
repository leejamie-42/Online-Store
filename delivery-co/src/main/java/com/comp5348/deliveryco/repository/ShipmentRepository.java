package com.comp5348.deliveryco.repository;

import com.comp5348.deliveryco.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// Repo for shipments
// Lets us query shipments by status, warehouse etc
@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

  // Find all shipments for a delivery request
  // Useful when we want to see all warehouse pickups for one order
  List<Shipment> findByDeliveryRequestId(Long deliveryRequestId);

  // Find shipments by status
  // Used by simulation service to update pending/in-transit shipments
  // Limited to 20 so we don't overload memory
  List<Shipment> findTop20ByStatusOrderByCreatedAtAsc(String status);

  // Find shipments for a specific warehouse
  // Could be useful for warehouse dashboard showing their pickups
  List<Shipment> findByWarehouseId(Long warehouseId);

}
