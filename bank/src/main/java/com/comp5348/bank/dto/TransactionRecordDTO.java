package com.comp5348.bank.dto;

import com.comp5348.bank.model.TransactionRecord;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for TransactionRecord.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionRecordDTO {
    private Long id;
    private BigDecimal amount;
    private String memo;
    private Long toAccountId;
    private Long fromAccountId;
    private String status;
    private LocalDateTime createdAt;

    public TransactionRecordDTO(TransactionRecord transaction) {
        this.id = transaction.getId();
        this.amount = transaction.getAmount();
        this.memo = transaction.getMemo();
        this.toAccountId = transaction.getToAccount() != null ? transaction.getToAccount().getId() : null;
        this.fromAccountId = transaction.getFromAccount() != null ? transaction.getFromAccount().getId() : null;
        this.status = transaction.getStatus().name();
        this.createdAt = transaction.getCreatedAt();
    }
}
