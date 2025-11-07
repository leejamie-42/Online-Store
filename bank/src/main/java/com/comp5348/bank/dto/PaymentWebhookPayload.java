package com.comp5348.bank.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentWebhookPayload {

    private String type; // "BPAY_PAYMENT_COMPLETED" or "REFUND_COMPLETED"
    private String orderId;
    private String paymentId; // BPAY reference
    private Long refundId; // Transaction record ID for refund
    private BigDecimal amount;
    private LocalDateTime paidAt;
    private LocalDateTime refundedAt;
}
