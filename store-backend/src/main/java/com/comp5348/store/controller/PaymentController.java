package com.comp5348.store.controller;

import com.comp5348.store.dto.payment.*;
import com.comp5348.store.model.auth.User;
import com.comp5348.store.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Payment", description = "Payment management operations")
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Create payment for an order.
     *
     * POST /api/payments
     *
     * Request:
     * {
     *   "order_id": 1,
     *   "method": "BPAY"
     * }
     *
     * Response: 201 Created
     * {
     *   "payment_id": 1,
     *   "status": "pending"
     * }
     */
    @Operation(
        summary = "Create payment for an order",
        description = "Generate BPAY payment instructions for an order. Requires authentication.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "201",
                description = "Payment created successfully"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid request or unsupported payment method"
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Unauthorized - missing or invalid JWT token"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - user doesn't own the order"
            ),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(
                responseCode = "502",
                description = "Bank Service unavailable"
            ),
        }
    )
    @PostMapping
    public ResponseEntity<CreatePaymentResponse> createPayment(
        @Valid @RequestBody CreatePaymentRequest request,
        @AuthenticationPrincipal User authenticatedUser
    ) {
        log.info(
            "POST /api/payments - Create payment for order {} with method {}",
            request.getOrderId(),
            request.getMethod()
        );

        CreatePaymentResponse response = paymentService.createPayment(
            request,
            authenticatedUser.getId()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get BPAY payment information.
     *
     * GET /api/payments/{id}
     *
     * Response: 200 OK
     * {
     *   "biller_code": "93242",
     *   "reference_number": "BP-ORD-001",
     *   "amount": 149.97,
     *   "expires_at": "2025-10-20T12:00:00"
     * }
     */
    @Operation(
        summary = "Get BPAY payment information",
        description = "Retrieve BPAY payment details for a payment. Requires authentication.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "BPAY info retrieved successfully"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Payment is not BPAY type"
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Unauthorized - missing or invalid JWT token"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - user doesn't own the payment"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Payment not found"
            ),
        }
    )
    @GetMapping("/{id}")
    public ResponseEntity<BpayInfoResponse> getBpayInfo(
        @PathVariable Long id,
        @AuthenticationPrincipal User authenticatedUser
    ) {
        log.info(
            "GET /api/payments/{} - Get BPAY info for user {}",
            id,
            authenticatedUser.getId()
        );

        BpayInfoResponse response = paymentService.getBpayInfo(
            id,
            authenticatedUser.getId()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Request refund for a payment.
     *
     * POST /api/payments/refund
     *
     * Request:
     * {
     *
     *   "orderId": "Order id"
     *   "reason": "Order cancelled by customer"
     * }
     *
     * Response: 200 OK
     * {
     *   "payment_id": 1,
     *   "status": "processing",
     *   "refunded_at": null
     * }
     */
    @Operation(
        summary = "Request payement refund for a order",
        description = "Request refund for a completed payment. Requires authentication.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Refund requested successfully"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid payment status or duplicate refund"
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Unauthorized - missing or invalid JWT token"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - user doesn't own the payment"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Payment not found"
            ),
            @ApiResponse(
                responseCode = "502",
                description = "Bank Service unavailable"
            ),
        }
    )
    @PostMapping("/refund")
    public ResponseEntity<RefundResponse> requestRefund(
        @Valid @RequestBody RefundRequest request,
        @AuthenticationPrincipal User authenticatedUser
    ) {
        log.info(
            "POST /api/payments/refund - Request refund for user {} with reason: {}",
            request.getOrderId(),
            authenticatedUser.getId(),
            request.getReason()
        );

        RefundResponse response = paymentService.requestRefund(
            request.getOrderId(),
            request.getReason(),
            authenticatedUser.getId()
        );

        return ResponseEntity.ok(response);
    }
}
