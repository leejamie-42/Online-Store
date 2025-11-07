package com.comp5348.store.repository;

import com.comp5348.store.model.payment.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {

    /**
     * Find refund by payment ID.
     */
    Optional<Refund> findByPaymentId(Long paymentId);

    /**
     * Check if refund exists for payment.
     */
    boolean existsByPaymentId(Long paymentId);
}
