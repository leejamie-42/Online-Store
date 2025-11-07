package com.comp5348.store.dto.payment;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentResponse {
    private Long paymentId;
    private String status; // "pending"
}
