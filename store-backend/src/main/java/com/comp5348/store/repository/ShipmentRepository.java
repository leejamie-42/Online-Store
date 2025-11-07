package com.comp5348.store.repository;

import com.comp5348.store.model.shipment.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    Optional<Shipment> findByShipmentId(String shipmentId);
    Optional<Shipment> findByOrderId(Long orderId);
    boolean existsByOrderId(Long orderId);
}

