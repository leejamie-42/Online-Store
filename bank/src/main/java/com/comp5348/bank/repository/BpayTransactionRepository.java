package com.comp5348.bank.repository;

import com.comp5348.bank.enums.BpayStatus;
import com.comp5348.bank.model.BpayTransactionInformation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BpayTransactionRepository extends JpaRepository<BpayTransactionInformation, Long> {
    Optional<BpayTransactionInformation> findByReferenceId(String referenceId);

    List<BpayTransactionInformation> findByStatus(BpayStatus status);
}
