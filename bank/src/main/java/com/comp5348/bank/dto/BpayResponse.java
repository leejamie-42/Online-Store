package com.comp5348.bank.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class BpayResponse {
    private String billerCode;
    private String referenceNumber;
    private BigDecimal amount;
    private LocalDateTime expiresAt;
}
