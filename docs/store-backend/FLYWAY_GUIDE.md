# Flyway Database Migration Guide

**Version**: 1.0
**Last Updated**: October 24, 2025
**Audience**: Developers working on store-backend microservice

---

## Table of Contents

1. [Overview](#overview)
2. [Why Flyway?](#why-flyway)
3. [Quick Start](#quick-start)
4. [Migration Workflow](#migration-workflow)
5. [Naming Conventions](#naming-conventions)
6. [Best Practices](#best-practices)
7. [Common Patterns](#common-patterns)
8. [Troubleshooting](#troubleshooting)
9. [Reference](#reference)

---

## Overview

Flyway is a database version control and migration tool integrated into the store-backend service. It manages schema evolution through versioned SQL migration files, ensuring consistent database structure across all environments.

### Current Migration Status

```
V0: << Flyway Baseline >>
V1: Create users table
V2: Create product table
V3: Rename product to products
V4+: Future migrations
```

### Configuration

Flyway is configured in `store-backend/src/main/resources/application-local.yml`:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # Hibernate validates only, doesn't create schema

  flyway:
    enabled: true
    baseline-on-migrate: true
    baseline-version: 0
    locations: classpath:db/migration
    validate-on-migrate: true
```

---

## Why Flyway?

### Problems Solved

| Problem | Flyway Solution |
|---------|----------------|
| **Schema drift** | Version-controlled SQL files in Git |
| **Team coordination** | Sequential migrations prevent conflicts |
| **Environment inconsistency** | Same migrations apply everywhere |
| **Production risk** | Validated migrations before deployment |
| **Audit trail** | `flyway_schema_history` table tracks changes |

### Flyway vs Hibernate DDL-Auto

| Feature | Hibernate DDL-Auto | Flyway |
|---------|-------------------|--------|
| Version control | ❌ No Git tracking | ✅ SQL files in Git |
| Team collaboration | ❌ Manual sync | ✅ Automatic sync |
| Production safety | ❌ Risky auto-updates | ✅ Validated migrations |
| Rollback support | ❌ No rollback | ✅ Document rollback steps |
| Audit trail | ❌ No history | ✅ Schema history table |

---

## Quick Start

### For Existing Project (Current State)

```bash
# 1. Ensure infrastructure is running
docker-compose up -d

# 2. Configure environment
cd store-backend
cp .env.example .env
# Edit .env with your PG_PASSWORD

# 3. Run application - Flyway auto-applies migrations
../gradlew bootRun

# 4. Verify migrations applied
docker exec store-postgres psql -U postgres -d store_dev_db \
  -c "SELECT version, description FROM flyway_schema_history;"

# Expected output:
# version |        description
# ---------+----------------------------
#  0       | << Flyway Baseline >>
#  1       | create users table
#  2       | create product table
#  3       | rename product to products
```

### For Fresh Setup (New Team Member)

```bash
# 1. Clone repository
git clone <repo-url>
cd <repo-name>

# 2. Start infrastructure
docker-compose up -d

# 3. Configure store-backend
cd store-backend
cp .env.example .env
# Add your PG_PASSWORD and other secrets

# 4. Run application
../gradlew bootRun

# ✅ Flyway automatically creates schema from migrations!
```

---

## Migration Workflow

### The Proper Development Flow

**Migration-First Approach** ✅

```
1. Design Schema (think about database)
   ↓
2. Create Migration SQL (VX__description.sql)
   ↓
3. Create JPA Entity (match migration)
   ↓
4. Test Migration (apply and verify)
   ↓
5. Implement Feature (service, controller, etc.)
```

### Step-by-Step: Adding a New Feature

Let's walk through adding an "Orders" feature:

#### Step 1: Design the Schema

Consider:
- What tables do you need?
- What relationships (foreign keys)?
- What indexes for performance?
- What constraints for data integrity?

**Example Design**:
```
orders table
├── Relationships: users.id, products.id
├── Indexes: user_id, product_id, status, created_at
└── Constraints: quantity > 0
```

#### Step 2: Create Migration File

**File**: `store-backend/src/main/resources/db/migration/V4__create_orders_table.sql`

```sql
-- =====================================================
-- Migration: V4 - Create Orders Table
-- Description: Order management with relationships
-- Author: Your Name
-- Date: 2025-10-24
-- =====================================================

CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'pending',

    -- Shipping information
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    email VARCHAR(255),
    mobile_number VARCHAR(20),
    address_line1 VARCHAR(255),
    city VARCHAR(100),
    postcode VARCHAR(20),
    country VARCHAR(100),

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign keys
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_orders_product FOREIGN KEY (product_id)
        REFERENCES products(id) ON DELETE RESTRICT
);

-- Performance indexes
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_product_id ON orders(product_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at DESC);

-- Data integrity
ALTER TABLE orders ADD CONSTRAINT chk_orders_quantity_positive
    CHECK (quantity > 0);

-- Documentation
COMMENT ON TABLE orders IS 'Customer orders with shipping details';
COMMENT ON COLUMN orders.status IS 'Values: pending, processing, picked_up, delivering, delivered, cancelled';
```

**Key Points**:
- Use `BIGSERIAL` for auto-increment IDs (PostgreSQL)
- Always include foreign key constraints
- Add indexes for frequently queried columns
- Use descriptive constraint names
- Add comments for complex fields

#### Step 3: Create JPA Entity

**File**: `store-backend/src/main/java/com/comp5348/store/model/Order.java`

```java
package com.comp5348.store.model;

import com.comp5348.store.model.auth.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(
    name = "orders",  // Matches migration table name
    indexes = {
        @Index(name = "idx_orders_user_id", columnList = "user_id"),
        @Index(name = "idx_orders_product_id", columnList = "product_id"),
        @Index(name = "idx_orders_status", columnList = "status"),
        @Index(name = "idx_orders_created_at", columnList = "created_at")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_id",  // Matches column name in SQL
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_orders_user")  // Matches constraint name
    )
    private User user;

    @NotNull(message = "Product is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "product_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_orders_product")
    )
    private Product product;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    @Column(nullable = false)
    private Integer quantity;

    @NotBlank(message = "Status is required")
    @Column(nullable = false, length = 20)
    private String status = "pending";

    // Shipping fields (snake_case in DB, camelCase in Java)
    @Size(max = 100)
    @Column(name = "first_name", length = 100)
    private String firstName;

    @Size(max = 100)
    @Column(name = "last_name", length = 100)
    private String lastName;

    @Email
    @Column(length = 255)
    private String email;

    @Size(max = 20)
    @Column(name = "mobile_number", length = 20)
    private String mobileNumber;

    @Size(max = 255)
    @Column(name = "address_line1", length = 255)
    private String addressLine1;

    @Size(max = 100)
    @Column(length = 100)
    private String city;

    @Size(max = 20)
    @Column(length = 20)
    private String postcode;

    @Size(max = 100)
    @Column(length = 100)
    private String country;

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

**Matching Requirements**:
- ✅ Table name: `@Table(name = "orders")` matches SQL
- ✅ Column names: `@Column(name = "user_id")` matches SQL
- ✅ Index names: `@Index(name = "idx_orders_user_id")` matches SQL
- ✅ Foreign key names: `@ForeignKey(name = "fk_orders_user")` matches SQL
- ✅ Data types: `BIGINT` → `Long`, `VARCHAR` → `String`, `INTEGER` → `Integer`

#### Step 4: Create Repository

```java
package com.comp5348.store.repository;

import com.comp5348.store.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);
    List<Order> findByStatus(String status);
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);
}
```

#### Step 5: Test Migration

```bash
# Test on clean database (recommended)
docker-compose down -v
docker-compose up -d

# Run application - Flyway applies V4
cd store-backend
../gradlew bootRun

# Verify migration applied
docker exec store-postgres psql -U postgres -d store_dev_db \
  -c "SELECT version, description, success FROM flyway_schema_history;"

# Check table structure
docker exec store-postgres psql -U postgres -d store_dev_db \
  -c "\d orders"

# Verify foreign keys
docker exec store-postgres psql -U postgres -d store_dev_db \
  -c "\d+ orders"

# Run tests
../gradlew test
```

#### Step 6: Implement Business Logic

Now that schema is ready, implement:
- `OrderService.java` - Business logic
- `OrderController.java` - REST endpoints
- `OrderRequest/ResponseDto.java` - API contracts
- Tests for service and controller

---

## Naming Conventions

### Migration File Naming

**Format**: `V{version}__{description}.sql`

**Rules**:
- `V` prefix (uppercase)
- Version number (sequential: 1, 2, 3, ...)
- Double underscore `__`
- Description in snake_case
- `.sql` extension

**Examples**:

```
✅ Good:
V1__create_users_table.sql
V2__create_products_table.sql
V3__rename_product_to_products.sql
V4__create_orders_table.sql
V5__add_order_status_index.sql
V6__add_products_category_column.sql

❌ Bad:
v1_create_users.sql              (lowercase v)
V1_create_users.sql               (single underscore)
V1__create-users-table.sql        (kebab-case)
V1__CreateUsersTable.sql          (PascalCase)
create_users_table.sql            (no version)
```

### Database Naming Conventions

| Element | Convention | Example |
|---------|-----------|---------|
| **Tables** | Plural, lowercase, underscores | `users`, `products`, `orders` |
| **Columns** | Singular, lowercase, underscores | `user_id`, `first_name`, `created_at` |
| **Primary Keys** | `id` | `users.id`, `products.id` |
| **Foreign Keys** | `{table}_id` | `user_id`, `product_id` |
| **Indexes** | `idx_{table}_{column(s)}` | `idx_orders_user_id` |
| **Unique Indexes** | `idx_{table}_{column}_unique` | `idx_users_email_unique` |
| **Constraints** | `chk_{table}_{column}_{rule}` | `chk_products_price_positive` |
| **Foreign Keys** | `fk_{table}_{ref_table}` | `fk_orders_user` |

---

## Best Practices

### ✅ DO This

1. **Test Migrations Locally First**
   ```bash
   # Always test on clean database
   docker-compose down -v
   docker-compose up -d
   ../gradlew bootRun
   ```

2. **Write Idempotent Migrations** (when possible)
   ```sql
   -- Use IF NOT EXISTS for safety
   CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
   ```

3. **Include Descriptive Comments**
   ```sql
   -- Purpose: Improve order search performance
   -- Reason: Status filtering is used in 80% of queries
   CREATE INDEX idx_orders_status ON orders(status);
   ```

4. **One Migration Per Logical Change**
   ```
   ✅ V4__create_orders_table.sql
   ✅ V5__create_payments_table.sql

   ❌ V4__create_orders_and_payments_tables.sql
   ```

5. **Version Control with Code**
   ```bash
   # Commit migration with related code
   git add store-backend/src/main/resources/db/migration/V4__*.sql
   git add store-backend/src/main/java/com/comp5348/store/model/Order.java
   git commit -m "feat: add Order entity with V4 migration"
   ```

### ❌ DON'T Do This

1. **Never Modify Applied Migrations**
   ```bash
   # ❌ BAD: Editing V2 after it's applied
   vim V2__create_products_table.sql  # Causes checksum errors!

   # ✅ GOOD: Create new migration
   vim V4__add_products_category.sql
   ```

2. **Don't Skip Version Numbers**
   ```
   ❌ V1, V2, V4, V5  (missing V3)
   ✅ V1, V2, V3, V4  (sequential)
   ```

3. **Don't Use Database-Specific Syntax** (unless necessary)
   ```sql
   -- ❌ PostgreSQL-only (breaks H2 tests)
   CREATE TABLE products (
       id BIGSERIAL PRIMARY KEY
   );

   -- ✅ Portable (works in PostgreSQL and H2)
   CREATE TABLE products (
       id BIGINT PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY
   );

   -- Note: Main migrations can use BIGSERIAL since production uses PostgreSQL
   --       Tests use H2 in PostgreSQL compatibility mode
   ```

4. **Don't Commit Without Testing**
   ```bash
   # ❌ BAD
   git add V4__create_orders.sql
   git commit -m "add orders"

   # ✅ GOOD
   ./gradlew bootRun          # Test migration
   ./gradlew test             # Verify tests pass
   git add V4__create_orders.sql
   git commit -m "feat: add orders table (V4 migration)"
   ```

5. **Don't Use `ddl-auto: update` Anymore**
   ```yaml
   # ❌ Conflicts with Flyway
   jpa:
     hibernate:
       ddl-auto: update

   # ✅ Let Flyway manage schema
   jpa:
     hibernate:
       ddl-auto: validate
   ```

---

## Common Patterns

### Adding a New Table

```sql
-- V4__create_payments_table.sql
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_payments_order FOREIGN KEY (order_id)
        REFERENCES orders(id) ON DELETE CASCADE
);

CREATE INDEX idx_payments_order_id ON payments(order_id);
CREATE INDEX idx_payments_status ON payments(status);
```

### Adding a Column

```sql
-- V5__add_products_category.sql
ALTER TABLE products ADD COLUMN category VARCHAR(50);

-- Add index if frequently queried
CREATE INDEX idx_products_category ON products(category);

-- Add comment
COMMENT ON COLUMN products.category IS 'Product category for filtering';
```

### Renaming a Column

```sql
-- V6__rename_products_image_url.sql
ALTER TABLE products RENAME COLUMN image_url TO image_path;

-- Update comment
COMMENT ON COLUMN products.image_path IS 'Relative path to product image';
```

### Adding a Constraint

```sql
-- V7__add_products_stock_constraint.sql
ALTER TABLE products
    ADD CONSTRAINT chk_products_quantity_max
    CHECK (quantity <= 10000);

COMMENT ON CONSTRAINT chk_products_quantity_max ON products IS
    'Prevent unrealistic stock quantities';
```

### Creating an Index

```sql
-- V8__add_orders_search_index.sql
-- Purpose: Improve full-text search on shipping address
CREATE INDEX idx_orders_address_search ON orders
    USING GIN (to_tsvector('english', address_line1));

COMMENT ON INDEX idx_orders_address_search IS
    'Full-text search index for address filtering';
```

### Modifying Data (Use With Caution)

```sql
-- V9__update_order_status_values.sql
-- Purpose: Standardize status values

-- Update existing data
UPDATE orders SET status = 'processing' WHERE status = 'in_process';
UPDATE orders SET status = 'cancelled' WHERE status = 'canceled';

-- Add constraint to prevent old values
ALTER TABLE orders
    ADD CONSTRAINT chk_orders_status_values
    CHECK (status IN ('pending', 'processing', 'picked_up', 'delivering', 'delivered', 'cancelled'));
```

---

## Troubleshooting

### Problem: "Found more than one migration with version X"

**Cause**: Duplicate migration files (e.g., in both `main/resources` and `test/resources`)

**Solution**:
```bash
# Find duplicates
find store-backend/src -name "V*.sql"

# Remove test-specific migrations (use main migrations for both)
rm -rf store-backend/src/test/resources/db/migration/
```

### Problem: "Validate failed: Migrations have failed validation"

**Cause**: Modified an already-applied migration (checksum changed)

**Solution Option 1** - Repair (if modification was intentional):
```bash
./gradlew :store-backend:flywayRepair
```

**Solution Option 2** - Revert (recommended):
```bash
# Revert file to original state
git checkout store-backend/src/main/resources/db/migration/VX__*.sql
```

### Problem: "Migration checksum mismatch"

**Cause**: File encoding or line ending changes

**Solution**:
```bash
# Ensure consistent line endings
git config core.autocrlf false

# Repair checksums
./gradlew :store-backend:flywayRepair
```

### Problem: "Syntax error in SQL statement"

**Cause**: Database-specific SQL syntax (usually in tests with H2)

**Solution**:
```sql
-- ❌ PostgreSQL-specific that breaks H2
ALTER TABLE products
    ADD CONSTRAINT chk_price CHECK (price > 0),
    ADD CONSTRAINT chk_quantity CHECK (quantity >= 0);

-- ✅ H2-compatible (separate statements)
ALTER TABLE products ADD CONSTRAINT chk_price CHECK (price > 0);
ALTER TABLE products ADD CONSTRAINT chk_quantity CHECK (quantity >= 0);
```

### Problem: "ddl-auto: validate" fails with schema mismatch

**Cause**: JPA entity doesn't match database schema

**Solution**:
```bash
# 1. Check entity definition
cat store-backend/src/main/java/com/comp5348/store/model/Order.java

# 2. Check migration SQL
cat store-backend/src/main/resources/db/migration/VX__*.sql

# 3. Verify names match:
#    - Table name: @Table(name = "orders") = CREATE TABLE orders
#    - Column names: @Column(name = "user_id") = user_id BIGINT
#    - Index names: @Index(name = "idx_x") = CREATE INDEX idx_x
```

### Problem: Tests fail after migration

**Cause**: Test data needs updating

**Solution**:
```java
// Update test fixtures to match new schema
@BeforeEach
void setUp() {
    Order order = Order.builder()
        .user(testUser)
        .product(testProduct)
        .quantity(1)
        .status("pending")  // New field added in migration
        .build();
    orderRepository.save(order);
}
```

---

## Reference

### Flyway Configuration Options

| Property | Value | Purpose |
|----------|-------|---------|
| `enabled` | `true` | Enable Flyway |
| `baseline-on-migrate` | `true` | Handle existing databases |
| `baseline-version` | `0` | Baseline version number |
| `locations` | `classpath:db/migration` | Migration file location |
| `validate-on-migrate` | `true` | Validate before applying |
| `out-of-order` | `false` | Require sequential migrations |
| `clean-disabled` | `true` | Safety: prevent database drops |

### Gradle Flyway Commands

```bash
# Apply migrations (usually auto-applied on startup)
./gradlew :store-backend:flywayMigrate

# Check migration status
./gradlew :store-backend:flywayInfo

# Validate applied migrations
./gradlew :store-backend:flywayValidate

# Repair schema history (use with caution)
./gradlew :store-backend:flywayRepair

# Clean database (DESTRUCTIVE - disabled by default)
# ./gradlew :store-backend:flywayClean
```

### Database Inspection Commands

```bash
# Connect to PostgreSQL
docker exec -it store-postgres psql -U postgres -d store_dev_db

# List all tables
\dt

# Describe table structure
\d table_name

# Check Flyway history
SELECT * FROM flyway_schema_history ORDER BY installed_rank;

# View constraints
\d+ table_name

# Exit psql
\q
```

### Migration File Template

```sql
-- =====================================================
-- Migration: VX - Brief Description
-- Description: Detailed explanation of what this migration does
-- Author: Your Name
-- Date: YYYY-MM-DD
-- Related Issue: #123 (if applicable)
-- =====================================================

-- Main migration logic
CREATE TABLE example (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_example_name ON example(name);

-- Constraints
ALTER TABLE example ADD CONSTRAINT chk_example_name_length
    CHECK (LENGTH(name) >= 3);

-- Foreign keys
ALTER TABLE example ADD CONSTRAINT fk_example_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- Comments
COMMENT ON TABLE example IS 'Brief table description';
COMMENT ON COLUMN example.name IS 'User-provided name field';
```

### Entity-to-Migration Mapping

| JPA Annotation | SQL Equivalent |
|----------------|----------------|
| `@Entity` | `CREATE TABLE` |
| `@Table(name = "x")` | `CREATE TABLE x` |
| `@Id` | `PRIMARY KEY` |
| `@GeneratedValue(IDENTITY)` | `BIGSERIAL` or `GENERATED BY DEFAULT AS IDENTITY` |
| `@Column(name = "x")` | Column `x` |
| `@Column(nullable = false)` | `NOT NULL` |
| `@Column(unique = true)` | `UNIQUE` constraint or unique index |
| `@ManyToOne` | Foreign key column |
| `@JoinColumn(name = "x")` | Column `x` |
| `@ForeignKey(name = "fk_x")` | `CONSTRAINT fk_x FOREIGN KEY` |
| `@Index(name = "idx_x")` | `CREATE INDEX idx_x` |

---

## Additional Resources

- [Flyway Documentation](https://flywaydb.org/documentation/)
- [Spring Boot Flyway Integration](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.data-initialization.migration-tool.flyway)
- [Flyway Best Practices](https://flywaydb.org/documentation/learnmore/bestpractices)
- [Migration Naming Convention](https://flywaydb.org/documentation/concepts/migrations#naming)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/current/)

---

**Questions or Issues?**

If you encounter problems not covered in this guide:
1. Check `tasks/store-backend/phase-3-flyway-migration.md` for detailed implementation notes
2. Review existing migrations for patterns: `store-backend/src/main/resources/db/migration/`
3. Ask the team for guidance on complex migrations
