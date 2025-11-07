# RabbitMQ Integration - Store Backend

## Overview

This document describes the RabbitMQ messaging infrastructure integrated into the Store Backend service for asynchronous operations.

**Integration Scope (Part 1):**

- Email notifications for order and payment events
- Optional inventory rollback queue (async alternative to gRPC)
- Product catalog synchronization from Warehouse service

**Out of Scope (Part 2 - Separate Implementation):**

- DeliveryCo REST API integration for shipment management
- Delivery tracking webhooks and email notifications

## Architecture

### Message Flows

```
┌─────────────────────────────────────────────────────────────────┐
│                     RabbitMQ Architecture                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌──────────────────┐      ┌──────────────────────────┐         │
│  │  Store Backend   │─────▶│  store.topic.exchange    │         │
│  │  (Publisher)     │      │  (Topic Exchange)        │         │
│  └──────────────────┘      └──────────────────────────┘         │
│                                      │                            │
│                        ┌─────────────┼─────────────┐             │
│                        │             │             │             │
│                 email.*│    inventory.rollback.*   │product.*    │
│                        │             │             │             │
│                ┌───────▼───┐  ┌──────▼──────┐  ┌──▼──────┐     │
│                │email.queue│  │inventory.   │  │product. │     │
│                │           │  │rollback.    │  │updates. │     │
│                │ + DLQ     │  │queue + DLQ  │  │queue    │     │
│                └─────┬─────┘  └──────┬──────┘  └────┬────┘     │
│                      │               │              │           │
│                      ▼               ▼              ▼           │
│              ┌─────────────┐  ┌───────────┐  ┌──────────┐     │
│              │Email Service│  │Warehouse  │  │Store     │     │
│              │(Consumer)   │  │Service    │  │Backend   │     │
│              └─────────────┘  └───────────┘  └──────────┘     │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Exchange Strategy

**Topic Exchange:** `store.topic.exchange`

- Flexible routing using pattern matching
- Supports multiple consumers per message type
- Routing keys:
  - `email.*` - All email notifications
  - `inventory.rollback.*` - Inventory rollback events
  - `product.update.*` - Product synchronization events

**Dead Letter Exchange:** `store.dlx.exchange`

- Handles failed messages that exhaust retry attempts
- Provides manual inspection and recovery capabilities
- Separate DLQs for email and inventory operations

### Queue Topology

| Queue                      | Routing Key            | Publisher         | Consumer          | DLQ | Purpose                                        |
| -------------------------- | ---------------------- | ----------------- | ----------------- | --- | ---------------------------------------------- |
| `email.queue`              | `email.*`              | Store Backend     | Email Service     | ✓   | Order confirmations, payment receipts, refunds |
| `inventory.rollback.queue` | `inventory.rollback.*` | Store Backend     | Warehouse Service | ✓   | Async stock rollback (optional)                |
| `product.updates.queue`    | `product.update.*`     | Warehouse Service | Store Backend     | ✗   | Real-time product catalog sync                 |

## Event Schemas

### EmailEvent

Published when user-facing notifications need to be sent.

```json
{
  "type": "ORDER_CONFIRMATION",
  "to": "customer@example.com",
  "template": "order_confirmation",
  "params": {
    "orderId": 123,
    "orderNumber": "ORD-123",
    "total": 99.99,
    "customerName": "John Doe",
    "productName": "Wireless Mouse",
    "quantity": 2
  },
  "eventId": "evt-order-123",
  "timestamp": "2025-10-28T10:30:00"
}
```

**Event Types:**

- `ORDER_CONFIRMATION` - After successful payment and stock commitment
- `PAYMENT_FAILED` - After failed payment processing
- `REFUND_CONFIRMATION` - When refund is requested by customer/admin
- `REFUND_SUCCESS` - When refund is completed by bank (webhook confirmation)

**Routing:** Published to `email.<type>` (e.g., `email.order_confirmation`)

### InventoryRollbackEvent

Optional async alternative to gRPC `rollbackStock()` calls.

```json
{
  "orderId": 123,
  "productId": 456,
  "amount": 2,
  "reason": "Order cancelled",
  "eventId": "evt-rollback-123",
  "timestamp": "2025-10-28T10:35:00"
}
```

**Note:** In most cases, synchronous gRPC `rollbackStock()` is preferred for immediate consistency. Use this only for non-critical rollback scenarios where eventual consistency is acceptable.

**Routing:** Published to `inventory.rollback.request`

### ProductUpdateEvent

Published by Warehouse service when product data changes.

```json
{
  "productId": 789,
  "name": "Wireless Mouse",
  "price": 49.99,
  "stock": 150,
  "published": true,
  "imageUrl": "https://cdn.example.com/products/mouse.jpg",
  "timestamp": "2025-10-28T09:00:00"
}
```

**Routing:** Published to `product.update.*`

## Complete Workflow

### Order and Payment Flow with Email Events

```
┌─────────────────────────────────────────────────────────────────────┐
│                   Order Creation Flow                                │
├─────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  1. User creates order                                               │
│     └─> OrderService.createOrder()                                  │
│                                                                       │
│  2. Reserve stock (SYNC gRPC)                                       │
│     └─> warehouseGrpcClient.reserveStock()                          │
│                                                                       │
│  3. Order created with PENDING status                               │
│     └─> Payment initiated (redirect to Bank Service)                │
│                                                                       │
│  ❌ NO EMAIL SENT AT ORDER CREATION                                 │
│                                                                       │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                   Payment Success Flow                               │
├─────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  1. Bank Service webhook → BPAY_PAYMENT_COMPLETED                   │
│     └─> WebhookController.handleWebhook()                          │
│                                                                       │
│  2. Update payment status → COMPLETED                                │
│     └─> PaymentService.handlePaymentSuccess()                       │
│                                                                       │
│  3. Update order status → PROCESSING                                 │
│                                                                       │
│  4. Commit stock (SYNC gRPC)                                        │
│     └─> warehouseGrpcClient.commitStock()                           │
│     └─> Returns warehouse addresses                                 │
│                                                                       │
│  5. ✅ Publish ORDER_CONFIRMATION email                             │
│     └─> eventPublisher.publishEmailEvent()                          │
│     └─> RabbitMQ: email.order_confirmation                          │
│                                                                       │
│  6. Email Service consumes and sends email                          │
│                                                                       │
│  [Part 2] Request shipment from DeliveryCo                          │
│                                                                       │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                   Payment Failure Flow                               │
├─────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  1. Bank Service webhook → BPAY_PAYMENT_FAILED                      │
│     └─> WebhookController.handleWebhook()                          │
│                                                                       │
│  2. Update payment status → FAILED                                   │
│     └─> PaymentService.handlePaymentFailure()                       │
│                                                                       │
│  3. Update order status → CANCELLED                                  │
│                                                                       │
│  4. Rollback reserved stock (SYNC gRPC)                             │
│     └─> warehouseGrpcClient.rollbackStock()                         │
│                                                                       │
│  5. ✅ Publish PAYMENT_FAILED email                                 │
│     └─> eventPublisher.publishEmailEvent()                          │
│     └─> RabbitMQ: email.payment_failed                              │
│                                                                       │
│  6. Email Service consumes and sends email                          │
│                                                                       │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                   Refund Request Flow                                │
├─────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  1. Admin/Customer requests refund                                   │
│     └─> PaymentService.requestRefund()                              │
│                                                                       │
│  2. Initiate refund with Bank Service                               │
│     └─> BankServiceClient.initiateRefund()                          │
│                                                                       │
│  3. Refund status → REQUESTED                                        │
│                                                                       │
│  4. ✅ Publish REFUND_CONFIRMATION email                            │
│     └─> eventPublisher.publishEmailEvent()                          │
│     └─> RabbitMQ: email.refund_confirmation                         │
│                                                                       │
│  5. Email Service sends refund request confirmation                 │
│                                                                       │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                   Refund Completion Flow                             │
├─────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  1. Bank Service webhook → REFUND_COMPLETED                         │
│     └─> WebhookController.handleWebhook()                          │
│                                                                       │
│  2. Update refund status → COMPLETED                                 │
│     └─> PaymentService.handleRefundWebhook()                        │
│                                                                       │
│  3. ✅ Publish REFUND_SUCCESS email                                 │
│     └─> eventPublisher.publishEmailEvent()                          │
│     └─> RabbitMQ: email.refund_success                              │
│                                                                       │
│  4. Email Service sends refund completion notification              │
│                                                                       │
└─────────────────────────────────────────────────────────────────────┘
```

**Key Points:**

- ✅ **ORDER_CONFIRMATION** is sent ONLY after payment success and stock commitment, not at order creation
- ✅ **Stock operations use SYNC gRPC**, not RabbitMQ, for immediate consistency
- ✅ **Email publishing is non-blocking** - failures don't break the main transaction
- ✅ **All emails have retry and DLQ** for reliability

## Implementation Details

### Publisher Integration Points

**PaymentService.java**

- Publishes `ORDER_CONFIRMATION` email after successful payment and stock commitment (in `handlePaymentSuccess()`)
- Publishes `PAYMENT_FAILED` email after payment failure (in `handlePaymentFailure()`)
- Publishes `REFUND_CONFIRMATION` email when refund is requested (in `requestRefund()`)
- Publishes `REFUND_SUCCESS` email when refund is completed (in `handleRefundWebhook()`)
- Integrates with gRPC `commitStock()` and `rollbackStock()` for synchronous inventory operations

### Consumer Implementation

**ProductUpdateConsumer.java**

- Listens to `product.updates.queue`
- Synchronizes product catalog from Warehouse (source of truth)
- Creates new products if they don't exist locally
- Updates name, price, stock (quantity), and image URL
- Transactional processing with rollback on failure

### Error Handling Strategy

**Publisher Side:**

- All `publishEmailEvent()` calls catch exceptions and log them
- Email publishing failures don't break main business transactions
- Retry handled automatically by RabbitMQ template configuration

**Consumer Side:**

- Failed messages are automatically retried (3 attempts with exponential backoff)
- After retry exhaustion, messages go to Dead Letter Queue (DLQ)
- DLQ messages require manual inspection and reprocessing
- `ListenerExecutionFailedException` prevents infinite retry loops

**Retry Configuration:**

- Initial interval: 3000ms (3 seconds)
- Max attempts: 3
- Backoff multiplier: 2 (3s, 6s, 12s)

## Design Decisions

### Why RabbitMQ for Email?

✅ **Benefits:**

- **Non-blocking:** Email sending doesn't delay order/payment transactions
- **Reliability:** DLQ ensures no lost notifications
- **Scalability:** Email service scales independently
- **Resilience:** Retries handle transient failures

### Why Sync gRPC for Stock Operations?

✅ **Benefits:**

- **Immediate consistency:** Inventory accuracy is critical
- **Transactional atomicity:** Payment → commit must be atomic
- **Simpler error handling:** No async compensation needed
- **Real-time validation:** Stock checks happen instantly

❌ **RabbitMQ async would be problematic:**

- Race conditions between reserve and commit
- Eventual consistency not acceptable for payments
- Complex compensation flows

### Product Updates via RabbitMQ?

✅ **Benefits:**

- **Warehouse is source of truth:** Store subscribes to changes
- **Real-time sync:** Product catalog stays current without polling
- **Decoupled services:** Store doesn't need direct Warehouse API access

## Configuration

### application-local.yml

```yaml
spring:
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USERNAME:guest}
    password: ${RABBITMQ_PASSWORD:guest}
    virtual-host: ${RABBITMQ_VHOST:/}
    listener:
      simple:
        acknowledge-mode: auto
        retry:
          enabled: true
          initial-interval: 3000
          max-attempts: 3
          multiplier: 2
    template:
      retry:
        enabled: true
        initial-interval: 1000
        max-attempts: 3
```

### docker-compose.yml

```yaml
rabbitmq:
  image: rabbitmq:3.13-management-alpine
  container_name: store-rabbitmq
  restart: unless-stopped
  ports:
    - "5672:5672" # AMQP port
    - "15672:15672" # Management UI port
  environment:
    RABBITMQ_DEFAULT_USER: ${RABBITMQ_USERNAME:-guest}
    RABBITMQ_DEFAULT_PASS: ${RABBITMQ_PASSWORD:-guest}
  volumes:
    - rabbitmq_data:/var/lib/rabbitmq
  healthcheck:
    test: ["CMD", "rabbitmq-diagnostics", "-q", "ping"]
    interval: 10s
    timeout: 5s
    retries: 5
```

## Testing

### Unit Tests

**EventPublisherTest.java**

- Verifies correct routing keys for each event type
- Tests exception handling (no exceptions thrown)
- Mocks RabbitTemplate to avoid real broker

**ProductUpdateConsumerTest.java**

- Tests existing product updates
- Tests new product creation
- Tests exception handling and DLQ behavior

### Integration Testing with TestContainers

```java
@Testcontainers
class RabbitMQIntegrationTest {

    @Container
    static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:3.13-management-alpine");

    @Test
    void publishAndConsumeEmailEvent() {
        // Test end-to-end message flow
    }
}
```

### Manual Testing

**RabbitMQ Management UI:** http://localhost:15672

- Username: `guest`
- Password: `guest`

**Steps:**

1. Create order and trigger payment success webhook → Check `email.queue` for ORDER_CONFIRMATION event
2. Trigger payment failed webhook → Check `email.queue` for PAYMENT_FAILED event
3. Inspect message bodies and routing keys
4. Monitor DLQs for failed messages

## Monitoring

### Health Check

RabbitMQ health is exposed via Spring Boot Actuator:

**Endpoint:** `GET /actuator/health`

```json
{
  "status": "UP",
  "components": {
    "rabbitmq": {
      "status": "UP",
      "details": {
        "rabbitmq": "Available",
        "host": "localhost",
        "port": 5672
      }
    }
  }
}
```

### Metrics

Spring Boot Actuator automatically exposes RabbitMQ metrics:

- Connection pool stats
- Message publish/consume rates
- Error rates
- Queue depths

**Endpoint:** `GET /actuator/metrics/rabbitmq.*`

## Troubleshooting

### Common Issues

**Issue:** Messages stuck in DLQ

- **Cause:** Consumer threw `ListenerExecutionFailedException` after 3 retries
- **Solution:** Inspect DLQ messages in Management UI, fix data issue, manually republish

**Issue:** Email events not consumed

- **Cause:** Email Service not running or not subscribed
- **Solution:** Start Email Service, verify queue binding in Management UI

**Issue:** Connection refused

- **Cause:** RabbitMQ container not running
- **Solution:** `docker-compose up rabbitmq`, check health: `docker ps`

**Issue:** Messages lost after publish

- **Cause:** Non-durable queue or exchange
- **Solution:** Check `RabbitMQConfig` - all queues/exchanges are durable

### Debug Logging

Enable RabbitMQ debug logging:

```yaml
logging:
  level:
    org.springframework.amqp: DEBUG
    com.comp5348.store.service.event: DEBUG
```

## Next Steps (Part 2)

**DeliveryCo Integration** (Out of scope for RabbitMQ Part 1):

1. REST client for shipment creation
2. Webhook handler for delivery status updates
3. Database schema for shipment tracking
4. Order status state machine enhancements
5. Email notifications for delivery events (PICKED_UP, IN_TRANSIT, DELIVERED)

## References

- [RabbitMQ Official Documentation](https://www.rabbitmq.com/documentation.html)
- [Spring AMQP Documentation](https://docs.spring.io/spring-amqp/reference/)
- [System Interface Specification](../SYSTEM_INTERFACE_SPEC.md)
- [Payment API Implementation](./PAYMENT_API_IMPLEMENTATION.md)
- [Order API Implementation](./ORDER_API_IMPLEMENTATION.md)
