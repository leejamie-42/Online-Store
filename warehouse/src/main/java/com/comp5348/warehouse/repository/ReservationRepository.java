package com.comp5348.warehouse.repository;

import com.comp5348.warehouse.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // Find reservations by order ID
    List<Reservation> findByOrderId(long orderId);

    // Find reservations by order and product
    List<Reservation> findByOrderIdAndProductId(long orderId, Long productId);
}
