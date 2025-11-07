package com.comp5348.deliveryco.repository;

import com.comp5348.deliveryco.entity.DeliveryRequest;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// Repo for delivery requests
// Spring JPA creates these methods automatically from the method names
@Repository
public interface DeliveryRepository extends JpaRepository<DeliveryRequest, Long> {

  // Find delivery by order ID - useful for checking if delivery exists
  // or updating status
  Optional<DeliveryRequest> findByOrderId(Long orderId);

  // Find delivery with shipments loaded - used by status API
  // @EntityGraph loads shipments in same query to avoid lazy loading issues
  @EntityGraph(attributePaths = {"shipments"})
  Optional<DeliveryRequest> findWithShipmentsByOrderId(Long orderId);

  // Gets the oldest 20 deliveries with a specific status
  // Used by simulation service to batch process deliveries
  // Limited to 20 so we don't load too much into memory at once
  List<DeliveryRequest> findTop20ByStatusOrderByCreatedAtAsc(String status);

}
