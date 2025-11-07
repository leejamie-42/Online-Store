# DeliveryCo Integration Plan - Part 2

## Overview

Integrate DeliveryCo shipment service to handle the complete delivery lifecycle after payment success. This includes shipment request creation, delivery status tracking via webhooks, database persistence, and email notifications for delivery events.

**Dependencies**: Requires Part 1 (RabbitMQ Integration) to be completed for email notifications.

## Architecture

### Complete Order-Payment-Delivery Flow

```
Order Created → Payment Initiated → Payment Success Webhook
  ↓
  ├─ Update payment → COMPLETED
  ├─ Update order → PROCESSING
  ├─ Commit stock (gRPC - SYNC)
  │   └─ Returns: warehouseAddresses
  │
  ├─ Request shipment from DeliveryCo (REST - SYNC)
  │   └─ Returns: shipmentId
  │
  ├─ Save shipment to database
  ├─ Register delivery webhook with DeliveryCo
  └─ Send ORDER_CONFIRMATION email

Later: DeliveryCo Webhook → Update shipment status → Update order status → Send delivery email
```

### Delivery Status Transitions

```
PROCESSING → PICKED_UP → IN_TRANSIT → DELIVERED
                        └→ LOST (error case)
```

### Database Schema

**New Table: `shipments`**

```sql
shipments (
  id BIGSERIAL PRIMARY KEY,
  order_id BIGINT NOT NULL UNIQUE REFERENCES orders(id),
  shipment_id VARCHAR(100) NOT NULL UNIQUE,
  status VARCHAR(50) NOT NULL,
  carrier VARCHAR(100),
  tracking_number VARCHAR(100),
  estimated_delivery TIMESTAMP,
  actual_delivery TIMESTAMP,
  current_warehouse_id BIGINT,
  pickup_path TEXT,  -- Format: "WH-1,WH-2,WH-3" (tracks pickup journey)
  delivery_address TEXT,
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
)
```

**Design Decision**: `pickup_path` stores the warehouse pickup sequence (e.g., "WH-1->WH-2") to track multi-warehouse fulfillment journey. `current_warehouse_id` indicates the next warehouse to pick up from.

## Implementation Phases

### Phase 1: Database Schema and Migration

**Generate Migration using Gradle Task:**

Run the Gradle task to generate the migration file:

```bash
./gradlew generateMigration -Pname=create_shipments_table
```

This will create: `store-backend/src/main/resources/db/migration/Vxxxxxx__create_shipments_table.sql`

**File Content: `Vxxxxx__create_shipments_table.sql`**

```sql
-- V5__create_shipments_table.sql
-- Generated via: ./gradlew generateMigration -Pname=create_shipments_table
CREATE TABLE IF NOT EXISTS shipments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE,
    shipment_id VARCHAR(100) NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL,
    carrier VARCHAR(100),
    tracking_number VARCHAR(100),
    estimated_delivery TIMESTAMP,
    actual_delivery TIMESTAMP,
    current_warehouse_id BIGINT,
    pickup_path TEXT,  -- Format: "WH-1->WH-2->WH-3" (tracks multi-warehouse pickup journey)
    delivery_address TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),

    CONSTRAINT fk_shipments_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT chk_shipment_status CHECK (status IN ('PROCESSING', 'PICKED_UP', 'IN_TRANSIT', 'DELIVERED', 'LOST'))
);

CREATE INDEX idx_shipments_order_id ON shipments(order_id);
CREATE INDEX idx_shipments_shipment_id ON shipments(shipment_id);
CREATE INDEX idx_shipments_status ON shipments(status);
CREATE INDEX idx_shipments_current_warehouse ON shipments(current_warehouse_id);
CREATE INDEX idx_shipments_created_at ON shipments(created_at);

COMMENT ON TABLE shipments IS 'Tracks delivery shipments for orders via DeliveryCo service';
COMMENT ON COLUMN shipments.shipment_id IS 'External DeliveryCo shipment identifier';
COMMENT ON COLUMN shipments.status IS 'Current delivery status from DeliveryCo webhooks';
COMMENT ON COLUMN shipments.current_warehouse_id IS 'Next warehouse to pick up from (for multi-warehouse fulfillment)';
COMMENT ON COLUMN shipments.pickup_path IS 'Warehouse pickup journey path (e.g., "WH-1->WH-2->WH-3")';
```

**Note**: After editing the generated SQL file, run `./gradlew flywayMigrate` to apply the migration.

---

### Phase 2: Shipment Entity and Repository

**File: `store-backend/src/main/java/com/comp5348/store/model/shipment/Shipment.java`**

```java
package com.comp5348.store.model.shipment;

import com.comp5348.store.model.order.Order;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "shipments", indexes = {
    @Index(name = "idx_shipments_order_id", columnList = "order_id"),
    @Index(name = "idx_shipments_shipment_id", columnList = "shipment_id"),
    @Index(name = "idx_shipments_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(name = "shipment_id", nullable = false, unique = true, length = 100)
    private String shipmentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ShipmentStatus status;

    @Column(length = 100)
    private String carrier;

    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    @Column(name = "estimated_delivery")
    private LocalDateTime estimatedDelivery;

    @Column(name = "actual_delivery")
    private LocalDateTime actualDelivery;

    @Column(name = "warehouse_address", columnDefinition = "TEXT")
    private String warehouseAddress;

    @Column(name = "delivery_address", columnDefinition = "TEXT")
    private String deliveryAddress;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void updateStatus(ShipmentStatus newStatus) {
        this.status = newStatus;
        if (newStatus == ShipmentStatus.DELIVERED) {
            this.actualDelivery = LocalDateTime.now();
        }
    }
}
```

**File: `store-backend/src/main/java/com/comp5348/store/model/shipment/ShipmentStatus.java`**

```java
package com.comp5348.store.model.shipment;

public enum ShipmentStatus {
    PROCESSING,
    PICKED_UP,
    IN_TRANSIT,
    DELIVERED,
    LOST
}
```

**File: `store-backend/src/main/java/com/comp5348/store/repository/ShipmentRepository.java`**

```java
package com.comp5348.store.repository;

import com.comp5348.store.model.shipment.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    Optional<Shipment> findByShipmentId(String shipmentId);
    Optional<Shipment> findByOrderId(Long orderId);
    boolean existsByOrderId(Long orderId);
}
```

---

### Phase 3: DeliveryCo DTOs

**Directory: `store-backend/src/main/java/com/comp5348/store/dto/delivery/`**

**DeliveryRequest.java**

```java
package com.comp5348.store.dto.delivery;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryRequest {
    private String orderId;
    private String warehouseAddress;
    private String deliveryAddress;
    private String recipientName;
    private String recipientPhone;
    private String recipientEmail;
    private Integer packageCount;
    private BigDecimal declaredValue;
}
```

**DeliveryResponse.java**

```java
package com.comp5348.store.dto.delivery;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryResponse {
    private String shipmentId;
    private String trackingNumber;
    private String carrier;
    private LocalDateTime estimatedDelivery;
    private String status;
}
```

**DeliveryWebhookEvent.java**

```java
package com.comp5348.store.dto.delivery;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryWebhookEvent {
    private String shipmentId;
    private String event; // SHIPMENT_CREATED, PICKED_UP, IN_TRANSIT, DELIVERED, LOST
    private LocalDateTime timestamp;
}
```

---

### Phase 4: DeliveryCo REST Client

**File: `store-backend/src/main/java/com/comp5348/store/service/delivery/DeliveryCoClient.java`**

```java
package com.comp5348.store.service.delivery;

import com.comp5348.store.dto.delivery.*;
import com.comp5348.store.exception.DeliveryServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryCoClient {

    private final RestTemplate restTemplate;

    @Value("${deliveryco.service.base-url}")
    private String deliveryCoBaseUrl;

    /**
     * Request shipment from DeliveryCo.
     */
    public DeliveryResponse requestShipment(DeliveryRequest request) {
        String url = deliveryCoBaseUrl + "/deliveryCo/api/shipments";

        try {
            log.info("Requesting shipment for order {}", request.getOrderId());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<DeliveryRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<DeliveryResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                DeliveryResponse.class
            );

            DeliveryResponse deliveryResponse = response.getBody();
            log.info("Successfully created shipment for order {}: shipmentId={}",
                request.getOrderId(), deliveryResponse.getShipmentId());

            return deliveryResponse;

        } catch (RestClientException e) {
            log.error("Failed to request shipment for order {}: {}",
                request.getOrderId(), e.getMessage());
            throw new DeliveryServiceException("Failed to communicate with DeliveryCo", e);
        }
    }

    /**
     * Register webhook callback URL with DeliveryCo.
     */
    public void registerWebhook(String callbackUrl) {
        String url = deliveryCoBaseUrl + "/deliveryCo/api/webhooks/register";

        try {
            log.info("Registering delivery webhook: {}", callbackUrl);

            Map<String, String> request = Map.of(
                "event", "SHIPMENT_STATUS_UPDATE",
                "callback_url", callbackUrl
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

            restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);

            log.info("Successfully registered delivery webhook");

        } catch (RestClientException e) {
            log.error("Failed to register delivery webhook: {}", e.getMessage());
            // Don't throw - webhook registration failure shouldn't prevent app startup
        }
    }
}
```

---

### Phase 5: Shipment Service

**File: `store-backend/src/main/java/com/comp5348/store/service/ShipmentService.java`**

```java
package com.comp5348.store.service;

import com.comp5348.store.dto.delivery.*;
import com.comp5348.store.dto.event.EmailEvent;
import com.comp5348.store.exception.*;
import com.comp5348.store.model.order.Order;
import com.comp5348.store.model.order.OrderStatus;
import com.comp5348.store.model.shipment.*;
import com.comp5348.store.repository.*;
import com.comp5348.store.service.delivery.DeliveryCoClient;
import com.comp5348.store.service.event.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;
    private final DeliveryCoClient deliveryCoClient;
    private final EventPublisher eventPublisher;

    /**
     * Create shipment request to DeliveryCo after payment success.
     */
    @Transactional
    public Shipment requestShipment(Order order, String warehouseAddress) {
        // Validate order status
        if (order.getStatus() != OrderStatus.PROCESSING) {
            throw new IllegalStateException(
                "Cannot request shipment for order not in PROCESSING status");
        }

        // Check if shipment already exists
        if (shipmentRepository.existsByOrderId(order.getId())) {
            throw new IllegalStateException(
                "Shipment already exists for order " + order.getId());
        }

        // Build delivery request
        String deliveryAddress = formatDeliveryAddress(order);

        DeliveryRequest request = DeliveryRequest.builder()
            .orderId("ORD-" + order.getId())
            .warehouseAddress(warehouseAddress)
            .deliveryAddress(deliveryAddress)
            .recipientName(order.getFirstName() + " " + order.getLastName())
            .recipientPhone(order.getMobileNumber())
            .recipientEmail(order.getEmail())
            .packageCount(1)
            .declaredValue(order.getTotalAmount())
            .build();

        // Call DeliveryCo API
        DeliveryResponse response = deliveryCoClient.requestShipment(request);

        // Create shipment entity
        Shipment shipment = Shipment.builder()
            .order(order)
            .shipmentId(response.getShipmentId())
            .status(ShipmentStatus.SHIPMENT_CREATED)
            .carrier(response.getCarrier())
            .trackingNumber(response.getTrackingNumber())
            .estimatedDelivery(response.getEstimatedDelivery())
            .warehouseAddress(warehouseAddress)
            .deliveryAddress(deliveryAddress)
            .build();

        shipmentRepository.save(shipment);

        log.info("Created shipment {} for order {}", shipment.getShipmentId(), order.getId());

        return shipment;
    }

    /**
     * Handle delivery status webhook from DeliveryCo.
     */
    @Transactional
    public void handleDeliveryWebhook(DeliveryWebhookEvent event) {
        log.info("Handling delivery webhook: shipmentId={}, event={}",
            event.getShipmentId(), event.getEvent());

        // Find shipment
        Shipment shipment = shipmentRepository.findByShipmentId(event.getShipmentId())
            .orElseThrow(() -> new ShipmentNotFoundException(
                "Shipment not found: " + event.getShipmentId()));

        Order order = shipment.getOrder();

        // Map webhook event to shipment status
        ShipmentStatus newStatus = mapEventToStatus(event.getEvent());
        shipment.updateStatus(newStatus);
        shipmentRepository.save(shipment);

        // Update order status
        OrderStatus newOrderStatus = mapShipmentStatusToOrderStatus(newStatus);
        if (newOrderStatus != null) {
            order.setStatus(newOrderStatus);
            orderRepository.save(order);
        }

        // Send email notification
        sendDeliveryEmail(order, shipment, event.getEvent());

        log.info("Updated shipment {} to status {}", shipment.getShipmentId(), newStatus);
    }

    private ShipmentStatus mapEventToStatus(String event) {
        return switch (event) {
            case "SHIPMENT_CREATED" -> ShipmentStatus.SHIPMENT_CREATED;
            case "PICKED_UP" -> ShipmentStatus.PICKED_UP;
            case "IN_TRANSIT" -> ShipmentStatus.IN_TRANSIT;
            case "DELIVERED" -> ShipmentStatus.DELIVERED;
            case "LOST" -> ShipmentStatus.LOST;
            default -> throw new IllegalArgumentException("Unknown delivery event: " + event);
        };
    }

    private OrderStatus mapShipmentStatusToOrderStatus(ShipmentStatus shipmentStatus) {
        return switch (shipmentStatus) {
            case PICKED_UP -> OrderStatus.PICKED_UP;
            case IN_TRANSIT -> OrderStatus.DELIVERING;
            case DELIVERED -> OrderStatus.DELIVERED;
            case LOST -> OrderStatus.CANCELLED;
            default -> null; // No order status change
        };
    }

    private void sendDeliveryEmail(Order order, Shipment shipment, String event) {
        String emailType = mapEventToEmailType(event);
        if (emailType == null) return;

        EmailEvent emailEvent = EmailEvent.builder()
            .type(emailType)
            .to(order.getEmail())
            .template(emailType.toLowerCase())
            .params(Map.of(
                "orderId", "ORD-" + order.getId(),
                "shipmentId", shipment.getShipmentId(),
                "trackingNumber", shipment.getTrackingNumber(),
                "customerName", order.getFirstName() + " " + order.getLastName(),
                "estimatedDelivery", shipment.getEstimatedDelivery()
            ))
            .eventId("evt-delivery-" + shipment.getId())
            .timestamp(LocalDateTime.now())
            .build();

        eventPublisher.publishEmailEvent(emailEvent);
    }

    private String mapEventToEmailType(String event) {
        return switch (event) {
            case "PICKED_UP" -> "SHIPMENT_PICKED_UP";
            case "IN_TRANSIT" -> "SHIPMENT_IN_TRANSIT";
            case "DELIVERED" -> "SHIPMENT_DELIVERED";
            case "LOST" -> "SHIPMENT_LOST";
            default -> null;
        };
    }

    private String formatDeliveryAddress(Order order) {
        return String.format("%s, %s %s %s",
            order.getAddressLine1(),
            order.getCity(),
            order.getState(),
            order.getPostcode());
    }
}
```

---

### Phase 6: Update WebhookController

**Update: `store-backend/src/main/java/com/comp5348/store/controller/WebhookController.java`**

Add delivery webhook endpoint:

```java
@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final PaymentService paymentService;
    private final ShipmentService shipmentService; // ADD THIS

    // Existing payment webhook...

    /**
     * Receive delivery status events from DeliveryCo.
     */
    @Operation(
        summary = "Handle delivery webhook from DeliveryCo",
        description = "Process shipment status updates from DeliveryCo service.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Webhook processed successfully"),
        @ApiResponse(responseCode = "404", description = "Shipment not found")
    })
    @PostMapping("/delivery")
    public ResponseEntity<Void> handleDeliveryWebhook(@RequestBody DeliveryWebhookEvent event) {
        log.info("Received delivery webhook: shipmentId={}, event={}",
            event.getShipmentId(), event.getEvent());

        try {
            shipmentService.handleDeliveryWebhook(event);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to process delivery webhook: {}", e.getMessage());
            return ResponseEntity.ok().build(); // Prevent retries
        }
    }
}
```

---

### Phase 7: Integrate into PaymentService

**Update: `store-backend/src/main/java/com/comp5348/store/service/PaymentService.java`**

Replace TODO comment with shipment request:

```java
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final ShipmentService shipmentService; // ADD THIS
    // ... other dependencies ...

    private void handlePaymentSuccess(PaymentWebhookEvent event) {
        // ... existing code: update payment, order, commitStock ...

        // Request shipment from DeliveryCo
        try {
            String warehouseAddress = "123 Warehouse St, Sydney NSW 2000"; // TODO: Get from commitStock response
            shipmentService.requestShipment(order, warehouseAddress);
            log.info("Successfully requested shipment for order {}", order.getId());
        } catch (Exception e) {
            log.error("Failed to request shipment for order {}: {}",
                order.getId(), e.getMessage(), e);
            // Don't throw - shipment can be retried manually
        }

        // Publish ORDER_CONFIRMATION email (existing code)
        publishOrderConfirmationEmail(order, payment);
    }
}
```

---

### Phase 8: Configuration

**Update: `store-backend/src/main/resources/application-local.yml`**

Add DeliveryCo configuration:

```yaml
# --- DeliveryCo Service Configuration ---
deliveryco:
  service:
    base-url: ${DELIVERYCO_BASE_URL:http://localhost:8084}
```

**Update: `store-backend/.env.example` (if exists)**

```bash
# DeliveryCo Service Configuration
DELIVERYCO_BASE_URL=http://localhost:8083
```

---

### Phase 9: Exception Handling

**File: `store-backend/src/main/java/com/comp5348/store/exception/DeliveryServiceException.java`**

```java
package com.comp5348.store.exception;

public class DeliveryServiceException extends RuntimeException {
    public DeliveryServiceException(String message) {
        super(message);
    }

    public DeliveryServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

**File: `store-backend/src/main/java/com/comp5348/store/exception/ShipmentNotFoundException.java`**

```java
package com.comp5348.store.exception;

public class ShipmentNotFoundException extends RuntimeException {
    public ShipmentNotFoundException(String message) {
        super(message);
    }
}
```

**Update: `store-backend/src/main/java/com/comp5348/store/exception/GlobalExceptionHandler.java`**

Add handlers for new exceptions:

```java
@ExceptionHandler(DeliveryServiceException.class)
public ResponseEntity<ErrorResponse> handleDeliveryServiceException(DeliveryServiceException ex) {
    log.error("Delivery service error: {}", ex.getMessage());
    return ResponseEntity
        .status(HttpStatus.SERVICE_UNAVAILABLE)
        .body(new ErrorResponse("DELIVERY_SERVICE_ERROR", ex.getMessage()));
}

@ExceptionHandler(ShipmentNotFoundException.class)
public ResponseEntity<ErrorResponse> handleShipmentNotFoundException(ShipmentNotFoundException ex) {
    log.error("Shipment not found: {}", ex.getMessage());
    return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(new ErrorResponse("SHIPMENT_NOT_FOUND", ex.getMessage()));
}
```

---

### Phase 10: Update Email Events

**Update: `store-backend/src/main/java/com/comp5348/store/dto/event/EmailEvent.java`**

Add delivery email types to documentation:

```java
/**
 * Email event for RabbitMQ messaging.
 *
 * Event Types:
 * - ORDER_CONFIRMATION - After successful payment and stock commitment
 * - PAYMENT_FAILED - After failed payment
 * - REFUND_CONFIRMATION - When refund is requested
 * - REFUND_SUCCESS - When refund is completed
 * - SHIPMENT_PICKED_UP - When shipment picked up from warehouse
 * - SHIPMENT_IN_TRANSIT - When shipment is in transit
 * - SHIPMENT_DELIVERED - When shipment is delivered
 * - SHIPMENT_LOST - When shipment is lost
 */
```

---

### Phase 11: Testing

**File: `store-backend/src/test/java/com/comp5348/store/service/ShipmentServiceTest.java`**

Unit tests for shipment service:

```java
package com.comp5348.store.service;

import com.comp5348.store.dto.delivery.*;
import com.comp5348.store.model.order.Order;
import com.comp5348.store.model.order.OrderStatus;
import com.comp5348.store.model.shipment.*;
import com.comp5348.store.repository.*;
import com.comp5348.store.service.delivery.DeliveryCoClient;
import com.comp5348.store.service.event.EventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private DeliveryCoClient deliveryCoClient;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private ShipmentService shipmentService;

    @Test
    void requestShipment_Success() {
        // Given
        Order order = Order.builder()
            .id(1L)
            .status(OrderStatus.PROCESSING)
            .firstName("John")
            .lastName("Doe")
            .email("john@example.com")
            .build();

        DeliveryResponse deliveryResponse = DeliveryResponse.builder()
            .shipmentId("SHP-001")
            .trackingNumber("TRACK-001")
            .carrier("DeliveryCo")
            .estimatedDelivery(LocalDateTime.now().plusDays(3))
            .build();

        when(shipmentRepository.existsByOrderId(1L)).thenReturn(false);
        when(deliveryCoClient.requestShipment(any())).thenReturn(deliveryResponse);

        // When
        Shipment shipment = shipmentService.requestShipment(order, "Warehouse Address");

        // Then
        assertNotNull(shipment);
        assertEquals("SHP-001", shipment.getShipmentId());
        verify(shipmentRepository).save(any(Shipment.class));
    }

    @Test
    void handleDeliveryWebhook_UpdatesStatusAndSendsEmail() {
        // Given
        DeliveryWebhookEvent event = DeliveryWebhookEvent.builder()
            .shipmentId("SHP-001")
            .event("PICKED_UP")
            .timestamp(LocalDateTime.now())
            .build();

        Order order = Order.builder().id(1L).email("john@example.com").build();
        Shipment shipment = Shipment.builder()
            .id(1L)
            .order(order)
            .shipmentId("SHP-001")
            .status(ShipmentStatus.SHIPMENT_CREATED)
            .build();

        when(shipmentRepository.findByShipmentId("SHP-001")).thenReturn(Optional.of(shipment));

        // When
        shipmentService.handleDeliveryWebhook(event);

        // Then
        assertEquals(ShipmentStatus.PICKED_UP, shipment.getStatus());
        verify(shipmentRepository).save(shipment);
        verify(orderRepository).save(order);
        verify(eventPublisher).publishEmailEvent(any());
    }
}
```

---

### Phase 12: Documentation

**File: `docs/store-backend/DELIVERYCO_INTEGRATION.md`**

Create comprehensive documentation with:

- Architecture overview
- API endpoints and request/response schemas
- Webhook handling flow
- Database schema and relationships
- Email notification types
- Error handling and retry strategy
- Testing guide
- Deployment checklist

## Key Design Decisions

### Why SYNC REST for Shipment Request?

- Immediate feedback on shipment creation
- Transaction consistency (payment → stock → shipment)
- Simpler error handling

### Why Separate Shipment Entity?

- Decouples delivery tracking from order lifecycle
- Allows multiple shipment attempts
- Stores historical tracking data

### Webhook Idempotency

- Shipment status updates are idempotent
- Always returns 200 OK to prevent retries
- Log failures for manual investigation

## Implementation Checklist

- [ ] Create Flyway migration V5 for shipments table
- [ ] Create Shipment entity, ShipmentStatus enum, ShipmentRepository
- [ ] Create DeliveryCo DTOs (DeliveryRequest, DeliveryResponse, DeliveryWebhookEvent)
- [ ] Implement DeliveryCoClient for REST API calls
- [ ] Implement ShipmentService with requestShipment and handleDeliveryWebhook
- [ ] Add delivery webhook endpoint to WebhookController
- [ ] Integrate shipment request into PaymentService.handlePaymentSuccess()
- [ ] Add DeliveryCo configuration to application-local.yml
- [ ] Create DeliveryServiceException and ShipmentNotFoundException
- [ ] Update GlobalExceptionHandler with new exception handlers
- [ ] Update EmailEvent documentation with delivery email types
- [ ] Write unit tests for ShipmentService
- [ ] Create DELIVERYCO_INTEGRATION.md documentation
- [ ] Test end-to-end delivery flow
- [ ] Test webhook handling and email notifications

## Testing Strategy

1. Unit Tests: Mock DeliveryCoClient, verify shipment creation and webhook handling
2. Integration Tests: Use WireMock for DeliveryCo API simulation
3. Manual Testing: Use Postman to trigger webhooks
4. E2E Testing: Full order flow from payment to delivery

## Next Steps

After Part 2 implementation:

- Implement retry mechanism for failed shipments
- Add shipment cancellation support
- Implement admin dashboard for shipment tracking
- Add real-time tracking updates via WebSocket
