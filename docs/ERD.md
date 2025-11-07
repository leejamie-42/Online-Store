# ERD Design

**Version:** 1.0
**Last Updated:** October 16 2025
**Status:** Active

## STORE BACKEND SERVICE

### User

| Field           | Type      | Notes              |
| --------------- | --------- | ------------------ |
| id              | bigint    | PK, auto-increment |
| name            | varchar   |                    |
| email           | varchar   |                    |
| password_hashed | varchar   |                    |
| created_at      | timestamp |                    |
| updated_at      | timestamp |                    |

### Product

| Field       | Type          | Notes              |
| ----------- | ------------- | ------------------ |
| id          | bigint        | PK, auto-increment |
| name        | varchar       |                    |
| description | text          |                    |
| price       | decimal(10,2) |                    |
| image_url   | varchar       |                    |
| quantity    | integer       |                    |
| created_at  | timestamp     |                    |
| updated_at  | timestamp     |                    |

### Order

| Field         | Type      | Notes                                                                        |
| ------------- | --------- | ---------------------------------------------------------------------------- |
| id            | bigint    | PK, auto-increment                                                           |
| product_id    | bigint    | FK → Product.id                                                              |
| user_id       | bigint    | FK → User.id                                                                 |
| quantity      | int       |                                                                              |
| status        | enum      | `pending`, `processing`, `picked_up`, `delivering`, `delivered`, `cancelled` |
| first_name    | varchar   |                                                                              |
| last_name     | varchar   |                                                                              |
| email         | varchar   |                                                                              |
| mobile_number | varchar   |                                                                              |
| address_line1 | varchar   |                                                                              |
| address_line2 | varchar   |                                                                              |
| country       | varchar   |                                                                              |
| city          | varchar   |                                                                              |
| suburb        | varchar   |                                                                              |
| postcode      | varchar   |                                                                              |
| created_at    | timestamp |                                                                              |
| updated_at    | timestamp |                                                                              |

### PaymentMethod

| Field      | Type      | Notes                     |
| ---------- | --------- | ------------------------- |
| id         | bigint    | PK, auto-increment        |
| type       | enum      | `BPAY`                    |
| payload    | jsonb     | Payment metadata (ref ID) |
| created_at | timestamp |                           |
| updated_at | timestamp |                           |

### Payment

| Field      | Type          | Notes                                |
| ---------- | ------------- | ------------------------------------ |
| id         | bigint        | PK, auto-increment                   |
| amount     | decimal(10,2) |                                      |
| order_id   | bigint        | FK → Order.id                        |
| status     | enum          | `pending`, `processing`, `completed` |
| method_id  | bigint        | FK → Payment_method.id               |
| created_at | timestamp     |                                      |
| updated_at | timestamp     |                                      |

### Refund

| Field          | Type          | Notes                                          |
| -------------- | ------------- | ---------------------------------------------- |
| id             | bigint        | PK                                             |
| payment_id     | bigint        | FK → Payment.id, unique                        |
| transaction_id | bigint        | External reference                             |
| amount         | decimal(15,2) |                                                |
| status         | enum          | `pending`, `processing`, `completed`, `failed` |
| created_at     | timestamp     |                                                |
| updated_at     | timestamp     |                                                |
| refunded_at    | timestamp     |                                                |

### Shipment

| Field                | Type          | Notes                                                                           |
| -------------------- | ------------- | ------------------------------------------------------------------------------- |
| id                   | bigint        | PK, auto-increment                                                              |
| order_id             | bigint        | FK → Order.id, unique                                                           |
| shipment_id          | varchar(100)  | External DeliveryCo shipment identifier, unique                                 |
| status               | varchar(50)   | `SHIPMENT_CREATED`, `PROCESSING`, `PICKED_UP`, `IN_TRANSIT`, `DELIVERED`, `LOST` |
| carrier              | varchar(100)  | Carrier name (e.g., "DeliveryCo")                                               |
| tracking_number      | varchar(100)  | Tracking number for shipment                                                    |
| estimated_delivery   | timestamp     | Estimated delivery timestamp                                                    |
| actual_delivery      | timestamp     | Actual delivery timestamp (set when DELIVERED)                                  |
| current_warehouse_id | bigint        | Next warehouse to pick up from (multi-warehouse fulfillment)                    |
| pickup_path          | text          | Warehouse pickup journey (e.g., "WH-1->WH-2->WH-3")                             |
| delivery_address     | text          | Full delivery address                                                           |
| created_at           | timestamp     | Default NOW()                                                                   |
| updated_at           | timestamp     | Default NOW()                                                                   |

## WAREHOUSE SERVICE

### Warehouse_Product

| Field       | Type          | Notes                        |
| ----------- | ------------- | ---------------------------- |
| id          | bigint        | PK, auto-increment           |
| name        | varchar       |                              |
| description | text          |                              |
| price       | decimal(10,2) |                              |
| image_url   | varchar       |                              |
| published   | boolean       | Controls visibility in store |
| created_at  | timestamp     |                              |
| updated_at  | timestamp     |                              |

### Warehouse

| Field         | Type      | Notes              |
| ------------- | --------- | ------------------ |
| id            | bigint    | PK, auto-increment |
| name          | varchar   |                    |
| description   | text      |                    |
| address_line1 | varchar   |                    |
| address_line2 | varchar   |                    |
| country       | varchar   |                    |
| city          | varchar   |                    |
| suburb        | varchar   |                    |
| postcode      | varchar   |                    |
| created_at    | timestamp |                    |
| updated_at    | timestamp |                    |

### Inventory

| Field        | Type      | Notes                             |
| ------------ | --------- | --------------------------------- |
| id           | bigint    | PK, auto-increment                |
| quantity     | int       |                                   |
| warehouse_id | bigint    | FK → Warehouse.id                 |
| product_id   | bigint    | FK → Warehouse_Product.id         |
| version      | int       | Optimistic concurrency versioning |
| created_at   | timestamp |                                   |
| updated_at   | timestamp |                                   |

### InventoryTransactionRecord

| Field        | Type      | Notes                                                                       |
| ------------ | --------- | --------------------------------------------------------------------------- |
| id           | bigint    | PK, auto-increment                                                          |
| amount       | int       | Inventory delta                                                             |
| order_id     | bigint    | FK                                                                          |
| warehouse_id | bigint    | FK → Warehouse.id                                                           |
| version      | int       | For concurrency control                                                     |
| created_at   | timestamp |  


### Reservation

| Field        | Type      | Notes                                                                       |
| ------------ | --------- | --------------------------------------------------------------------------- |
| id           | bigint    | PK, auto-increment                                                          |
| order_id     | bigint    | FK                                                                          |
| warehouse_id | bigint    | FK → Warehouse.id                                                           |
| product_id   | bigint    | FK → WarehouseProduct.id                                                    |
| status       | enum      | `reserved`, `committed`, `rolled_back`                                      |
| quantity     | int       | Reserved stock quantity                                                     |
| version      | int       | For concurrency control                                                     |
| created_at   | timestamp |                                                                             |
| updated_at   | timestamp |                                                                             |

## BANK SERVICE

### Customer

| Field      | Type      | Notes              |
| ---------- | --------- | ------------------ |
| id         | bigint    | PK, auto-increment |
| first_name | varchar   |                    |
| last_name  | varchar   |                    |
| created_at | timestamp |                    |
| updated_at | timestamp |                    |

### Account

| Field       | Type          | Notes                                      |
| ----------- | ------------- | ------------------------------------------ |
| id          | bigint        | PK, auto-increment                         |
| customer_id | bigint        | FK → Customer.id                           |
| name        | varchar       | Account name                               |
| type        | enum          | `Personal`, `Business`, `INTERNAL_REVENUE` |
| balance     | decimal(15,2) |                                            |
| version     | int           | For concurrency control                    |
| created_at  | timestamp     |                                            |
| updated_at  | timestamp     |                                            |

### TransactionRecord

| Field        | Type          | Notes                                                 |
| ------------ | ------------- | ----------------------------------------------------- |
| id           | bigint        | PK, auto-increment                                    |
| amount       | decimal(15,2) |                                                       |
| memo         | text          | Optional note                                         |
| to_account   | bigint        | FK → Account.id                                       |
| from_account | bigint        | FK → Account.id                                       |
| status       | enum          | `initiated`, `processing`, `completed`, `failed`      |
| version      | int           | For concurrency control                               |
| created_at   | timestamp     |                                                       |

### Merchant

| Field       | Type      | Notes              |
| ----------- | --------- | ------------------ |
| id          | bigint    | PK, auto-increment |
| customer_id | bigint    | FK → Customer.id   |
| biller_code | varchar   | Unique             |
| account_id  | bigint    | FK → Account.id    |
| created_at  | timestamp |                    |
| updated_at  | timestamp |                    |

### BPAY_Transaction_Information

| Field        | Type          | Notes                                       |
| ------------ | ------------- | ------------------------------------------- |
| id           | bigint        | PK, auto-increment                          |
| reference_id | varchar       | BPAY Reference (unique)                     |
| biller_code  | varchar       | From Merchant table                         |
| amount       | decimal(15,2) |                                             |
| status       | enum          | `pending`, `paid`, `expired`, `cancelled`   |
| created_at   | timestamp     |                                             |
| expired_at   | timestamp     |                                             |
| paid_at      | timestamp     | Nullable, set when status becomes `paid`    |

### WebhookRegistration

| Field         | Type      | Notes              |
| ------------- | --------- | ------------------ |
| id            | bigint    | PK, auto-increment |
| event         | varchar   | Event type         |
| callback_url  | varchar   | Store's webhook URL|
| registered_at | timestamp |                    |

## EMAIL SERVICE

### Email

| Field      | Type      | Notes                    |
| ---------- | --------- | ------------------------ |
| id         | bigint    | PK, auto-increment       |
| title      | varchar   | Subject or template name |
| sender     | varchar   | Email sender             |
| receiver   | varchar   | Email receiver           |
| content    | text      | Body content             |
| order_id   | bigint    | FK (optional)            |
| created_at | timestamp | Timestamp                |

## DELIVERYCO SERVICE

### DeliveryRequest

| Field            | Type          | Notes                            |
| ---------------- | ------------- | -------------------------------- |
| id               | bigint        | PK, auto-increment               |
| amount           | decimal(10,2) |                                  |
| status           | varchar       | e.g., `queued`, `failed`, `sent` |
| receiver_address | text          |                                  |

### Shipment

| Field               | Type   | Notes                                          |
| ------------------- | ------ | ---------------------------------------------- |
| id                  | bigint | PK, auto-increment                             |
| status              | enum   | `initiated`, `in transit`, `delivered`, `fail` |
| delivery_request_id | bigint | FK → Delivery_Request.id                       |
| pickup_address      | text   | Where warehouse pickup occurs                  |
