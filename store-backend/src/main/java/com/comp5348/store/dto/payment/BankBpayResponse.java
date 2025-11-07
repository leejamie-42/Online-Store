package com.comp5348.store.dto.payment;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response from Bank Service for BPAY generation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankBpayResponse {
    private String billerCode;
    private String referenceNumber;
    private BigDecimal amount;
    private LocalDateTime expiresAt;
}
