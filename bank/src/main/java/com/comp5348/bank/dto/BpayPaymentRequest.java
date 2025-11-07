package com.comp5348.bank.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BpayPaymentRequest {
    @NotBlank
    private String referenceId; // e.g., "BP-ORD-123"
    @NotNull
    private Long customerId; // payer customer id
    @NotNull
    private Long customerAccountId; // payer account id
}


