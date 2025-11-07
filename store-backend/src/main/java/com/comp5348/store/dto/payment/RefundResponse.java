package com.comp5348.store.dto.payment;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundResponse {
    private Long paymentId;
    private String status;
    private LocalDateTime refundedAt;
}
