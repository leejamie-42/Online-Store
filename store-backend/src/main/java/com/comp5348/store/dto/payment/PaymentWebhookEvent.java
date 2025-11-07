package com.comp5348.store.dto.payment;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Webhook event from Bank Service.
 *
 * Example:
 * {
 *   "type": "BPAY_PAYMENT_COMPLETED",
 *   "order_id": "ORD-001",
 *   "payment_id": "PAY-123",
 *   "amount": 149.97,
 *   "paid_at": "2025-10-21T11:00:00Z"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentWebhookEvent {
    private String type; // "BPAY_PAYMENT_COMPLETED", "REFUND_COMPLETED"
    private String orderId;
    private String paymentId;
    private BigDecimal amount;
    private LocalDateTime paidAt;
}
