# 🧱 System Architecture Documentation

## 🔧 System Name  
**Store Platform — Distributed Microservices Architecture**

---

## 🧭 1. Overview

This system represents an **e-commerce platform** built with **microservices**, following an event-driven and modular design.

It supports:

- Product browsing and ordering
- BPAY payment processing
- Warehouse fulfillment and inventory tracking
- External shipment (DeliveryCo)
- Notification via email

---

## 🧩 2. High-Level Architecture

**Components:**

| Component         | Description                                                      |
| ----------------- | ---------------------------------------------------------------- |
| Client (Frontend) | User interface (mockups provided) to interact with store system  |
| Store Backend     | Core business logic: orders, payments, product listing, etc.     |
| Warehouse Service | Inventory and fulfillment logic with version-controlled stock    |
| Bank Service      | External-style BPAY simulation with webhook callback support     |
| DeliveryCo        | Simulated external shipment service with webhook integration     |
| Email Service     | Internal message-based email dispatcher (via RabbitMQ + Mailgun) |
| RabbitMQ          | Message broker connecting internal asynchronous workflows        |
| gRPC Layer        | RPC between Store ↔ Warehouse for atomic inventory updates      |

---

## 3. Communication Overview

| From          | To            | Protocol | Purpose                                     |
| ------------- | ------------- | -------- | ------------------------------------------- |
| Frontend      | Store Backend | REST     | Order, login, products                      |
| Store Backend | Warehouse     | gRPC     | Reserve/commit/rollback stock               |
| Store Backend | Bank          | REST     | Generate BPAY, Register Webhook             |
| Bank          | Store Backend | Webhook  | Notify on payment/refund events             |
| Store Backend | DeliveryCo    | REST     | Request shipment, register webhook          |
| DeliveryCo    | Store Backend | Webhook  | Send delivery status updates                |
| Store Backend | RabbitMQ      | AMQP     | Publish email, fulfillment, rollback events |
| RabbitMQ      | Email Service | Consumer | Async email handling                        |

---

## 4. Store Backend Responsibilities

- Product listing (synced from Warehouse)
- Order placement
- Payment initiation
- Delivery request orchestration
- Event publishing:
  - Email notification
  - Fulfillment trigger
  - Inventory rollback

---

## 5. Warehouse Service Responsibilities

- Acts as source of truth for products and inventory
- Exposes gRPC endpoints:
  - `CheckStock`
  - `ReserveStock`
  - `CommitStock`
  - `RollbackStock`
- Listens to RabbitMQ `inventory.rollback.queue`
- Publishes product sync updates to `warehouse.product-updates`

---

## 6. Bank Service Responsibilities

- Generates BPAY payment instructions
- Simulates payment lifecycle (paid/refunded/failed)
- Calls back Store via webhook when payment status changes
- Supports dynamic webhook registration:
  - `POST /bank/api/webhooks/register`
- Uses unified webhook: `PAYMENT_EVENT` with `type` in payload

---

## 7. DeliveryCo Responsibilities

- Simulates shipment status lifecycle
- Accepts webhook registration:
  - `POST /delivery/api/webhooks/register`
- Sends webhook to Store:
  - `POST /api/webhooks/delivery`
  - With `status`: `PICKED_UP`, `IN_TRANSIT`, `DELIVERED`, etc.

---

## 8. Email Service Responsibilities

- Consumes from `email.queue`
- Sends order/shipping-related emails
- Internal only (no REST API)
- Powered by RabbitMQ and external mail API (e.g., Mailgun)

---

## 9. RabbitMQ Topics / Queues

| Queue                       | Publisher     | Subscriber    | Message Type                  |
| --------------------------- | ------------- | ------------- | ----------------------------- |
| `email.queue`               | Store Backend | Email Service | Order/shipment notifications  |
| `fulfillment.queue`         | Store Backend | Warehouse     | Order commit trigger          |
| `inventory.rollback.queue`  | Store Backend | Warehouse     | Rollback instruction          |
| `warehouse.product-updates` | Warehouse     | Store Backend | Product stock/visibility sync |

**Email Event Types:**
- Order confirmation (after payment success)
- Payment failed notification
- Refund confirmations
- Shipment status updates (picked up, in transit, delivered, lost)

**Inventory Rollback Triggers:**
- Order cancellation
- Payment failure
- Lost shipment (from DeliveryCo webhook)

---

## 10. Webhook Contracts

### Payment Webhook

**URL:** `/api/webhooks/payment`

**Payload Example:**

```json
{
  "type": "BPAY_PAYMENT_COMPLETED",
  "order_id": "ORD-001",
  "payment_id": "PAY-123",
  "amount": 149.97,
  "paid_at": "2025-10-21T11:00:00Z"
}
```

### Delivery Webhook

**URL:** `/api/webhooks/delivery`

**Payload Example:**

```json
{
  "shipment_id": "SHP-001",
  "status": "DELIVERED",
  "timestamp": "2025-10-22T10:00:00Z"
}
```

## Summary

This microservice system follows a clean separation of concerns with:

- Synchronous flows via gRPC / REST

- Asynchronous workflows via RabbitMQ

- Extensible webhook handling per external service

- Domain ownership (Warehouse owns stock, Store owns order flow)
