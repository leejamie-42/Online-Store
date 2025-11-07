package com.comp5348.store.repository;

import com.comp5348.store.model.payment.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {

    /**
     * Find payment method by type.
     */
    Optional<PaymentMethod> findByType(String type);
}
