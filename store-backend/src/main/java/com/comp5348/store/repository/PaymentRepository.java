package com.comp5348.store.repository;

import com.comp5348.store.model.payment.Payment;
import com.comp5348.store.model.payment.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Find payment by order ID.
     */
    Optional<Payment> findByOrderId(Long orderId);

    /**
     * Find payment by Bank Service payment ID.
     */
    Optional<Payment> findByBankPaymentId(String bankPaymentId);

    /**
     * Find all payments for a user's orders.
     */
    List<Payment> findByOrderUserId(Long userId);

    /**
     * Find payments by status.
     */
    List<Payment> findByStatus(PaymentStatus status);

    /**
     * Check if payment exists for order.
     */
    boolean existsByOrderId(Long orderId);
}
