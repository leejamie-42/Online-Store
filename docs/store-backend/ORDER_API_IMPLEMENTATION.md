# Order API Implementation Guide

**Version**: 1.0
**Last Updated**: October 27, 2025
**Status**: Implementation Ready

---

## Overview

Implement Order API in store-backend with Warehouse gRPC integration for stock validation and reservation.

### Objectives

1. Create orders with shipping information
2. Validate stock availability via Warehouse gRPC before order creation
3. Reserve inventory atomically during order creation
4. Retrieve order details and user order history
5. Enforce user authorization (users can only access own orders)

### Key Technologies

- **PostgreSQL** with Flyway migrations
- **Spring Data JPA** for ORM
- **gRPC** for Warehouse communication
- **Jakarta Bean Validation** for request validation
- **Spring Security** with JWT for authentication

---

## Implementation Phases

### Phase 1: Database Schema

**File**: `V4__create_orders_table.sql`

**Key Elements**:

- Orders table with foreign keys to users and products
- Shipping info fields matching frontend schema (first_name, last_name, email, mobile_number, address_line1, city, state, postcode, country)
- Order status enum: pending → processing → picked_up → delivering → delivered | cancelled
- Indexes on user_id, product_id, status, created_at for performance
- Constraints: quantity > 0, valid Australian postcode/state

**Commands**:

```bash
./gradlew :store-backend:flywayMigrate
./gradlew :store-backend:flywayInfo
```

---

### Phase 2: Domain Models

**Files**:

- `model/OrderStatus.java` - enum with transitions
- `model/Order.java` - entity matching migration

**Key Points**:

- @ManyToOne relationships to User and Product (LAZY fetch)
- Embedded shipping fields
- @PrePersist/@PreUpdate for timestamps
- `calculateTotal()` business method

---

### Phase 3: DTOs

**Package**: `dto/order/`

**Files**:

1. `ShippingInfoDto.java` - reusable nested DTO with validation
2. `CreateOrderRequest.java` - POST /api/orders request
3. `CreateOrderResponse.java` - order_id, status, total
4. `OrderDetailResponse.java` - GET /api/orders/{id}
5. `OrderSummaryResponse.java` - GET /api/orders list
6. `util/OrderMapper.java` - entity-to-DTO mapping

**Validation**: Match frontend checkout.schema.ts exactly

- Australian state validation (NSW, VIC, QLD, SA, WA, TAS, NT, ACT)
- Postcode: 4 digits
- Email, mobile number format validation

---

### Phase 4: Warehouse gRPC Integration

**Files**:

- `src/main/proto/warehouse.proto` - shared with Warehouse service
- `service/warehouse/WarehouseGrpcClient.java`

**gRPC Methods**:

1. `checkStock(productId, quantity)` → available + total_available
2. `reserveStock(productId, quantity, orderId)` → success + message + reserved_from_warehouses (repeated)
3. `rollbackStock(orderId)` → rolled_back + message (compensation on failure)
4. `commitStock(orderId)` → success + warehouse_addresses + message (mark as sold after payment)

**Multi-Warehouse Support**:

- Products can be sourced from multiple warehouses based on availability
- `reserved_from_warehouses` returns a list of warehouse IDs that contributed to the reservation
- Enables optimal inventory distribution and reduces single points of failure
- Supports scenarios where a single order requires stock from multiple locations

**Configuration** (`application-local.yml`):

```yaml
warehouse:
  grpc:
    host: localhost
    port: 9090
    timeout: 5000
```

**Dependencies** (`build.gradle`):

```gradle
implementation 'io.grpc:grpc-netty:1.59.0'
implementation 'io.grpc:grpc-protobuf:1.59.0'
implementation 'io.grpc:grpc-stub:1.59.0'
```

---

### Phase 5: Repository Layer

**File**: `repository/OrderRepository.java`

**Methods**:

- `findByUserIdOrderByCreatedAtDesc(userId)` - user's order history
- `findByIdAndUserId(id, userId)` - security: user owns order
- `findByStatus(status)` - admin/fulfillment queries
- `countByUserId(userId)`
- `existsByIdAndUserId(id, userId)`

---

### Phase 6: Business Logic

**File**: `service/OrderService.java`

**createOrder() Workflow**:

1. Validate: user_id matches authenticated user → throw UnauthorizedOrderAccessException if not
2. Fetch product → throw ProductNotFoundException if not found
3. Check stock via gRPC → throw InsufficientStockException if unavailable
4. Reserve stock via gRPC → throw StockReservationException if fails
5. Create Order entity (status=PENDING)
6. Save to database (transaction)
7. **On database failure**: Rollback stock reservation
8. Return CreateOrderResponse

**Security**: All methods validate user owns the order

---

### Phase 7: Exception Handling

**Files** (in `exception/` package):

- `InsufficientStockException` - 400 Bad Request
- `StockReservationException` - 500 Internal Server Error
- `OrderNotFoundException` - 404 Not Found
- `UnauthorizedOrderAccessException` - 403 Forbidden

**Update**: `GlobalExceptionHandler.java` with handlers for each exception

---

### Phase 8: REST Controller

**File**: `controller/OrderController.java`

**Endpoints**:

| Method | Path             | Description       | Auth     | Response    |
| ------ | ---------------- | ----------------- | -------- | ----------- |
| POST   | /api/orders      | Create order      | Required | 201 Created |
| GET    | /api/orders/{id} | Get order detail  | Required | 200 OK      |
| GET    | /api/orders      | Get user's orders | Required | 200 OK      |

**Example Request** (POST /api/orders):

```json
{
  "product_id": 1,
  "quantity": 2,
  "user_id": 1,
  "shipping_info": {
    "first_name": "John",
    "last_name": "Doe",
    "email": "john@example.com",
    "mobile_number": "0400000000",
    "address_line1": "123 Main St",
    "city": "Sydney",
    "state": "NSW",
    "postcode": "2000",
    "country": "Australia"
  }
}
```

**Example Response**:

```json
{
  "order_id": "ORD-001",
  "status": "PENDING",
  "total": 199.98
}
```

---

### Phase 9: Testing

**Unit Tests** (`OrderServiceTest.java`):

- Test successful order creation with stock validation
- Test unauthorized user attempt (user_id mismatch)
- Test product not found scenario
- Test insufficient stock exception
- Test stock rollback on database failure
- Test order retrieval with authorization
- Test user order history

**Integration Tests** (`OrderControllerTest.java`):

- Test full order creation flow with TestRestTemplate
- Test validation errors (invalid shipping info)
- Test authentication (401 Unauthorized)
- Test authorization (403 Forbidden)

**Mock gRPC Client** in tests using Mockito

---

## Order Creation Workflow Diagram

```
┌──────────┐     POST /api/orders      ┌────────────┐
│ Frontend │ ────────────────────────► │ Controller │
└──────────┘                            └─────┬──────┘
                                              │
                                              ▼
                                        ┌──────────┐
                                        │ Service  │
                                        └────┬─────┘
                                             │
           ┌─────────────────────────────────┼─────────────────────┐
           │                                 │                     │
           ▼                                 ▼                     ▼
    ┌────────────┐                   ┌──────────────┐      ┌──────────┐
    │  Product   │                   │  Warehouse   │      │  Order   │
    │ Repository │                   │  gRPC Client │      │Repository│
    └────────────┘                   └──────────────┘      └──────────┘
                                             │
                                      ┌──────┴──────┐
                                      │             │
                                      ▼             ▼
                              checkStock()   reserveStock()
                                      │             │
                                      └──────┬──────┘
                                             │
                                             ▼
                                    ┌────────────────┐
                                    │   Warehouse    │
                                    │    Service     │
                                    └────────────────┘
```

**Flow Steps**:

1. Controller receives authenticated request
2. Service validates user authorization
3. Service fetches product from database
4. Service checks stock via Warehouse gRPC
5. Service reserves stock via Warehouse gRPC
6. Service creates Order entity
7. Service saves order to database
8. If save fails → rollback stock reservation
9. Return success response

---

## Order Status State Machine

```
PENDING ──► PROCESSING ──► PICKED_UP ──► DELIVERING ──► DELIVERED
   │
   └────────────────────► CANCELLED ◄────────────────────────
```

**Transitions**:

- PENDING → PROCESSING: After payment confirmation
- PROCESSING → PICKED_UP: Warehouse fulfillment
- PICKED_UP → DELIVERING: Shipment initiated
- DELIVERING → DELIVERED: Delivery confirmed
- Any → CANCELLED: User cancellation

---

## Security Considerations

1. **JWT Authentication**: Required for all endpoints
2. **User Authorization**:
   - user_id in request must match authenticated user
   - Users can only view their own orders
   - Enforced at service layer, not just controller
3. **Input Validation**: Bean Validation on all DTOs
4. **gRPC Security**: Use TLS in production (plaintext for local dev)

---

## Troubleshooting

### Stock Reservation Fails

**Symptom**: StockReservationException

**Solutions**:

```bash
# Check warehouse service
# Verify gRPC configuration
grep "warehouse.grpc" application-local.yml

# Test gRPC connection
grpcurl -plaintext localhost:9090 list
```

### Validation Errors

**Common Issues**:

- Invalid state (must be NSW, VIC, QLD, SA, WA, TAS, NT, ACT)
- Postcode not exactly 4 digits
- Email/mobile format invalid

**Fix**: Ensure request matches validation rules in DTOs

### Migration Issues

```bash
# Check migration status
./gradlew :store-backend:flywayInfo

# Repair checksums
./gradlew :store-backend:flywayRepair

# Clean and re-migrate (local only)
docker-compose down -v
docker-compose up -d
./gradlew :store-backend:flywayMigrate
```

---

## Implementation Checklist

### Database

- [ ] V4\_\_create_orders_table.sql migration
- [ ] Test migration on clean database
- [ ] Verify constraints

### Domain Layer

- [ ] OrderStatus.java enum
- [ ] Order.java entity
- [ ] OrderRepository.java

### DTOs

- [ ] ShippingInfoDto.java
- [ ] CreateOrderRequest.java
- [ ] CreateOrderResponse.java
- [ ] OrderDetailResponse.java
- [ ] OrderSummaryResponse.java
- [ ] OrderMapper.java utility

### gRPC Integration

- [ ] warehouse.proto file
- [ ] WarehouseGrpcClient.java
- [ ] Configuration in application-local.yml
- [ ] gRPC dependencies in build.gradle

### Business Logic

- [ ] OrderService.java
- [ ] createOrder() with stock validation
- [ ] getOrder() with authorization
- [ ] getUserOrders()

### Exception Handling

- [ ] Custom exceptions (4 files)
- [ ] GlobalExceptionHandler updates

### REST API

- [ ] OrderController.java
- [ ] 3 endpoints with security

### Testing

- [ ] OrderServiceTest.java (unit tests)
- [ ] OrderControllerTest.java (integration tests)

---

## Next Steps

After Order API implementation:

1. **Payment API**: BPAY payment generation and management
2. **Webhooks**: Receive payment and delivery status updates
3. **Order Status Updates**: Update order based on external events
4. **Email Integration**: RabbitMQ for async email notifications

---

**For detailed code examples, see the full implementation guide sections above or refer to:**

- `FLYWAY_GUIDE.md` - Database migration details
- `AUTHENTICATION_FLOW.md` - JWT authentication
- `SYSTEM_INTERFACE_SPEC.md` - Complete API specifications
