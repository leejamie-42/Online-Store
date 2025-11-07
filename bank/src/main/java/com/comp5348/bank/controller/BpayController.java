package com.comp5348.bank.controller;

import com.comp5348.bank.dto.BpayPaymentRequest;
import com.comp5348.bank.dto.BpayRefundRequest;
import com.comp5348.bank.dto.BpayRefundResponse;
import com.comp5348.bank.dto.BpayRequest;
import com.comp5348.bank.dto.BpayResponse;
import com.comp5348.bank.service.BpayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bank/api/bpay")
@RequiredArgsConstructor
@Slf4j
public class BpayController {

    private final BpayService bpayService;

    /**
     * Generate BPAY payment instructions
     * POST /bank/api/bpay
     */
    @PostMapping
    public ResponseEntity<BpayResponse> createBpayPayment(
        @Valid @RequestBody BpayRequest request
    ) {
        log.info(
            "Creating BPAY payment for accountId={}, orderId={}",
            request.getAccountId(),
            request.getOrderId()
        );
        BpayResponse response = bpayService.createBpayPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Process BPAY payment (triggered by store frontend simulating bank payment)
     * POST /bank/api/bpay/pay
     */
    @PostMapping("/pay")
    public ResponseEntity<Void> processBpayPayment(
        @Valid @RequestBody BpayPaymentRequest request
    ) {
        log.info(
            "Processing BPAY payment via API for reference: {}",
            request.getReferenceId()
        );
        bpayService.processBpayPayment(
            request.getReferenceId(),
            request.getCustomerId(),
            request.getCustomerAccountId()
        );
        return ResponseEntity.noContent().build();
    }

    /**
     * Request refund for a paid BPAY transaction
     * POST /bank/api/bpay/refund
     */
    @PostMapping("/refund")
    public ResponseEntity<BpayRefundResponse> requestRefund(
        @Valid @RequestBody BpayRefundRequest request
    ) {
        log.info(
            "Requesting refund for BPAY reference: {}",
            request.getReferenceId()
        );
        BpayRefundResponse response = bpayService.requestRefund(
            request.getReferenceId()
        );
        return ResponseEntity.ok(response);
    }
}
