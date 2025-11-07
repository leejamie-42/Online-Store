package com.comp5348.bank.repository;

import com.comp5348.bank.model.TransactionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRecordRepository extends JpaRepository<TransactionRecord, Long> {
    List<TransactionRecord> findByToAccountIdOrFromAccountId(Long toAccountId, Long fromAccountId);
}
