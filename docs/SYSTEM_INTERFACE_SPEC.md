# System Interface Specification

**Version:** 1.0
**Last Updated:** October 16 2025
**Status:** Active

---

This document defines all public and internal interfaces for the Store System, including:

- RESTful APIs (Frontend ↔ Store Backend)
- gRPC messages (Store Backend ↔ Warehouse Service)
- RabbitMQ async messages (Store ↔ Email/Warehouse)
- Webhooks (Bank, DeliveryCo)

---

## 1. RESTful API (Frontend ↔ Store Backend)

### User Login - `POST /api/auth/login`

Request:

```json
{
  "username": "customer",
  "password": "COMP5348"
}
```

Response:

```json
{
  "accessToken": "jwt-token",
  "user": {
    "id": "u123",
    "name": "customer",
    "email": "customer@example.com"
  }
}
```

### Product List - `GET /api/products`

Response:

```json
[
  {
    "id": "p123",
    "name": "Wireless Mouse",
    "price": 49.99,
    "stock": 12,
    "image_url": "...",
    "published": true
  }
]
```

### Product - `GET /api/products/{id}`

Response:

```json
{
  "id": "p123",
  "name": "Wireless Mouse",
  "description": "Ergonomic and precise",
  "price": 49.99,
  "stock": 25,
  "image_url": "https://cdn.com/images/wireless-mouse.png",
  "published": true
}
```

### Create Order - `POST /api/orders`

Request:

```json
{
  "product_id": "p123",
  "quantity": 2,
  "user_id": "u123",
  "shipping_info": {
    "first_name": "John",
    "last_name": "Doe",
    "email": "john@example.com",
    "mobile_number": "0400000000",
    "address_line1": "123 Main St",
    "city": "Sydney",
    "postcode": "2007",
    "country": "Australia"
  }
}
```

Response:

```json
{
  "order_id": "ORD-2025-001",
  "status": "pending",
  "total": 99.98
}
```

### Get Order detail - `GET /api/orders/{id}`

Response:

```json
{
  "order_id": "ORD-2025-001",
  "status": "delivered",
  "total": 149.97,
  "created_at": "2025-10-16T12:00:00Z",
  "shipping_info": {
    "first_name": "John",
    "last_name": "Doe",
    "email": "john@example.com",
    "mobile_number": "0400000000",
    "address": {
      "line1": "123 Main St",
      "line2": "",
      "city": "Sydney",
      "suburb": "Ultimo",
      "postcode": "2007",
      "country": "Australia"
    }
  },
  "shipments": [
    {
      "shipment_id": "SHP-001",
      "status": "delivered",
      "warehouse_id": 1
    }
  ],
  "payment": {
    "payment_id": "PAY-001",
    "status": "completed",
    "method": "BPAY"
  }
}
```

### Get all orders for a user - `GET /api/users/{user_id}/orders`

Response:

```json
{
  "orders": [
    {
      "order_id": "ORD-2025-001",
      "status": "delivered",
      "total": 149.97,
      "created_at": "2025-10-16T12:00:00Z"
    },
    {
      "order_id": "ORD-2025-002",
      "status": "pending",
      "total": 59.99,
      "created_at": "2025-10-17T09:00:00Z"
    }
  ]
}
```

### Cancel Order - `POST /api/orders/{id}/cancel`

Request:

```json
{ "reason": "Changed my mind" }
```

Response:

```json
{
  "order_id": "ORD-2025-001",
  "status": "cancelled",
  "refund": true
}
```

### Create Payment - `POST /api/payments`

Request:

```json
{
  "order_id": "ORD-2025-001",
  "method": "BPAY"
}
```

Response:

```json
{
  "payment_id": "PAY-001",
  "status": "pending"
}
```

### Retrieve BPAY biller info - `GET /api/payments/{id}`

Response:

```json
{
  "biller_code": "93242",
  "reference_number": "BP-ORD-001",
  "amount": 149.97,
  "expires_at": "2025-10-20T12:00:00Z"
}
```

### POST /api/payments/{id}/refund

Request refund

Request:

```json
{
  "reason": "Order cancelled by customer"
}
```

Response:

```json
{
  "payment_id": "PAY-001",
  "status": "refunded",
  "refunded_at": "2025-10-18T12:00:00Z"
}
```

### Create BPAY - /bank/api/bpay

Request:

```json
{
  "account_id": 1,
  "order_id": "ORD-2025-001",
  "amount": 149.97
}
```

Response:

```json
{
  "biller_code": "93242",
  "reference_number": "BPAY-2025-001",
  "amount": 149.97,
  "expires_at": "2025-10-19T23:59:59Z"
}
```

## 2. gRPC Interface (Store Backend ↔ Warehouse Service)

```proto
CheckStock
rpc CheckStock(CheckStockRequest) returns (CheckStockResponse);

message CheckStockRequest {
  int64 product_id;
  int32 quantity;
}

message CheckStockResponse {
  bool available;
  int32 total_available;  // Total available quantity across all warehouses
}

ReserveStock
rpc ReserveStock(ReserveStockRequest) returns (ReserveStockResponse);

message ReserveStockRequest {
  int64 product_id;
  int32 quantity;
  int64 order_id;
}

message ReserveStockResponse {
  bool success;
  string message;  // Error message if failed
  repeated string reserved_from_warehouses;  // Warehouse IDs where stock was reserved
}

CommitStock
rpc CommitStock(CommitStockRequest) returns (CommitStockResponse);

message CommitStockRequest {
  int64 order_id;
}

message CommitStockResponse {
  bool success;
  repeated string warehouse_addresses;
  string message;
}

RollbackStock
rpc RollbackStock(RollbackStockRequest) returns (RollbackStockResponse);

message RollbackStockRequest {
  int64 order_id;
}

message RollbackStockResponse {
  bool rolled_back;
  string message;
}
```

## 3. RabbitMQ Message Interfaces

email.queue

```json
{
  "type": "ORDER_CONFIRMATION",
  "to": "user@example.com",
  "template": "order_confirmation",
  "params": {
    "orderId": "ORD-2025-001",
    "amount": 99.98
  },
  "event_id": "evt-email-ord-001",
  "timestamp": "2025-10-17T09:00:00Z"
}
```

fulfillment.queue

```json
{
  "order_id": "ORD-2025-001",
  "warehouse_ids": [1],
  "product_id": "p123",
  "quantity": 2,
  "event": "START_FULFILLMENT",
  "timestamp": "2025-10-17T10:00:00Z"
}
```

inventory.rollback.queue

```json
{
  "order_id": "ORD-2025-001",
  "warehouse_id": 1,
  "product_id": "p123",
  "amount": 2,
  "reason": "Order cancelled",
  "event_id": "evt-rollback-001",
  "timestamp": "2025-10-17T10:05:00Z"
}
```

warehouse.product-updates

```json
{
  "product_id": "p123",
  "name": "Wireless Mouse",
  "price": 49.99,
  "stock": 12,
  "published": true,
  "image_url": "...",
  "timestamp": "2025-10-17T09:00:00Z"
}
```

## 4. Webhook Interfaces

### Register Bank Webhook - `POST /bank/api/webhooks/register`

Request:

```json
{
  "event": "PAYMENT_STATUS_UPDATE",
  "callback_url": "https://store.com/api/webhooks/payment"
}
```

### POST /api/webhooks/payment

```json
{
  "order_id": "ORD-2025-001",
  "payment_id": "PAY-001",
  "status": "completed", ("BPAY_PAYMENT_COMPLETED", "REFUND_COMPLETED")
  "paid_at": "2025-10-17T11:00:00Z"
}

```

### Register DeliveryCo Webhook - `POST /deliveryCo/api/webhooks/register`

Request:

```json
{
  "event": "SHIPMENT_STATUS_UPDATE",
  "callback_url": "https://store.com/api/webhooks/delivery"
}
```

### POST /api/webhooks/delivery

Receives delivery status updates from DeliveryCo service.

Request:

```json
{
  "shipment_id": "SHP-001",
  "event": "DELIVERED",
  "timestamp": "2025-10-18T10:30:00Z"
}
```

**Event Types:**

- `SHIPMENT_CREATED` - Shipment created in DeliveryCo system
- `PROCESSING` - Shipment is being processed
- `PICKED_UP` - Shipment picked up from warehouse
- `IN_TRANSIT` - Shipment is in transit to customer
- `DELIVERED` - Shipment delivered to customer
- `LOST` - Shipment lost (triggers inventory rollback)

Response: `200 OK`

---

## 5. DeliveryCo Service Interfaces

### Request Shipment - `POST /deliveryCo/api/shipments`

Creates a new shipment request with DeliveryCo.

Request:

```json
{
  "orderId": "ORD-123",
  "warehouseAddress": "Warehouse, 123 Storage St, Sydney NSW 2000",
  "deliveryAddress": "123 Customer St, Sydney NSW 2000",
  "recipientName": "John Doe",
  "recipientPhone": "+61412345678",
  "recipientEmail": "john@example.com",
  "packageCount": 1,
  "declaredValue": 149.97
}
```

Response:

```json
{
  "shipmentId": "SHP-001",
  "trackingNumber": "TRACK-001",
  "carrier": "DeliveryCo",
  "estimatedDelivery": "2025-10-21T14:00:00Z",
  "status": "SHIPMENT_CREATED"
}
```

---

## 6. RabbitMQ Message Interfaces (Updated)

### email.queue

```json
{
  "type": "ORDER_CONFIRMATION",
  "to": "user@example.com",
  "template": "order_confirmation",
  "params": {
    "orderId": "ORD-2025-001",
    "amount": 99.98
  },
  "event_id": "evt-email-ord-001",
  "timestamp": "2025-10-17T09:00:00Z"
}
```

**Email Event Types:**

- `ORDER_CONFIRMATION` - After successful payment and stock commitment
- `PAYMENT_FAILED` - After failed payment
- `REFUND_CONFIRMATION` - When refund is requested
- `REFUND_SUCCESS` - When refund is completed
- `SHIPMENT_PICKED_UP` - When shipment picked up from warehouse
- `SHIPMENT_IN_TRANSIT` - When shipment is in transit
- `SHIPMENT_DELIVERED` - When shipment is delivered
- `SHIPMENT_LOST` - When shipment is lost

### inventory.rollback.queue

```json
{
  "orderId": 123,
  "productId": 456,
  "amount": 2,
  "reason": "Shipment lost",
  "eventId": "evt-rollback-001",
  "timestamp": "2025-10-17T10:05:00Z"
}
```

**Rollback Reasons:**

- `Order cancelled` - User or system cancelled order
- `Payment failed` - Payment was not successful
- `Shipment lost` - DeliveryCo reported shipment as lost

### warehouse.product-updates

```json
{
  "product_id": "p123",
  "name": "Wireless Mouse",
  "price": 49.99,
  "stock": 12,
  "published": true,
  "image_url": "...",
  "timestamp": "2025-10-17T09:00:00Z"
}
```
