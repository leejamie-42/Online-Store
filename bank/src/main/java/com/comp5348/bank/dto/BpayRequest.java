package com.comp5348.bank.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BpayRequest {
    @NotNull
    private Long accountId; // Merchant's bank account ID; bank resolves billerCode
    @NotBlank
    private String orderId;
    @Positive
    private BigDecimal amount;
}
