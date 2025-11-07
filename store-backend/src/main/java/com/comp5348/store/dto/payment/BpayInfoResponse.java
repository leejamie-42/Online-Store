package com.comp5348.store.dto.payment;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for BPAY payment information.
 *
 * Example:
 * {
 *   "biller_code": "93242",
 *   "reference_number": "BP-ORD-001",
 *   "amount": 149.97,
 *   "expires_at": "2025-10-20T12:00:00"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BpayInfoResponse {
    private String billerCode;
    private String referenceNumber;
    private BigDecimal amount;
    private LocalDateTime expiresAt;
}
