package com.comp5348.bank.dto;

import com.comp5348.bank.enums.BpayStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BpayRefundResponse {

    private String referenceId;
    private Long refundTransactionId;
    private BigDecimal amount;
    private BpayStatus status;
    private LocalDateTime refundedAt;
}
