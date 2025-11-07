package com.comp5348.store.dto.payment;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequest {

    @NotBlank(message = "Refund for order id is required")
    private Long orderId;

    @NotBlank(message = "Refund reason is required")
    private String reason;
}
