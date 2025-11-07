package com.comp5348.store.service;

import com.comp5348.store.dto.event.EmailEvent;
import com.comp5348.store.dto.payment.*;
import com.comp5348.store.exception.*;
import com.comp5348.store.model.order.Order;
import com.comp5348.store.model.order.OrderStatus;
import com.comp5348.store.model.payment.*;
import com.comp5348.store.repository.*;
import com.comp5348.store.service.bank.BankServiceClient;
import com.comp5348.store.service.event.EventPublisher;
import com.comp5348.store.service.warehouse.WarehouseGrpcClient;
import com.comp5348.store.util.PaymentMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final RefundRepository refundRepository;
    private final OrderRepository orderRepository;
    private final BankServiceClient bankServiceClient;
    private final PaymentMapper paymentMapper;
    private final EventPublisher eventPublisher;
    private final WarehouseGrpcClient warehouseGrpcClient;
    private final ShipmentService shipmentService;

    /**
     * Create payment for an order and generate BPAY instructions.
     *
     * Workflow:
     * 1. Validate order exists and user owns it
     * 2. Check if payment already exists
     * 3. Calculate payment amount from order
     * 4. Generate BPAY via Bank Service
     * 5. Create PaymentMethod with BPAY metadata
     * 6. Create Payment record
     * 7. Return payment details
     *
     * @param request CreatePaymentRequest with order_id and method
     * @param userId  Authenticated user ID
     * @return CreatePaymentResponse with payment_id and status
     */
    @Transactional
    public CreatePaymentResponse createPayment(
        CreatePaymentRequest request,
        Long userId
    ) {
        log.info(
            "Creating payment for order {} with method {}",
            request.getOrderId(),
            request.getMethod()
        );

        // 1. Validate order
        Order order = orderRepository
            .findById(request.getOrderId())
            .orElseThrow(() ->
                new OrderNotFoundException(
                    "Order not found: " + request.getOrderId()
                )
            );

        if (!order.getUser().getId().equals(userId)) {
            throw new UnauthorizedOrderAccessException(
                "User does not own this order"
            );
        }

        // 2. Check duplicate payment
        if (paymentRepository.existsByOrderId(order.getId())) {
            throw new DuplicatePaymentException(
                "Payment already exists for order: " + order.getId()
            );
        }

        // 3. Calculate amount
        BigDecimal amount = calculateOrderTotal(order);

        // 4. Generate BPAY
        if (!"BPAY".equals(request.getMethod())) {
            throw new UnsupportedPaymentMethodException(
                "Only BPAY is supported"
            );
        }

        BankBpayResponse bpayResponse = bankServiceClient.createBpayPayment(
            "ORD-" + order.getId(),
            amount
        );

        // 5. Create PaymentMethod with BPAY metadata
        Map<String, Object> bpayMetadata = new HashMap<>();
        bpayMetadata.put("biller_code", bpayResponse.getBillerCode());
        bpayMetadata.put("reference_number", bpayResponse.getReferenceNumber());
        bpayMetadata.put("expires_at", bpayResponse.getExpiresAt().toString());

        PaymentMethod paymentMethod = PaymentMethod.builder()
            .type("BPAY")
            .payload(bpayMetadata)
            .build();

        paymentMethod = paymentMethodRepository.save(paymentMethod);

        // 6. Create Payment
        Payment payment = Payment.builder()
            .amount(amount)
            .order(order)
            .status(PaymentStatus.PENDING)
            .paymentMethod(paymentMethod)
            .build();
        payment = paymentRepository.save(payment);

        log.info(
            "Successfully created payment {} for order {}",
            payment.getId(),
            order.getId()
        );

        return paymentMapper.toCreateResponse(payment);
    }

    /**
     * Retrieve BPAY payment information.
     *
     * @param paymentId Payment identifier
     * @param userId    Authenticated user ID
     * @return BpayInfoResponse with biller code and reference
     */
    @Transactional(readOnly = true)
    public BpayInfoResponse getBpayInfo(Long paymentId, Long userId) {
        Payment payment = paymentRepository
            .findById(paymentId)
            .orElseThrow(() ->
                new PaymentNotFoundException("Payment not found: " + paymentId)
            );

        // Security: user owns order
        if (!payment.getOrder().getUser().getId().equals(userId)) {
            throw new UnauthorizedOrderAccessException(
                "User does not own this payment"
            );
        }

        if (!"BPAY".equals(payment.getPaymentMethod().getType())) {
            throw new InvalidPaymentMethodException("Payment is not BPAY type");
        }

        Map<String, Object> payload = payment.getPaymentMethod().getPayload();

        return BpayInfoResponse.builder()
            .billerCode((String) payload.get("biller_code"))
            .referenceNumber((String) payload.get("reference_number"))
            .amount(payment.getAmount())
            .expiresAt(LocalDateTime.parse((String) payload.get("expires_at")))
            .build();
    }

    /**
     * Handle payment confirmation webhook from Bank Service.
     *
     * Workflow:
     * 1. Find payment by bank_payment_id or order_id
     * 2. Update payment status based on webhook type
     * 3. For success: Update order to PROCESSING, commit stock via gRPC, send
     * success email
     * 4. For failure: Update order to CANCELLED, rollback stock via gRPC, send
     * failure email
     *
     * @param event PaymentWebhookEvent from Bank
     */
    @Transactional
    public void handlePaymentWebhook(PaymentWebhookEvent event) {
        log.info(
            "Handling payment webhook: type={}, orderId={}, paymentId={}",
            event.getType(),
            event.getOrderId(),
            event.getPaymentId()
        );

        if ("BPAY_PAYMENT_COMPLETED".equals(event.getType())) {
            handlePaymentSuccess(event);
        } else if ("BPAY_PAYMENT_FAILED".equals(event.getType())) {
            handlePaymentFailure(event);
        } else if ("REFUND_COMPLETED".equals(event.getType())) {
            handleRefundWebhook(event);
        }
    }

    @Transactional
    public RefundResponse requestRefund(
        Long orderId,
        String reason,
        Long userId
    ) {
        log.info(
            "Requesting refund for orderId {} with reason: {}",
            orderId,
            reason
        );

        // 1. Validate payment
        Payment payment = paymentRepository
            .findByOrderId(orderId)
            .orElseThrow(() ->
                new PaymentNotFoundException(
                    "Payment not found by order id: " + orderId
                )
            );

        if (!payment.getOrder().getUser().getId().equals(userId)) {
            throw new UnauthorizedOrderAccessException(
                "User does not own this payment"
            );
        }

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new InvalidPaymentStatusException(
                "Payment must be completed to request refund"
            );
        }

        // 2. Check duplicate refund
        if (refundRepository.existsByPaymentId(payment.getId())) {
            throw new DuplicateRefundException(
                "Refund already exists for payment: " + payment.getId()
            );
        }

        // 3. Request refund from Bank
        Long transactionId = bankServiceClient.requestRefund(
            payment.getBankPaymentId(),
            payment.getAmount(),
            reason
        );

        // 4. Create Refund record
        Refund refund = Refund.builder()
            .payment(payment)
            .transactionId(transactionId)
            .amount(payment.getAmount())
            .reason(reason)
            .status(RefundStatus.PROCESSING)
            .build();

        refund = refundRepository.save(refund);

        // 5. Update payment status
        payment.markRefunded();
        paymentRepository.save(payment);

        log.info(
            "Successfully requested refund {} for payment {}",
            refund.getId(),
            payment.getId()
        );

        return paymentMapper.toRefundResponse(refund);
    }

    /**
     * Handle refund completion webhook from Bank Service.
     */
    @Transactional
    public void handleRefundWebhook(PaymentWebhookEvent event) {
        log.info("Handling refund webhook: paymentId={}", event.getPaymentId());

        Payment payment = paymentRepository
            .findByBankPaymentId(event.getPaymentId())
            .orElseThrow(() ->
                new PaymentNotFoundException(
                    "Payment not found for refund webhook"
                )
            );

        Refund refund = refundRepository
            .findByPaymentId(payment.getId())
            .orElseThrow(() ->
                new RefundNotFoundException("Refund not found for payment")
            );

        refund.markCompleted();
        refundRepository.save(refund);

        log.info(
            "Refund {} completed for payment {}",
            refund.getId(),
            payment.getId()
        );

        // Publish refund confirmation email
        Order order = payment.getOrder();
        EmailEvent emailEvent = EmailEvent.builder()
            .type("REFUND_CONFIRMATION")
            .to(order.getEmail())
            .template("refund_confirmation")
            .params(
                Map.of(
                    "orderId",
                    "ORD-" + order.getId(),
                    "refundAmount",
                    refund.getAmount(),
                    "customerName",
                    order.getFirstName() + " " + order.getLastName(),
                    "processedDate",
                    LocalDateTime.now().toString()
                )
            )
            .eventId("evt-refund-confirmation-" + refund.getId())
            .timestamp(LocalDateTime.now())
            .build();

        eventPublisher.publishEmailEvent(emailEvent);
    }

    /**
     * Handle successful payment webhook.
     *
     * @param event PaymentWebhookEvent
     */
    private void handlePaymentSuccess(PaymentWebhookEvent event) {
        // Find payment
        Payment payment = findPaymentByEvent(event);
        Order order = payment.getOrder();

        // Update payment status
        payment.setBankPaymentId(event.getPaymentId());
        payment.markCompleted();
        paymentRepository.save(payment);

        // Update order status
        order.setStatus(OrderStatus.PROCESSING);
        orderRepository.save(order);

        log.info(
            "Payment {} completed for order {}",
            payment.getId(),
            order.getId()
        );

        // SYNC gRPC call to commit stock

        // TODO: Maybe we could also publish payment fulfillment event to RabbitMQ and
        // process
        // gRPC call in the fulfillment service.
        try {
            var response = warehouseGrpcClient.commitStock(order.getId());

            if (response.getSuccess()) {
                log.info(
                    "Successfully committed stock for order {}",
                    order.getId()
                );

                // Publish order confirmation email (async via RabbitMQ)
                publishOrderConfirmationEmail(order);

                // Request shipment from DeliveryCo (after successful stock commit)
                try {
                    shipmentService.requestShipment(
                        order,
                        response.getDeliveryPackagesList()
                    );
                    log.info(
                        "Successfully requested shipment for order {}",
                        order.getId()
                    );
                } catch (Exception shipmentEx) {
                    log.error(
                        "Failed to request shipment for order {}: {}",
                        order.getId(),
                        shipmentEx.getMessage()
                    );
                    // Don't fail the payment process if shipment request fails
                    // This can be retried manually or via scheduled job
                }
            } else {
                log.error(
                    "Failed to commit stock for order {}: {}",
                    order.getId(),
                    response.getMessage()
                );
                // TODO: Implement compensation logic
            }
        } catch (Exception e) {
            log.error(
                "gRPC commitStock failed for order {}: {}",
                order.getId(),
                e.getMessage(),
                e
            );
            // TODO: Implement compensation logic
        }
    }

    /**
     * Handle failed payment webhook.
     *
     * @param event PaymentWebhookEvent
     */
    private void handlePaymentFailure(PaymentWebhookEvent event) {
        // Find payment
        Payment payment = findPaymentByEvent(event);
        Order order = payment.getOrder();

        // Update payment status
        payment.setBankPaymentId(event.getPaymentId());
        payment.setStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);

        // Update order status
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        log.info(
            "Payment {} failed for order {}",
            payment.getId(),
            order.getId()
        );

        // SYNC gRPC call to rollback reserved stock
        try {
            var response = warehouseGrpcClient.rollbackStock(order.getId());
            if (!response.getRolledBack()) {
                log.error(
                    "Failed to rollback stock for order {}: {}",
                    order.getId(),
                    response.getMessage()
                );
            } else {
                log.info(
                    "Successfully rolled back stock for order {}",
                    order.getId()
                );
            }
        } catch (Exception e) {
            log.error(
                "gRPC rollbackStock failed for order {}: {}",
                order.getId(),
                e.getMessage(),
                e
            );
        }

        // Publish payment failure email (async via RabbitMQ)
        publishPaymentFailureEmail(order, payment);
    }

    /**
     * Find payment by webhook event (try bank payment ID first, fallback to order
     * ID).
     */
    private Payment findPaymentByEvent(PaymentWebhookEvent event) {
        return paymentRepository
            .findByBankPaymentId(event.getPaymentId())
            .or(() -> {
                // Fallback: find by order_id from webhook
                String orderIdStr = event.getOrderId().replace("ORD-", "");
                Long orderId = Long.parseLong(orderIdStr);
                return paymentRepository.findByOrderId(orderId);
            })
            .orElseThrow(() ->
                new PaymentNotFoundException("Payment not found for webhook")
            );
    }

    private void publishOrderConfirmationEmail(Order order) {
        EmailEvent emailEvent = EmailEvent.builder()
            .type("ORDER_CONFIRMATION")
            .to(order.getEmail())
            .template("order_confirmation")
            .params(
                Map.of(
                    "orderId",
                    order.getId(),
                    "orderNumber",
                    "ORD-" + order.getId(),
                    "total",
                    order.getTotalAmount(),
                    "customerName",
                    order.getFirstName() + " " + order.getLastName(),
                    "productName",
                    order.getProduct().getName(),
                    "quantity",
                    order.getQuantity()
                )
            )
            .eventId("evt-order-" + order.getId())
            .timestamp(LocalDateTime.now())
            .build();

        eventPublisher.publishEmailEvent(emailEvent);
    }

    /**
     * Publish payment failure email event.
     */
    private void publishPaymentFailureEmail(Order order, Payment payment) {
        EmailEvent emailEvent = EmailEvent.builder()
            .type("PAYMENT_FAILED")
            .to(order.getEmail())
            .template("payment_failed")
            .params(
                Map.of(
                    "orderId",
                    "ORD-" + order.getId(),
                    "amount",
                    payment.getAmount(),
                    "customerName",
                    order.getFirstName() + " " + order.getLastName(),
                    "reason",
                    "Payment processing failed"
                )
            )
            .eventId("evt-payment-failed-" + payment.getId())
            .timestamp(LocalDateTime.now())
            .build();

        eventPublisher.publishEmailEvent(emailEvent);
    }

    /**
     * Calculate order total amount.
     */
    private BigDecimal calculateOrderTotal(Order order) {
        return order
            .getProduct()
            .getPrice()
            .multiply(BigDecimal.valueOf(order.getQuantity()));
    }
}
