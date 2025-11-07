package com.comp5348.bank.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebhookRegistrationRequest {
    @NotBlank
    private String event; // "PAYMENT_EVENT"
    @NotBlank
    private String callbackUrl; // e.g., "http://localhost:8081/api/webhooks/payment"
}
