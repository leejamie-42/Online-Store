package com.comp5348.store.util;

import com.comp5348.store.dto.payment.*;
import com.comp5348.store.model.payment.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class PaymentMapper {

    public CreatePaymentResponse toCreateResponse(Payment payment) {
        return CreatePaymentResponse.builder()
            .paymentId(payment.getId())
            .status(payment.getStatus().name().toLowerCase())
            .build();
    }

    public BpayInfoResponse toBpayInfo(BankBpayResponse bankResponse) {
        return BpayInfoResponse.builder()
            .billerCode(bankResponse.getBillerCode())
            .referenceNumber(bankResponse.getReferenceNumber())
            .amount(bankResponse.getAmount())
            .expiresAt(bankResponse.getExpiresAt())
            .build();
    }

    public RefundResponse toRefundResponse(Refund refund) {
        return RefundResponse.builder()
            .paymentId(refund.getPayment().getId())
            .status(refund.getStatus().name().toLowerCase())
            .refundedAt(refund.getRefundedAt())
            .build();
    }
}
