package com.comp5348.store.dto.payment;

import lombok.*;
import java.math.BigDecimal;

/**
 * Request to Bank Service for BPAY generation.
 * POST /bank/api/bpay
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankBpayRequest {
    private Long accountId;    // Store's bank account
    private String orderId;
    private BigDecimal amount;
}
