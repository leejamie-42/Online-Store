package com.comp5348.bank.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BpayRefundRequest {

    @NotBlank(message = "Reference ID is required")
    private String referenceId;
}
