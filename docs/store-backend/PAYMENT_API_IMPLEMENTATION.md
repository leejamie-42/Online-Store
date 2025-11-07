# Payment API Implementation Guide

**Version**: 1.0
**Last Updated**: October 28, 2025
**Status**: Implementation Ready

---

## Overview

Implement Payment API in store-backend with Bank Service REST integration for BPAY payment generation, webhook handling, and refund processing.

### Objectives

1. Generate BPAY payment instructions for orders
2. Integrate with Bank Service REST API for payment processing
3. Handle payment confirmation via webhooks
4. Support refund requests and processing
5. Update order status based on payment lifecycle events
6. Publish RabbitMQ events for email notifications

### Key Technologies

- **PostgreSQL** with Flyway migrations
- **Spring Data JPA** for ORM
- **RestTemplate/WebClient** for Bank Service integration
- **Webhooks** for async payment event notifications
- **RabbitMQ** for email and order update events
- **Jakarta Bean Validation** for request validation
- **Spring Security** with JWT for authentication

---

## Implementation Phases

### Phase 1: Database Schema

**Files**:
- `V5__create_payment_methods_table.sql`
- `V6__create_payments_table.sql`
- `V7__create_refunds_table.sql`

**Key Elements**:

#### PaymentMethod Table
```sql
-- Payment method types and metadata
CREATE TABLE payment_methods (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(20) NOT NULL,           -- 'BPAY'
    payload JSONB,                       -- BPAY metadata (biller_code, reference)
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_payment_method_type CHECK (type IN ('BPAY'))
);

CREATE INDEX idx_payment_methods_type ON payment_methods(type);
```

#### Payment Table
```sql
-- Payment transaction records
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    amount DECIMAL(10,2) NOT NULL,
    order_id BIGINT NOT NULL UNIQUE,     -- One payment per order
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    method_id BIGINT NOT NULL,

    -- Bank service reference
    bank_payment_id VARCHAR(100),        -- External payment ID from Bank

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,

    -- Foreign keys
    CONSTRAINT fk_payments_order FOREIGN KEY (order_id)
        REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_payments_method FOREIGN KEY (method_id)
        REFERENCES payment_methods(id) ON DELETE RESTRICT,

    -- Constraints
    CONSTRAINT chk_payments_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_payments_status CHECK (status IN
        ('pending', 'processing', 'completed', 'failed', 'refunded'))
);

CREATE INDEX idx_payments_order_id ON payments(order_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_bank_payment_id ON payments(bank_payment_id);
```

#### Refund Table
```sql
-- Refund transaction records
CREATE TABLE refunds (
    id BIGSERIAL PRIMARY KEY,
    payment_id BIGINT NOT NULL UNIQUE,   -- One refund per payment
    transaction_id BIGINT,               -- Bank transaction reference
    amount DECIMAL(15,2) NOT NULL,
    reason TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'pending',

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    refunded_at TIMESTAMP,

    -- Foreign key
    CONSTRAINT fk_refunds_payment FOREIGN KEY (payment_id)
        REFERENCES payments(id) ON DELETE CASCADE,

    -- Constraints
    CONSTRAINT chk_refunds_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_refunds_status CHECK (status IN
        ('pending', 'processing', 'completed', 'failed'))
);

CREATE INDEX idx_refunds_payment_id ON refunds(payment_id);
CREATE INDEX idx_refunds_status ON refunds(status);
```

**Commands**:
```bash
./gradlew :store-backend:flywayMigrate
./gradlew :store-backend:flywayInfo
```

---

### Phase 2: Domain Models

**Files**:
- `model/payment/PaymentStatus.java` - enum with transitions
- `model/payment/PaymentMethod.java` - entity for payment types
- `model/payment/Payment.java` - payment transaction entity
- `model/payment/Refund.java` - refund transaction entity
- `model/payment/RefundStatus.java` - refund status enum

**PaymentStatus.java**:
```java
package com.comp5348.store.model.payment;

public enum PaymentStatus {
    PENDING,       // Payment initiated, awaiting confirmation
    PROCESSING,    // Payment being processed by Bank
    COMPLETED,     // Payment successfully completed
    FAILED,        // Payment failed
    REFUNDED;      // Payment refunded

    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED || this == REFUNDED;
    }

    public boolean canTransitionTo(PaymentStatus newStatus) {
        return switch (this) {
            case PENDING -> newStatus == PROCESSING || newStatus == FAILED;
            case PROCESSING -> newStatus == COMPLETED || newStatus == FAILED;
            case COMPLETED -> newStatus == REFUNDED;
            case FAILED, REFUNDED -> false;
        };
    }
}
```

**Payment.java**:
```java
package com.comp5348.store.model.payment;

import com.comp5348.store.model.order.Order;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status = PaymentStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "method_id", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "bank_payment_id", length = 100)
    private String bankPaymentId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void markCompleted() {
        this.status = PaymentStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void markRefunded() {
        this.status = PaymentStatus.REFUNDED;
    }
}
```

**PaymentMethod.java**:
```java
package com.comp5348.store.model.payment;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "payment_methods")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String type; // "BPAY"

    @Column(columnDefinition = "jsonb")
    private Map<String, Object> payload; // BPAY metadata

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

**RefundStatus.java**:
```java
package com.comp5348.store.model.payment;

public enum RefundStatus {
    PENDING,       // Refund requested
    PROCESSING,    // Refund being processed
    COMPLETED,     // Refund completed
    FAILED;        // Refund failed

    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED;
    }
}
```

**Refund.java**:
```java
package com.comp5348.store.model.payment;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "refunds")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false, unique = true)
    private Payment payment;

    @Column(name = "transaction_id")
    private Long transactionId; // Bank transaction reference

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RefundStatus status = RefundStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void markCompleted() {
        this.status = RefundStatus.COMPLETED;
        this.refundedAt = LocalDateTime.now();
    }
}
```

---

### Phase 3: DTOs

**Package**: `dto/payment/`

**Files**:

#### 1. CreatePaymentRequest.java
```java
package com.comp5348.store.dto.payment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

/**
 * Request DTO for creating a payment.
 *
 * Example:
 * {
 *   "order_id": 1,
 *   "method": "BPAY"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentRequest {

    @NotNull(message = "Order ID is required")
    @Positive(message = "Order ID must be positive")
    private Long orderId;

    @NotNull(message = "Payment method is required")
    private String method; // "BPAY"
}
```

#### 2. CreatePaymentResponse.java
```java
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
```

#### 3. BpayInfoResponse.java
```java
package com.comp5348.store.dto.payment;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for BPAY payment information.
 *
 * Example:
 * {
 *   "biller_code": "93242",
 *   "reference_number": "BP-ORD-001",
 *   "amount": 149.97,
 *   "expires_at": "2025-10-20T12:00:00"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BpayInfoResponse {
    private String billerCode;
    private String referenceNumber;
    private BigDecimal amount;
    private LocalDateTime expiresAt;
}
```

#### 4. RefundRequest.java
```java
package com.comp5348.store.dto.payment;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequest {

    @NotBlank(message = "Refund reason is required")
    private String reason;
}
```

#### 5. RefundResponse.java
```java
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
```

#### 6. PaymentWebhookEvent.java
```java
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
```

#### 7. BankBpayRequest.java (Bank Service API)
```java
package com.comp5348.store.dto.payment;

import lombok.*;
import java.math.BigDecimal;

/**
 * Request to Bank Service for BPAY generation.
 * POST /bank/api/bpay
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankBpayRequest {
    private Long accountId;    // Store's bank account
    private String orderId;
    private BigDecimal amount;
}
```

#### 8. BankBpayResponse.java (Bank Service API)
```java
package com.comp5348.store.dto.payment;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response from Bank Service for BPAY generation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankBpayResponse {
    private String billerCode;
    private String referenceNumber;
    private BigDecimal amount;
    private LocalDateTime expiresAt;
}
```

#### 9. PaymentMapper.java (Utility)
```java
package com.comp5348.store.util;

import com.comp5348.store.dto.payment.*;
import com.comp5348.store.model.payment.*;
import org.springframework.stereotype.Component;

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
```

---

### Phase 4: Bank Service REST Client

**Files**:
- `service/bank/BankServiceClient.java`
- `config/BankServiceConfig.java`

**BankServiceClient.java**:
```java
package com.comp5348.store.service.bank;

import com.comp5348.store.dto.payment.*;
import com.comp5348.store.exception.BankServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class BankServiceClient {

    private final RestTemplate restTemplate;

    @Value("${bank.service.base-url}")
    private String bankServiceBaseUrl;

    @Value("${bank.service.account-id}")
    private Long storeAccountId;

    /**
     * Generate BPAY payment instructions via Bank Service.
     *
     * @param orderId Order identifier
     * @param amount Payment amount
     * @return BPAY details (biller_code, reference_number, expires_at)
     * @throws BankServiceException if Bank Service fails
     */
    public BankBpayResponse createBpayPayment(String orderId, BigDecimal amount) {
        String url = bankServiceBaseUrl + "/bank/api/bpay";

        BankBpayRequest request = BankBpayRequest.builder()
            .accountId(storeAccountId)
            .orderId(orderId)
            .amount(amount)
            .build();

        try {
            log.info("Creating BPAY payment for order {} with amount {}", orderId, amount);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<BankBpayRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<BankBpayResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                BankBpayResponse.class
            );

            if (response.getStatusCode() != HttpStatus.OK && response.getStatusCode() != HttpStatus.CREATED) {
                throw new BankServiceException("Bank Service returned status: " + response.getStatusCode());
            }

            BankBpayResponse bpayResponse = response.getBody();
            log.info("Successfully created BPAY payment for order {}: biller={}, ref={}",
                orderId, bpayResponse.getBillerCode(), bpayResponse.getReferenceNumber());

            return bpayResponse;

        } catch (RestClientException e) {
            log.error("Failed to create BPAY payment for order {}: {}", orderId, e.getMessage());
            throw new BankServiceException("Failed to communicate with Bank Service", e);
        }
    }

    /**
     * Register webhook callback URL with Bank Service.
     *
     * @param callbackUrl Store's webhook endpoint
     */
    public void registerWebhook(String callbackUrl) {
        String url = bankServiceBaseUrl + "/bank/api/webhooks/register";

        try {
            log.info("Registering payment webhook: {}", callbackUrl);

            Map<String, String> request = Map.of(
                "event", "PAYMENT_EVENT",
                "callback_url", callbackUrl
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

            restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);

            log.info("Successfully registered payment webhook");

        } catch (RestClientException e) {
            log.error("Failed to register payment webhook: {}", e.getMessage());
            // Don't throw - webhook registration failure shouldn't prevent app startup
        }
    }

    /**
     * Request refund from Bank Service.
     *
     * @param paymentId Payment identifier
     * @param amount Refund amount
     * @param reason Refund reason
     * @return Transaction ID from Bank
     */
    public Long requestRefund(String paymentId, BigDecimal amount, String reason) {
        String url = bankServiceBaseUrl + "/bank/api/refunds";

        try {
            log.info("Requesting refund for payment {} with amount {}", paymentId, amount);

            Map<String, Object> request = Map.of(
                "payment_id", paymentId,
                "amount", amount,
                "reason", reason
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                Map.class
            );

            Map<String, Object> body = response.getBody();
            Long transactionId = ((Number) body.get("transaction_id")).longValue();

            log.info("Successfully requested refund for payment {}: transaction_id={}",
                paymentId, transactionId);

            return transactionId;

        } catch (RestClientException e) {
            log.error("Failed to request refund for payment {}: {}", paymentId, e.getMessage());
            throw new BankServiceException("Failed to request refund from Bank Service", e);
        }
    }
}
```

**BankServiceConfig.java**:
```java
package com.comp5348.store.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import java.time.Duration;

@Configuration
public class BankServiceConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
            .setConnectTimeout(Duration.ofSeconds(5))
            .setReadTimeout(Duration.ofSeconds(10))
            .build();
    }
}
```

**Configuration** (`application-local.yml`):
```yaml
bank:
  service:
    base-url: http://localhost:8082  # Bank Service URL
    account-id: 1                     # Store's bank account ID
```

---

### Phase 5: Repository Layer

**Files**:
- `repository/PaymentRepository.java`
- `repository/PaymentMethodRepository.java`
- `repository/RefundRepository.java`

**PaymentRepository.java**:
```java
package com.comp5348.store.repository;

import com.comp5348.store.model.payment.Payment;
import com.comp5348.store.model.payment.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Find payment by order ID.
     */
    Optional<Payment> findByOrderId(Long orderId);

    /**
     * Find payment by Bank Service payment ID.
     */
    Optional<Payment> findByBankPaymentId(String bankPaymentId);

    /**
     * Find all payments for a user's orders.
     */
    List<Payment> findByOrderUserId(Long userId);

    /**
     * Find payments by status.
     */
    List<Payment> findByStatus(PaymentStatus status);

    /**
     * Check if payment exists for order.
     */
    boolean existsByOrderId(Long orderId);
}
```

**PaymentMethodRepository.java**:
```java
package com.comp5348.store.repository;

import com.comp5348.store.model.payment.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {

    /**
     * Find payment method by type.
     */
    Optional<PaymentMethod> findByType(String type);
}
```

**RefundRepository.java**:
```java
package com.comp5348.store.repository;

import com.comp5348.store.model.payment.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {

    /**
     * Find refund by payment ID.
     */
    Optional<Refund> findByPaymentId(Long paymentId);

    /**
     * Check if refund exists for payment.
     */
    boolean existsByPaymentId(Long paymentId);
}
```

---

### Phase 6: Business Logic

**File**: `service/PaymentService.java`

**PaymentService.java**:
```java
package com.comp5348.store.service;

import com.comp5348.store.dto.payment.*;
import com.comp5348.store.exception.*;
import com.comp5348.store.model.order.Order;
import com.comp5348.store.model.order.OrderStatus;
import com.comp5348.store.model.payment.*;
import com.comp5348.store.repository.*;
import com.comp5348.store.service.bank.BankServiceClient;
import com.comp5348.store.util.PaymentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

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
    // TODO: Add RabbitMQ publisher for email notifications

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
     * @param userId Authenticated user ID
     * @return CreatePaymentResponse with payment_id and status
     */
    @Transactional
    public CreatePaymentResponse createPayment(CreatePaymentRequest request, Long userId) {
        log.info("Creating payment for order {} with method {}", request.getOrderId(), request.getMethod());

        // 1. Validate order
        Order order = orderRepository.findById(request.getOrderId())
            .orElseThrow(() -> new OrderNotFoundException("Order not found: " + request.getOrderId()));

        if (!order.getUser().getId().equals(userId)) {
            throw new UnauthorizedOrderAccessException("User does not own this order");
        }

        // 2. Check duplicate payment
        if (paymentRepository.existsByOrderId(order.getId())) {
            throw new DuplicatePaymentException("Payment already exists for order: " + order.getId());
        }

        // 3. Calculate amount
        BigDecimal amount = calculateOrderTotal(order);

        // 4. Generate BPAY
        if (!"BPAY".equals(request.getMethod())) {
            throw new UnsupportedPaymentMethodException("Only BPAY is supported");
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

        log.info("Successfully created payment {} for order {}", payment.getId(), order.getId());

        // TODO: Publish email notification event

        return paymentMapper.toCreateResponse(payment);
    }

    /**
     * Retrieve BPAY payment information.
     *
     * @param paymentId Payment identifier
     * @param userId Authenticated user ID
     * @return BpayInfoResponse with biller code and reference
     */
    @Transactional(readOnly = true)
    public BpayInfoResponse getBpayInfo(Long paymentId, Long userId) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + paymentId));

        // Security: user owns order
        if (!payment.getOrder().getUser().getId().equals(userId)) {
            throw new UnauthorizedOrderAccessException("User does not own this payment");
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
     * 2. Update payment status to COMPLETED
     * 3. Update order status to PROCESSING
     * 4. Publish RabbitMQ event for warehouse fulfillment
     * 5. Send email confirmation
     *
     * @param event PaymentWebhookEvent from Bank
     */
    @Transactional
    public void handlePaymentWebhook(PaymentWebhookEvent event) {
        log.info("Handling payment webhook: type={}, orderId={}, paymentId={}",
            event.getType(), event.getOrderId(), event.getPaymentId());

        if ("BPAY_PAYMENT_COMPLETED".equals(event.getType())) {
            // Find payment
            Payment payment = paymentRepository.findByBankPaymentId(event.getPaymentId())
                .or(() -> {
                    // Fallback: find by order_id from webhook
                    String orderIdStr = event.getOrderId().replace("ORD-", "");
                    Long orderId = Long.parseLong(orderIdStr);
                    return paymentRepository.findByOrderId(orderId);
                })
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for webhook"));

            // Update payment
            payment.setBankPaymentId(event.getPaymentId());
            payment.markCompleted();
            paymentRepository.save(payment);

            // Update order status
            Order order = payment.getOrder();
            order.setStatus(OrderStatus.PROCESSING);
            orderRepository.save(order);

            log.info("Payment {} completed for order {}", payment.getId(), order.getId());

            // TODO: Publish fulfillment event to RabbitMQ
            // TODO: Send email confirmation

        } else if ("REFUND_COMPLETED".equals(event.getType())) {
            handleRefundWebhook(event);
        }
    }

    /**
     * Request refund for a payment.
     *
     * Workflow:
     * 1. Validate payment exists and completed
     * 2. Check if refund already exists
     * 3. Request refund from Bank Service
     * 4. Create Refund record
     * 5. Update payment status to REFUNDED
     * 6. Cancel order
     * 7. Trigger inventory rollback
     *
     * @param paymentId Payment identifier
     * @param request RefundRequest with reason
     * @param userId Authenticated user ID
     * @return RefundResponse with status
     */
    @Transactional
    public RefundResponse requestRefund(Long paymentId, RefundRequest request, Long userId) {
        log.info("Requesting refund for payment {} with reason: {}", paymentId, request.getReason());

        // 1. Validate payment
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + paymentId));

        if (!payment.getOrder().getUser().getId().equals(userId)) {
            throw new UnauthorizedOrderAccessException("User does not own this payment");
        }

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new InvalidPaymentStatusException("Payment must be completed to request refund");
        }

        // 2. Check duplicate refund
        if (refundRepository.existsByPaymentId(paymentId)) {
            throw new DuplicateRefundException("Refund already exists for payment: " + paymentId);
        }

        // 3. Request refund from Bank
        Long transactionId = bankServiceClient.requestRefund(
            payment.getBankPaymentId(),
            payment.getAmount(),
            request.getReason()
        );

        // 4. Create Refund record
        Refund refund = Refund.builder()
            .payment(payment)
            .transactionId(transactionId)
            .amount(payment.getAmount())
            .reason(request.getReason())
            .status(RefundStatus.PROCESSING)
            .build();
        refund = refundRepository.save(refund);

        // 5. Update payment status
        payment.markRefunded();
        paymentRepository.save(payment);

        // 6. Cancel order
        Order order = payment.getOrder();
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        log.info("Successfully requested refund {} for payment {}", refund.getId(), paymentId);

        // TODO: Publish inventory rollback event to RabbitMQ
        // TODO: Send refund confirmation email

        return paymentMapper.toRefundResponse(refund);
    }

    /**
     * Handle refund completion webhook from Bank Service.
     */
    @Transactional
    public void handleRefundWebhook(PaymentWebhookEvent event) {
        log.info("Handling refund webhook: paymentId={}", event.getPaymentId());

        Payment payment = paymentRepository.findByBankPaymentId(event.getPaymentId())
            .orElseThrow(() -> new PaymentNotFoundException("Payment not found for refund webhook"));

        Refund refund = refundRepository.findByPaymentId(payment.getId())
            .orElseThrow(() -> new RefundNotFoundException("Refund not found for payment"));

        refund.markCompleted();
        refundRepository.save(refund);

        log.info("Refund {} completed for payment {}", refund.getId(), payment.getId());

        // TODO: Send refund completed email
    }

    /**
     * Calculate order total amount.
     */
    private BigDecimal calculateOrderTotal(Order order) {
        return order.getProduct().getPrice()
            .multiply(BigDecimal.valueOf(order.getQuantity()));
    }
}
```

---

### Phase 7: Exception Handling

**Files** (in `exception/` package):
- `PaymentNotFoundException.java` - 404 Not Found
- `DuplicatePaymentException.java` - 400 Bad Request
- `DuplicateRefundException.java` - 400 Bad Request
- `InvalidPaymentStatusException.java` - 400 Bad Request
- `InvalidPaymentMethodException.java` - 400 Bad Request
- `UnsupportedPaymentMethodException.java` - 400 Bad Request
- `RefundNotFoundException.java` - 404 Not Found
- `BankServiceException.java` - 502 Bad Gateway

**PaymentNotFoundException.java**:
```java
package com.comp5348.store.exception;

public class PaymentNotFoundException extends RuntimeException {
    public PaymentNotFoundException(String message) {
        super(message);
    }
}
```

**DuplicatePaymentException.java**:
```java
package com.comp5348.store.exception;

public class DuplicatePaymentException extends RuntimeException {
    public DuplicatePaymentException(String message) {
        super(message);
    }
}
```

**BankServiceException.java**:
```java
package com.comp5348.store.exception;

public class BankServiceException extends RuntimeException {
    public BankServiceException(String message) {
        super(message);
    }

    public BankServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

**Update GlobalExceptionHandler.java**:
```java
@ExceptionHandler(PaymentNotFoundException.class)
public ResponseEntity<ErrorResponse> handlePaymentNotFound(PaymentNotFoundException ex) {
    return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
}

@ExceptionHandler(DuplicatePaymentException.class)
public ResponseEntity<ErrorResponse> handleDuplicatePayment(DuplicatePaymentException ex) {
    return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
}

@ExceptionHandler(BankServiceException.class)
public ResponseEntity<ErrorResponse> handleBankServiceError(BankServiceException ex) {
    return buildErrorResponse("Payment service temporarily unavailable", HttpStatus.BAD_GATEWAY);
}

@ExceptionHandler(InvalidPaymentStatusException.class)
public ResponseEntity<ErrorResponse> handleInvalidPaymentStatus(InvalidPaymentStatusException ex) {
    return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
}

// Add similar handlers for other payment exceptions
```

---

### Phase 8: REST Controller

**Files**:
- `controller/PaymentController.java`
- `controller/WebhookController.java`

**PaymentController.java**:
```java
package com.comp5348.store.controller;

import com.comp5348.store.dto.payment.*;
import com.comp5348.store.security.JwtUtil;
import com.comp5348.store.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST API for payment management.
 *
 * Endpoints:
 * - POST   /api/payments           - Create payment
 * - GET    /api/payments/{id}      - Get BPAY info
 * - POST   /api/payments/{id}/refund - Request refund
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final JwtUtil jwtUtil;

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
    @PostMapping
    public ResponseEntity<CreatePaymentResponse> createPayment(
        @Valid @RequestBody CreatePaymentRequest request,
        Authentication authentication
    ) {
        Long userId = jwtUtil.getUserIdFromAuthentication(authentication);
        CreatePaymentResponse response = paymentService.createPayment(request, userId);
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
    @GetMapping("/{id}")
    public ResponseEntity<BpayInfoResponse> getBpayInfo(
        @PathVariable Long id,
        Authentication authentication
    ) {
        Long userId = jwtUtil.getUserIdFromAuthentication(authentication);
        BpayInfoResponse response = paymentService.getBpayInfo(id, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Request refund for a payment.
     *
     * POST /api/payments/{id}/refund
     *
     * Request:
     * {
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
    @PostMapping("/{id}/refund")
    public ResponseEntity<RefundResponse> requestRefund(
        @PathVariable Long id,
        @Valid @RequestBody RefundRequest request,
        Authentication authentication
    ) {
        Long userId = jwtUtil.getUserIdFromAuthentication(authentication);
        RefundResponse response = paymentService.requestRefund(id, request, userId);
        return ResponseEntity.ok(response);
    }
}
```

**WebhookController.java**:
```java
package com.comp5348.store.controller;

import com.comp5348.store.dto.payment.PaymentWebhookEvent;
import com.comp5348.store.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Webhook endpoints for external service callbacks.
 *
 * Security: In production, validate webhook signatures.
 */
@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final PaymentService paymentService;

    /**
     * Receive payment events from Bank Service.
     *
     * POST /api/webhooks/payment
     *
     * Request:
     * {
     *   "type": "BPAY_PAYMENT_COMPLETED",
     *   "order_id": "ORD-001",
     *   "payment_id": "PAY-123",
     *   "amount": 149.97,
     *   "paid_at": "2025-10-21T11:00:00Z"
     * }
     *
     * Response: 200 OK
     */
    @PostMapping("/payment")
    public ResponseEntity<Void> handlePaymentWebhook(@RequestBody PaymentWebhookEvent event) {
        log.info("Received payment webhook: {}", event);

        // TODO: In production, validate webhook signature

        try {
            paymentService.handlePaymentWebhook(event);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to process payment webhook: {}", e.getMessage());
            // Return 200 to prevent Bank Service retries
            // Failed webhooks should be logged and handled manually
            return ResponseEntity.ok().build();
        }
    }
}
```

---

### Phase 9: Testing

**Unit Tests** (`PaymentServiceTest.java`):
```java
package com.comp5348.store.service;

import com.comp5348.store.dto.payment.*;
import com.comp5348.store.exception.*;
import com.comp5348.store.model.order.Order;
import com.comp5348.store.model.payment.*;
import com.comp5348.store.repository.*;
import com.comp5348.store.service.bank.BankServiceClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private BankServiceClient bankServiceClient;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void createPayment_Success() {
        // Given
        CreatePaymentRequest request = new CreatePaymentRequest(1L, "BPAY");
        Order order = createMockOrder(1L, 1L);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.existsByOrderId(1L)).thenReturn(false);
        when(bankServiceClient.createBpayPayment(anyString(), any()))
            .thenReturn(createMockBpayResponse());

        // When
        CreatePaymentResponse response = paymentService.createPayment(request, 1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("pending");
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void createPayment_OrderNotFound() {
        // Given
        CreatePaymentRequest request = new CreatePaymentRequest(999L, "BPAY");
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> paymentService.createPayment(request, 1L))
            .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void createPayment_UnauthorizedUser() {
        // Given
        CreatePaymentRequest request = new CreatePaymentRequest(1L, "BPAY");
        Order order = createMockOrder(1L, 999L); // Different user

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // When/Then
        assertThatThrownBy(() -> paymentService.createPayment(request, 1L))
            .isInstanceOf(UnauthorizedOrderAccessException.class);
    }

    @Test
    void createPayment_DuplicatePayment() {
        // Given
        CreatePaymentRequest request = new CreatePaymentRequest(1L, "BPAY");
        Order order = createMockOrder(1L, 1L);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.existsByOrderId(1L)).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> paymentService.createPayment(request, 1L))
            .isInstanceOf(DuplicatePaymentException.class);
    }

    @Test
    void handlePaymentWebhook_Success() {
        // Given
        PaymentWebhookEvent event = PaymentWebhookEvent.builder()
            .type("BPAY_PAYMENT_COMPLETED")
            .orderId("ORD-1")
            .paymentId("PAY-123")
            .amount(new BigDecimal("149.97"))
            .build();

        Payment payment = createMockPayment();
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.of(payment));

        // When
        paymentService.handlePaymentWebhook(event);

        // Then
        verify(paymentRepository).save(argThat(p ->
            p.getStatus() == PaymentStatus.COMPLETED
        ));
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void requestRefund_Success() {
        // Given
        RefundRequest request = new RefundRequest("Customer request");
        Payment payment = createMockCompletedPayment();

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(refundRepository.existsByPaymentId(1L)).thenReturn(false);
        when(bankServiceClient.requestRefund(anyString(), any(), anyString()))
            .thenReturn(123L);

        // When
        RefundResponse response = paymentService.requestRefund(1L, request, 1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("processing");
        verify(refundRepository).save(any(Refund.class));
    }

    @Test
    void requestRefund_InvalidPaymentStatus() {
        // Given
        RefundRequest request = new RefundRequest("Customer request");
        Payment payment = createMockPendingPayment();

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        // When/Then
        assertThatThrownBy(() -> paymentService.requestRefund(1L, request, 1L))
            .isInstanceOf(InvalidPaymentStatusException.class);
    }

    // Helper methods
    private Order createMockOrder(Long orderId, Long userId) {
        // Create mock order with user and product
        // ...
    }

    private BankBpayResponse createMockBpayResponse() {
        // Create mock BPAY response
        // ...
    }
}
```

**Integration Tests** (`PaymentControllerTest.java`):
```java
package com.comp5348.store.controller;

import com.comp5348.store.dto.payment.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PaymentControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void createPayment_Success() {
        // Given
        String jwtToken = getTestJwtToken();
        CreatePaymentRequest request = new CreatePaymentRequest(1L, "BPAY");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        HttpEntity<CreatePaymentRequest> entity = new HttpEntity<>(request, headers);

        // When
        ResponseEntity<CreatePaymentResponse> response = restTemplate.exchange(
            "/api/payments",
            HttpMethod.POST,
            entity,
            CreatePaymentResponse.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPaymentId()).isPositive();
    }

    @Test
    void createPayment_Unauthorized() {
        // Given
        CreatePaymentRequest request = new CreatePaymentRequest(1L, "BPAY");
        HttpEntity<CreatePaymentRequest> entity = new HttpEntity<>(request);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
            "/api/payments",
            HttpMethod.POST,
            entity,
            String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void getBpayInfo_Success() {
        // Create payment first, then retrieve BPAY info
        // ...
    }

    @Test
    void requestRefund_Success() {
        // Create payment, complete it, then request refund
        // ...
    }

    private String getTestJwtToken() {
        // Login as test user and get JWT token
        // ...
    }
}
```

---

## Payment Creation Workflow Diagram

```
┌──────────┐     POST /api/payments     ┌────────────┐
│ Frontend │ ────────────────────────► │ Controller │
└──────────┘                            └─────┬──────┘
                                              │
                                              ▼
                                        ┌──────────┐
                                        │ Service  │
                                        └────┬─────┘
                                             │
           ┌─────────────────────────────────┼────────────────┐
           │                                 │                │
           ▼                                 ▼                ▼
    ┌────────────┐                   ┌─────────────┐   ┌──────────┐
    │   Order    │                   │    Bank     │   │ Payment  │
    │ Repository │                   │   Service   │   │Repository│
    └────────────┘                   │   Client    │   └──────────┘
                                     └──────┬──────┘
                                            │
                                            ▼
                                    ┌───────────────┐
                                    │ POST /bank/   │
                                    │   api/bpay    │
                                    └───────────────┘
                                            │
                                            ▼
                                    Return BPAY info
                                    (biller_code,
                                     reference_number)
```

**Flow Steps**:
1. Controller receives authenticated request
2. Service validates order exists and user owns it
3. Service checks for duplicate payment
4. Service calculates order total amount
5. Service calls Bank Service to generate BPAY
6. Bank Service returns BPAY instructions
7. Service creates PaymentMethod with BPAY metadata
8. Service creates Payment record
9. Service saves to database
10. Return payment ID and status

---

## Payment Lifecycle State Machine

```
PENDING ──► PROCESSING ──► COMPLETED ──► REFUNDED
   │             │
   │             │
   └─────────────┴──────► FAILED
```

**State Transitions**:
- **PENDING → PROCESSING**: Payment initiated at Bank
- **PROCESSING → COMPLETED**: Bank confirms payment via webhook
- **PROCESSING → FAILED**: Payment fails at Bank
- **COMPLETED → REFUNDED**: Refund requested and processed
- **PENDING → FAILED**: Payment timeout or early failure

**Refund States**:
```
PENDING ──► PROCESSING ──► COMPLETED
                │
                └──────► FAILED
```

---

## Security Considerations

1. **JWT Authentication**: Required for all payment endpoints
2. **User Authorization**:
   - Users can only create payments for their own orders
   - Users can only view their own payment details
   - Users can only request refunds for their own payments
   - Authorization enforced at service layer
3. **Webhook Security**:
   - Validate webhook signatures (production)
   - Use HTTPS for webhook endpoints
   - Implement idempotency checks using event_id
4. **Bank Service Communication**:
   - Use HTTPS for REST calls
   - Implement timeouts and retries
   - Circuit breaker for fault tolerance
5. **Sensitive Data**:
   - Never log full BPAY credentials
   - Encrypt sensitive payment metadata
   - Audit all payment operations

---

## RabbitMQ Integration Points

### Publish Events

**Email Queue** (`email.queue`):
```json
{
  "type": "PAYMENT_CONFIRMATION",
  "to": "customer@example.com",
  "template": "payment_confirmation",
  "params": {
    "order_id": "ORD-001",
    "payment_id": "PAY-123",
    "amount": 149.97,
    "biller_code": "93242",
    "reference_number": "BP-ORD-001"
  },
  "event_id": "evt-payment-001",
  "timestamp": "2025-10-28T10:00:00Z"
}
```

**Fulfillment Queue** (`fulfillment.queue`):
```json
{
  "order_id": "ORD-001",
  "warehouse_ids": [1],
  "product_id": "p123",
  "quantity": 2,
  "event": "START_FULFILLMENT",
  "trigger": "PAYMENT_COMPLETED",
  "timestamp": "2025-10-28T10:00:00Z"
}
```

**Inventory Rollback Queue** (`inventory.rollback.queue`):
```json
{
  "order_id": "ORD-001",
  "warehouse_id": 1,
  "product_id": "p123",
  "amount": 2,
  "reason": "Payment failed / Order cancelled",
  "event_id": "evt-rollback-001",
  "timestamp": "2025-10-28T10:05:00Z"
}
```

---

## Troubleshooting

### Bank Service Connection Failed

**Symptom**: BankServiceException

**Solutions**:
```bash
# Check Bank Service is running
curl http://localhost:8082/actuator/health

# Verify configuration
grep "bank.service" application-local.yml

# Test BPAY endpoint manually
curl -X POST http://localhost:8082/bank/api/bpay \
  -H "Content-Type: application/json" \
  -d '{"account_id":1,"order_id":"ORD-001","amount":149.97}'
```

### Payment Already Exists Error

**Symptom**: DuplicatePaymentException

**Cause**: Attempting to create multiple payments for same order

**Fix**: Each order can only have one payment. Check existing payment:
```sql
SELECT * FROM payments WHERE order_id = 1;
```

### Webhook Not Received

**Symptom**: Payment stuck in PENDING status

**Solutions**:
```bash
# Check webhook registration
# Verify callback URL is accessible from Bank Service
curl -X POST http://localhost:8081/api/webhooks/payment \
  -H "Content-Type: application/json" \
  -d '{"type":"BPAY_PAYMENT_COMPLETED","order_id":"ORD-1","payment_id":"PAY-123"}'

# Check application logs for webhook calls
grep "payment webhook" store-backend.log

# Manually trigger payment completion (testing only)
# Update payment status directly in database
```

### Refund Request Failed

**Symptom**: BankServiceException during refund

**Solutions**:
```bash
# Verify payment is in COMPLETED status
SELECT status FROM payments WHERE id = 1;

# Check Bank Service refund endpoint
curl -X POST http://localhost:8082/bank/api/refunds \
  -H "Content-Type: application/json" \
  -d '{"payment_id":"PAY-123","amount":149.97,"reason":"Test"}'
```

### Migration Issues

```bash
# Check migration status
./gradlew :store-backend:flywayInfo

# Validate migrations
./gradlew :store-backend:flywayValidate

# Apply pending migrations
./gradlew :store-backend:flywayMigrate

# Repair checksums (after file modification)
./gradlew :store-backend:flywayRepair

# Clean and rebuild (local only)
docker-compose down -v
docker-compose up -d
./gradlew :store-backend:flywayMigrate
```

---

## Implementation Checklist

### Database
- [ ] V5__create_payment_methods_table.sql migration
- [ ] V6__create_payments_table.sql migration
- [ ] V7__create_refunds_table.sql migration
- [ ] Test migrations on clean database
- [ ] Verify constraints and indexes

### Domain Layer
- [ ] PaymentStatus.java enum
- [ ] RefundStatus.java enum
- [ ] PaymentMethod.java entity
- [ ] Payment.java entity
- [ ] Refund.java entity
- [ ] PaymentRepository.java
- [ ] PaymentMethodRepository.java
- [ ] RefundRepository.java

### DTOs
- [ ] CreatePaymentRequest.java
- [ ] CreatePaymentResponse.java
- [ ] BpayInfoResponse.java
- [ ] RefundRequest.java
- [ ] RefundResponse.java
- [ ] PaymentWebhookEvent.java
- [ ] BankBpayRequest.java
- [ ] BankBpayResponse.java
- [ ] PaymentMapper.java utility

### Bank Service Integration
- [ ] BankServiceClient.java
- [ ] BankServiceConfig.java
- [ ] RestTemplate configuration
- [ ] Configuration in application-local.yml
- [ ] Webhook registration on startup

### Business Logic
- [ ] PaymentService.java
- [ ] createPayment() with BPAY generation
- [ ] getBpayInfo() with authorization
- [ ] handlePaymentWebhook()
- [ ] requestRefund()
- [ ] handleRefundWebhook()

### Exception Handling
- [ ] Custom exceptions (8 files)
- [ ] GlobalExceptionHandler updates

### REST API
- [ ] PaymentController.java
- [ ] WebhookController.java
- [ ] 3 payment endpoints with security
- [ ] 1 webhook endpoint

### Testing
- [ ] PaymentServiceTest.java (unit tests)
- [ ] PaymentControllerTest.java (integration tests)
- [ ] Mock BankServiceClient in tests

### RabbitMQ Integration (Future)
- [ ] RabbitMQ publisher configuration
- [ ] Email notification events
- [ ] Fulfillment trigger events
- [ ] Inventory rollback events

---

## Next Steps

After Payment API implementation:

1. **Webhook Integration**: Complete Bank and DeliveryCo webhook handling
2. **Order Status Updates**: Sync order status with payment/delivery events
3. **RabbitMQ Events**: Publish email and fulfillment events
4. **Email Service**: Implement async email dispatch
5. **Warehouse Fulfillment**: gRPC commit on payment confirmation
6. **Delivery Integration**: Create shipment requests after fulfillment
7. **Admin Dashboard**: Payment monitoring and refund management

---

**Related Documentation**:
- `ORDER_API_IMPLEMENTATION.md` - Order API implementation guide
- `FLYWAY_GUIDE.md` - Database migration details
- `AUTHENTICATION_FLOW.md` - JWT authentication
- `SYSTEM_INTERFACE_SPEC.md` - Complete API specifications
- `SYSTEM_ARCHITECTURE.md` - Microservices architecture overview
