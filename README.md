# Online Store - Group Project

* NOTE: This code is for viewing purposes only. Do not copy, distribute, or use without permission! *
This repository contains the source code for a highly available and fault-tolerant online store application, as a group project for a school assessment. 
Please note that I specifically worked on the warehouse and store-with-warehouse integration components, and I asked for my teammates' permission before uploading their code.
The system is designed to handle complex order fulfillment workflows by integrating with external enterprise services.

---

## Table of Contents

1.  [Project Overview](#project-overview)
2.  [High-Level Architecture](#high-level-architecture)
3.  [Key Features](#key-features)
4.  [Tech Stack](#tech-stack)
5.  [Getting Started](#getting-started)
6.  [Seed Data](#seed-data)
7.  [Database Migrations](#database-migrations)
8.  [Configuration Management](#configuration-management)
9.  [Project Structure](#project-structure)

---

## Project Overview

### Problem Statement

Modern e-commerce platforms must be robust, resilient, and capable of integrating seamlessly with a network of third-party services. The core challenge is to design and build a distributed system that maintains data integrity and high availability, even when individual components or external services fail. This project addresses that challenge by building an online store that can reliably process orders through a complex workflow involving warehouse logistics, payment processing, and delivery coordination.

---

## High-Level Architecture

The system is designed with a **distributed microservices architecture**, following an event-driven and modular design. A React single-page application (SPA) serves as the client, communicating with multiple backend services via REST, gRPC, and webhooks. The architecture emphasizes fault tolerance, scalability, and clear separation of concerns.

**Core Components:**

- **Frontend (React)**: User interface for browsing products, placing orders, and tracking shipments.
- **Store Backend (Spring Boot)**: Central orchestrator for order workflows, user authentication, and service coordination.
- **Warehouse Service**: Microservice managing inventory, product catalog, and stock fulfillment with version-controlled transactions.
- **Bank Service**: External-style BPAY payment simulator with webhook callback support for payment events.
- **DeliveryCo Service**: Simulated external shipment service with webhook integration for delivery status updates.
- **Email Service**: Internal message-based email dispatcher powered by RabbitMQ and Mailgun(We just need to print when demo locally).
- **RabbitMQ**: Message broker enabling asynchronous workflows (email, fulfillment, inventory rollback).
- **PostgreSQL**: Persistent storage distributed across service databases.
- **Redis**: High-speed caching layer for sessions and frequently accessed data.

**Communication Patterns:**

| From          | To            | Protocol | Purpose                                               |
| ------------- | ------------- | -------- | ----------------------------------------------------- |
| Frontend      | Store Backend | REST     | Order placement, authentication, product listing      |
| Store Backend | Warehouse     | gRPC     | Atomic inventory operations (reserve/commit/rollback) |
| Store Backend | Bank          | REST     | BPAY payment generation, webhook registration         |
| Bank          | Store Backend | Webhook  | Payment/refund event notifications                    |
| Store Backend | DeliveryCo    | REST     | Shipment requests, webhook registration               |
| DeliveryCo    | Store Backend | Webhook  | Delivery status updates                               |
| Store Backend | RabbitMQ      | AMQP     | Async email, fulfillment, rollback events             |
| RabbitMQ      | Email Service | Consumer | Email dispatch processing                             |

---

## Key Features

This application implements the full order lifecycle with distributed transaction patterns:

- **User Authentication**: Secure login with BCrypt password hashing. Default test account: `customer`/`COMP5348`.
- **Product Catalog Sync**: Real-time product synchronization from Warehouse service via RabbitMQ product updates.
- **Smart Warehouse Fulfillment**: gRPC-based inventory reservation with optimistic concurrency control and version tracking.
- **BPAY Payment Integration**: Generate BPAY biller codes with reference numbers, integrate with Bank service via webhooks.
- **Webhook-Driven Workflows**: Asynchronous payment and delivery status updates via webhook callbacks.
- **Delivery Coordination**: Full DeliveryCo integration with comprehensive shipment tracking:
  - Automatic shipment request after successful payment and stock commitment
  - Real-time status updates via webhooks (PICKED_UP → IN_TRANSIT → DELIVERED)
  - Multi-warehouse fulfillment support with pickup journey tracking
  - Automatic inventory rollback for lost shipments
  - Email notifications for all shipment status changes
- **Email Notifications**: RabbitMQ-powered async email dispatch for order confirmations, shipment updates, payment events, and cancellations.
- **Compensating Transactions**: Saga pattern implementation for order cancellations and lost shipments with automatic refunds and inventory rollback.
- **Optimistic Concurrency**: Version-based inventory control to prevent overselling and race conditions.

---

## Tech Stack

| Component              | Technology & Frameworks                                           |
| :--------------------- | :---------------------------------------------------------------- |
| **Store Backend**      | Java 17+, Spring Boot 3 (Web, Data JPA, Security, Actuator, AMQP) |
| **Warehouse Service**  | Java 17+, Spring Boot 3, gRPC, PostgreSQL with optimistic locking |
| **Bank Service**       | Java 17+, Spring Boot 3, BPAY simulation, webhook support         |
| **DeliveryCo Service** | Java 17+, Spring Boot 3, shipment simulation, webhook integration |
| **Email Service**      | Java 17+, Spring Boot 3, RabbitMQ consumer, Mailgun API           |
| **Frontend**           | React 18 with React Router, Vite, TypeScript, Tailwind CSS        |
| **Database**           | PostgreSQL (distributed across services)                          |
| **Message Broker**     | RabbitMQ with topic exchanges and dead letter queues              |
| **RPC Framework**      | gRPC for Store ↔ Warehouse communication                          |
| **Build Tools**        | Gradle 8+ (Backend), npm/pnpm (Frontend)                          |
| **API Protocols**      | REST, gRPC, Webhooks, AMQP                                        |
| **Infrastructure**     | Docker & Docker Compose                                           |

---

## Getting Started

Follow these instructions to set up the complete development environment.

### Prerequisites

- Java JDK 17 or newer
- Node.js v22+ (we recommend using `nvm`)
- Docker Desktop

### 1. Initial Setup

**Clone the repository and prepare your local environment:**

```bash
git clone <repository-url>

cd <repository-name>

# Copy the environment variable template
cp .env.example .env

# Open .env and fill in your local secrets (e.g., PG_PASSWORD)

# Start all services (PostgreSQL)
docker-compose up -d
```

### 2. Run Backend Services

The project uses a **Gradle multi-project build** with separate microservices. Each service can be run independently.

#### Store Backend (Main Orchestrator)

```bash
cd store-backend
# Copy the environment variable template
cp .env.example .env

# Open .env and fill in your local secrets (e.g., PG_PASSWORD)

# Run from store-backend directory
../gradlew bootRun

# Or run from root directory
./gradlew :store-backend:bootRun
```

The Store Backend will be available at `http://localhost:8081`

#### Other Microservices (When Implemented)

```bash
# Run individual services from root directory
./gradlew :warehouse:bootRun        # Warehouse service
./gradlew :bank:bootRun              # Bank service
./gradlew :delivery-co:bootRun       # DeliveryCo service
./gradlew :email-service:bootRun     # Email service

# Or run all services concurrently
./gradlew bootRun --parallel
```

#### Load Seed Data

Before testing the complete order flow, load seed data into both warehouse and store-backend databases:

```bash
# 1. Load warehouse seed data (3 warehouses, 10 products, 30 inventory records)
./gradlew :warehouse:reloadSeedData

# 2. Load store-backend seed data (10 products aligned with warehouse)
./gradlew :store-backend:reloadSeedData
```

**Important**: The store-backend product catalog is aligned with the warehouse service. Both services use the same product IDs (1-10) with matching names, prices, and images. Store-backend quantities represent aggregated totals from all warehouse inventories.

This provides realistic inventory across Australian warehouses and a synchronized product catalog for end-to-end testing. See [Seed Data](#seed-data) section for details.

### 3. Run Frontend

```bash
cd frontend

# Install dependencies (first time only)
npm install

# Copy the environment variable template
cp .env.example .env.local

# Start the development server
npm run dev
```

The frontend will be available at `http://localhost:3001`

#### Frontend Scripts

| Script          | Command                 | Description                         |
| --------------- | ----------------------- | ----------------------------------- |
| **Development** | `npm run dev`           | Start Vite dev server with HMR      |
| **Build**       | `npm run build`         | Type-check and build for production |
| **Preview**     | `npm run preview`       | Preview production build locally    |
| **Lint**        | `npm run lint`          | Run ESLint                          |
| **Lint Fix**    | `npm run lint:fix`      | Auto-fix ESLint issues              |
| **Format**      | `npm run format`        | Format code with Prettier           |
| **Type Check**  | `npm run type-check`    | Run TypeScript compiler             |
| **Test**        | `npm run test`          | Run tests with Vitest               |
| **Test UI**     | `npm run test:ui`       | Run tests with UI                   |
| **Coverage**    | `npm run test:coverage` | Generate test coverage              |

For detailed frontend documentation, see [frontend/README.md](./frontend/README.md) and [docs/frontend/](./docs/frontend/).

---

## Seed Data

Both warehouse and store-backend services include seed data for end-to-end testing. The product catalogs are **aligned** to ensure consistent product IDs, names, and pricing across services.

### Product Alignment

| Service           | Products    | Product IDs     | Quantity Source                       |
| ----------------- | ----------- | --------------- | ------------------------------------- |
| **Warehouse**     | 10 products | 1-10            | Distributed across 3 warehouses       |
| **Store-Backend** | 10 products | 1-10 (matching) | Aggregated totals from all warehouses |

**Key Points:**

- ✅ Same product IDs (1-10) in both services
- ✅ Same names, descriptions, prices, and image URLs
- ✅ Store-backend quantities = SUM of warehouse inventories
- ✅ Runtime sync handled by RabbitMQ ProductUpdateEvent

### Quick Setup

#### Warehouse Service

```bash
# Load seed data into warehouse database
./gradlew :warehouse:reloadSeedData

# Clear all warehouse data
./gradlew :warehouse:clearSeedData
```

**What's Included:**

- **3 Warehouses**: Sydney (primary), Melbourne (secondary), Brisbane (backup)
- **10 Products**: Electronics, Home & Living, Fashion & Accessories, Sports & Fitness
- **30 Inventory Records**: Complete stock distribution (~2,545 total units)

#### Store-Backend Service

```bash
# Load seed data into store-backend database
./gradlew :store-backend:reloadSeedData

# Clear all store-backend products
./gradlew :store-backend:clearSeedData
```

**What's Included:**

- **10 Products**: Aligned with warehouse products (IDs 1-10)
- **Aggregated Quantities**: Total stock across all warehouses per product
  - Example: Product 1 (headphones) = 280 units (Sydney: 150 + Melbourne: 80 + Brisbane: 50)

#### Full Demo Reset

```bash
# Reset both services to fresh demo state
./gradlew :warehouse:reloadSeedData
./gradlew :store-backend:reloadSeedData
```

### Product Catalog Summary

The seed data includes 10 products across 4 categories:

**Electronics (3 products):**

- Wireless Bluetooth Headphones (ID: 1) - 280 units total
- Smart Watch Pro (ID: 2) - 200 units total
- Portable Bluetooth Speaker (ID: 3) - 400 units total

**Home & Living (2 products):**

- Stainless Steel Coffee Maker (ID: 4) - 160 units total
- LED Desk Lamp (ID: 5) - 240 units total

**Fashion & Accessories (2 products):**

- Leather Crossbody Bag (ID: 6) - 125 units total
- Polarized Sunglasses (ID: 7) - 300 units total

**Sports & Fitness (3 products):**

- Yoga Mat Premium (ID: 8) - 190 units total
- Resistance Bands Set (ID: 9) - 350 units total
- Stainless Steel Water Bottle (ID: 10) - 280 units total

### Verification

#### Warehouse Database

```bash
# Verify warehouse data
docker exec -i store-postgres psql -U postgres -d warehouse \
  -c "SELECT COUNT(*) FROM warehouse;"           # Should show: 3
docker exec -i store-postgres psql -U postgres -d warehouse \
  -c "SELECT COUNT(*) FROM warehouse_product;"  # Should show: 10
docker exec -i store-postgres psql -U postgres -d warehouse \
  -c "SELECT COUNT(*) FROM inventory;"           # Should show: 30
```

#### Store-Backend Database

```bash
# Verify store-backend data
docker exec -i store-postgres psql -U postgres -d store_db \
  -c "SELECT COUNT(*) FROM products;"           # Should show: 10

# Verify product alignment (compare IDs and names)
docker exec -i store-postgres psql -U postgres -d store_db \
  -c "SELECT id, name, quantity, price FROM products ORDER BY id;"
```

#### Alignment Check

```bash
# Compare product IDs and names between services
docker exec -i store-postgres psql -U postgres -d warehouse \
  -c "SELECT id, name FROM warehouse_product ORDER BY id;" > /tmp/warehouse_products.txt

docker exec -i store-postgres psql -U postgres -d store_db \
  -c "SELECT id, name FROM products ORDER BY id;" > /tmp/store_products.txt

diff /tmp/warehouse_products.txt /tmp/store_products.txt
# Should show no differences (IDs and names match)
```

### Seed Data Files

- **Warehouse**: `warehouse/src/main/resources/data.sql`
- **Store-Backend**: `store-backend/src/main/resources/data.sql`

Both files are managed via Gradle tasks and can be reloaded independently without affecting the other service.

---

## Database Migrations

The store-backend uses **Flyway** for database version control and schema management. Flyway ensures consistent database structure across all environments through versioned SQL migration files.

### Current Migration Status

```
V0: << Flyway Baseline >>
V1: Create users table
V2: Create products table
V3: Rename product to products (plural naming convention)
V4: Create orders table
V5: Create payment_methods table
V6: Create payments table
V7: Create refunds table
V8: Create shipments table
```

**Latest Schema Additions:**

- **Shipments Table (V8)**: Tracks delivery shipments via DeliveryCo service with multi-warehouse fulfillment support
  - Stores shipment status, tracking information, and pickup journey
  - Supports inventory rollback on lost shipments
  - Integrates with DeliveryCo webhooks for real-time status updates

### Quick Verification

```bash
# Check applied migrations
docker exec store-postgres psql -U postgres -d store_db \
  -c "SELECT version, description, installed_on FROM flyway_schema_history;"

# Verify table structure
docker exec store-postgres psql -U postgres -d store_db -c "\dt"
```

### Flyway Gradle Commands

The project includes the Flyway Gradle plugin for direct database migration management. All commands must be run from the **root directory**.

#### Available Commands

```bash
# Show migration status and history (most commonly used)
./gradlew :store-backend:flywayInfo

# Validate migrations against database
./gradlew :store-backend:flywayValidate

# Apply pending migrations manually
./gradlew :store-backend:flywayMigrate

# Repair schema history (fix checksums after migration file changes)
./gradlew :store-backend:flywayRepair

# Baseline an existing database
./gradlew :store-backend:flywayBaseline

# ⚠️ Clean database (DANGEROUS - drops all objects)
./gradlew :store-backend:flywayClean
```

#### Generate New Migration Files (added)

A Gradle task is available to generate timestamp-versioned Flyway SQL files with a standard header. This helps avoid manual naming mistakes and ensures unique, ordered versions.

```bash
# From repo root — provide a short, kebab-case name for the migration
./gradlew :store-backend:generateMigrationFile -PmgName=create_orders_table

# Example output
# ✅ Created migration file: store-backend/src/main/resources/db/migration/V20251028153045__create_orders_table.sql
```

Notes:

- The task writes a new SQL file under `store-backend/src/main/resources/db/migration/`.
- Version format is `VyyyyMMddHHmmss__<name>.sql` (e.g., `V20251028153045__create_orders_table.sql`).
- Keep the name brief and descriptive (kebab_case recommended).
- Do not edit previously applied migrations; create a new one instead.

#### Example Output: `flywayInfo`

```
Schema version: 3
+-----------+---------+----------------------------+----------+---------------------+----------+
| Category  | Version | Description                | Type     | Installed On        | State    |
+-----------+---------+----------------------------+----------+---------------------+----------+
|           | 0       | << Flyway Baseline >>      | BASELINE | 2025-10-24 21:48:14 | Baseline |
| Versioned | 1       | create users table         | SQL      | 2025-10-24 21:59:16 | Success  |
| Versioned | 2       | create product table       | SQL      | 2025-10-24 22:24:30 | Success  |
| Versioned | 3       | rename product to products | SQL      | 2025-10-24 22:25:18 | Success  |
+-----------+---------+----------------------------+----------+---------------------+----------+
```

#### Common Use Cases

| Scenario                    | Command                                                         |
| --------------------------- | --------------------------------------------------------------- |
| Check migration status      | `./gradlew :store-backend:flywayInfo`                           |
| Verify migrations are valid | `./gradlew :store-backend:flywayValidate`                       |
| Fix checksum mismatch       | `./gradlew :store-backend:flywayRepair`                         |
| Apply new migrations        | `./gradlew :store-backend:flywayMigrate` (auto-runs on bootRun) |

**Note**: The `.env` file in `store-backend/` is automatically loaded by the Flyway Gradle plugin to provide database credentials.

### How It Works

1. **Automatic Migration**: Flyway runs automatically when you start the application
2. **Version Control**: All schema changes are tracked in SQL files (`store-backend/src/main/resources/db/migration/`)
3. **Schema Validation**: Hibernate validates that JPA entities match the database schema
4. **No Manual Intervention**: Fresh database setups automatically apply all migrations

### Adding New Features

**Migration-First Workflow**:

```
1. Design Schema → 2. Create Migration SQL → 3. Create JPA Entity → 4. Test → 5. Implement Feature
```

**Example**: Adding an Orders table

```bash
# 1. Create migration file
cat > store-backend/src/main/resources/db/migration/V4__create_orders_table.sql << 'EOF'
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_orders_product FOREIGN KEY (product_id) REFERENCES products(id)
);
CREATE INDEX idx_orders_user_id ON orders(user_id);
EOF

# 2. Create corresponding JPA entity
# See docs/store-backend/FLYWAY_GUIDE.md for complete example

# 3. Run application - Flyway applies V4 automatically
./gradlew :store-backend:bootRun

# 4. Verify migration applied
docker exec store-postgres psql -U postgres -d store_db \
  -c "SELECT version FROM flyway_schema_history;"
```

### Key Principles

- ✅ **Never modify applied migrations** - Create new migrations for changes
- ✅ **Test locally first** - Always test migrations on clean database
- ✅ **Match entity to schema** - JPA entities must exactly match migration SQL
- ✅ **Sequential versions** - Use V1, V2, V3... (no gaps, no reordering)
- ✅ **Prefer timestamped versions going forward** - Use `generateMigrationFile` for new migrations to guarantee ordering in teams

### Documentation

- **Comprehensive Guide**: [docs/store-backend/FLYWAY_GUIDE.md](./docs/store-backend/FLYWAY_GUIDE.md)
  - Complete migration workflow
  - Step-by-step examples
  - Best practices and troubleshooting
  - Common patterns and templates

---

## Configuration Management

- **`application.yml`**: The main configuration file. It sets the default active profile to `local`.
- **`application-local.yml`**: Contains all shared, non-secret settings for local development. This file is committed to Git.
- **`.env`**: This file contains your personal, secret credentials (like database passwords). It is loaded by Spring Boot at startup and is never committed to Git (it's in `.gitignore`).

---

## Project Structure

```
.
├── store-backend/                          # Store Backend microservice
│   ├── src/main/
│   │   ├── java/com/comp5348/store/        # Java source code
│   │   └── resources/
│   │       ├── application.yml             # Main config (sets 'local' profile)
│   │       └── application-local.yml       # SHARED settings for local dev
│   ├── build.gradle                        # Backend dependencies
│   └── .env.example                        # Backend environment template
│
├── warehouse/                              # Warehouse microservice (planned)
│   ├── src/main/java/                      # gRPC service implementation
│   └── build.gradle                        # Warehouse dependencies
│
├── bank/                                   # Bank microservice (planned)
│   ├── src/main/java/                      # BPAY + webhook implementation
│   └── build.gradle                        # Bank dependencies
│
├── delivery-co/                            # DeliveryCo microservice (planned)
│   ├── src/main/java/                      # Shipment + webhook implementation
│   └── build.gradle                        # DeliveryCo dependencies
│
├── email-service/                          # Email microservice (planned)
│   ├── src/main/java/                      # RabbitMQ consumer + Mailgun integration
│   └── build.gradle                        # Email service dependencies
│
├── frontend/
│   ├── src/                                # React application source
│   │   ├── api/                            # API integration layer
│   │   ├── components/                     # Reusable React components
│   │   │   ├── ui/                         # Base UI components
│   │   │   ├── layout/                     # Layout components
│   │   │   ├── features/                   # Feature-specific components
│   │   │   └── common/                     # Common components
│   │   ├── pages/                          # Page components
│   │   ├── hooks/                          # Custom React hooks
│   │   ├── context/                        # React Context providers
│   │   ├── types/                          # TypeScript type definitions
│   │   ├── utils/                          # Utility functions
│   │   ├── styles/                         # Global styles
│   │   └── config/                         # Configuration files
│   ├── public/                             # Static assets
│   ├── package.json                        # Frontend dependencies
│   ├── vite.config.ts                      # Vite configuration
│   ├── tailwind.config.js                  # Tailwind CSS configuration
│   ├── .env.example                        # Frontend environment template
│   └── README.md                           # Frontend documentation
│
├── docs/
│   ├── SYSTEM_ARCHITECTURE.md              # Complete microservices architecture
│   ├── SYSTEM_INTERFACE_SPEC.md            # REST, gRPC, webhook, RabbitMQ specs
│   ├── ERD.md                              # Entity relationship diagrams
│   └── frontend/                           # Comprehensive frontend docs
│       ├── README.md                       # Documentation index
│       ├── ARCHITECTURE.md                 # Technical architecture
│       ├── UI_DESIGN_SYSTEM.md             # Design system guide
│       ├── API_INTEGRATION.md              # API integration guide
│       ├── DEVELOPMENT_STANDARDS.md        # Coding standards
│       └── IMPLEMENTATION_PLAN.md          # Implementation roadmap
│
├── gradle/wrapper/                         # Gradle wrapper files
├── .env.example                            # Template for environment variables
├── docker-compose.yml                      # Docker configuration for all services
├── settings.gradle                         # Gradle multi-project configuration
├── gradlew                                 # Gradle wrapper script (Unix)
├── gradlew.bat                             # Gradle wrapper script (Windows)
└── README.md                               # This file
```

**Note**: The project uses **Gradle multi-project build** structure. Microservices beyond `store-backend` are defined in `settings.gradle` and will be implemented incrementally.
