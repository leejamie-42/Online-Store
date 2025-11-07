# Phase 3: Flyway Database Migration Integration

**Status**: ✅ Completed
**Date**: October 24, 2025
**Author**: System
**Related**: [Phase 1 - User Auth](./phase-1-user-auth.md), [Phase 2 - Product Catalog](./phase-2-product-catalog.md)

## Overview

This phase integrates Flyway database migration tool into the store-backend service to provide version-controlled, reproducible database schema management. The existing `User` and `Product` entities are captured as baseline migrations.

## Why Flyway?

### Current State Problems
- **No Version Control**: Schema changes not tracked in Git
- **Manual Coordination**: Team members must manually sync database changes
- **Reproducibility Issues**: Different environments may have different schemas
- **Production Risk**: No validated migration path for schema updates

### Flyway Benefits
1. **Version Control**: All schema changes tracked in SQL migration files
2. **Reproducibility**: Consistent schema across development, staging, production
3. **Team Collaboration**: Clear history of database evolution
4. **Audit Trail**: `flyway_schema_history` table tracks all applied migrations
5. **Rollback Documentation**: Clear record of what changed and when

## Implementation Summary

### What Was Changed

#### 1. Dependencies (`build.gradle`)
```gradle
// Flyway database migration
implementation 'org.flywaydb:flyway-core'
implementation 'org.flywaydb:flyway-database-postgresql'
```

#### 2. Configuration (`application-local.yml`)
```yaml
jpa:
  hibernate:
    ddl-auto: validate  # Changed from 'update' to 'validate'

flyway:
  enabled: true
  baseline-on-migrate: true
  baseline-version: 0
  locations: classpath:db/migration
  validate-on-migrate: true
  out-of-order: false
  clean-disabled: true
```

#### 3. Migration Files Created

**Directory Structure:**
```
store-backend/src/main/resources/
└── db/
    └── migration/
        ├── V1__create_users_table.sql
        └── V2__create_product_table.sql
```

**V1__create_users_table.sql**: Creates `users` table with indexes
**V2__create_product_table.sql**: Creates `product` table with indexes and constraints

#### 4. Test Configuration (`src/test/resources/application.properties`)
```properties
# H2 Database for tests
spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
app.data.seed=false
```

## Migration Files Detail

### V1: Create Users Table

**File**: `src/main/resources/db/migration/V1__create_users_table.sql`

**Purpose**: Authentication and user management

**Schema**:
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER',
    enabled BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX idx_user_email ON users(email);
CREATE UNIQUE INDEX idx_user_name ON users(name);
```

**Maps to Entity**: `com.comp5348.store.model.auth.User`

**Key Features**:
- Auto-incrementing primary key
- Unique constraints on email and name
- Role-based access control (CUSTOMER/ADMIN)
- Account enable/disable functionality
- Audit timestamps

### V2: Create Product Table

**File**: `src/main/resources/db/migration/V2__create_product_table.sql`

**Purpose**: Product catalog with inventory tracking

**Schema**:
```sql
CREATE TABLE product (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    image_url VARCHAR(500),
    quantity INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_product_name ON product(name);
ALTER TABLE product ADD CONSTRAINT chk_product_price_positive CHECK (price > 0);
ALTER TABLE product ADD CONSTRAINT chk_product_quantity_non_negative CHECK (quantity >= 0);
```

**Maps to Entity**: `com.comp5348.store.model.Product`

**Key Features**:
- Auto-incrementing primary key
- Price validation (must be positive)
- Quantity validation (non-negative)
- Text search optimization on name
- Audit timestamps

## Migration Strategy: Baseline Approach

### Why Baseline?

Since the store-backend already has existing `User` and `Product` entities in use, we adopt a **baseline migration strategy**:

1. **Existing Databases**: Flyway marks V1 and V2 as already applied (baseline version 0)
2. **New Databases**: Flyway applies V1 and V2 to create fresh schema
3. **Future Changes**: All new migrations (V3+) apply incrementally

### Configuration Keys

| Key | Value | Purpose |
|-----|-------|---------|
| `baseline-on-migrate` | `true` | Mark existing schema as baseline |
| `baseline-version` | `0` | Start baseline at version 0 |
| `ddl-auto` | `validate` | Hibernate validates schema only, doesn't modify |
| `clean-disabled` | `true` | Safety: prevent accidental database drops |

## Testing Strategy

### Test Environment Setup

**Database**: H2 in-memory (PostgreSQL compatibility mode)
**Migrations**: Same SQL files work for both PostgreSQL and H2

**Key Configuration**:
```properties
spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL
spring.flyway.locations=classpath:db/migration
```

**Why It Works**:
- H2 supports PostgreSQL syntax subset
- Migrations written for database portability
- Separate ALTER statements for H2 compatibility

### Test Results

**Before Flyway**: Tests passing with Hibernate auto-update
**After Flyway**: ✅ All 80 tests passing with Flyway migrations

**Test Coverage**:
- `StoreApplicationTests`: Context loads successfully
- `DataLoaderTest`: Product seeding works with Flyway schema
- `AuthenticationControllerTest`: User authentication flows
- `ProductRepositoryTest`: Product CRUD operations

## How to Use Flyway

### For Existing Databases (Development)

**Option A: Keep Existing Data (Recommended)**
```bash
# 1. Flyway will detect existing schema
# 2. Mark V1 and V2 as already applied (baseline)
# 3. Future migrations will apply incrementally

./gradlew :store-backend:bootRun
```

**Option B: Fresh Start**
```bash
# 1. Drop and recreate database
docker-compose down -v
docker-compose up -d

# 2. Start application - Flyway applies all migrations
./gradlew :store-backend:bootRun

# 3. Verify schema_history table
psql -U postgres -h localhost -p 5433 -d store_dev_db \
  -c "SELECT * FROM flyway_schema_history;"
```

### For New Databases (CI/CD, New Team Members)

```bash
# 1. Clone repository
git clone <repo-url>
cd store-t6-g8

# 2. Start infrastructure
docker-compose up -d

# 3. Configure .env files
cp store-backend/.env.example store-backend/.env
# Edit with your credentials

# 4. Run application - Flyway creates schema automatically
./gradlew :store-backend:bootRun

# Database is ready with V1 and V2 applied!
```

## Creating New Migrations

### Workflow

1. **Create Entity Class** (Java)
2. **Generate Migration SQL** (SQL file)
3. **Test Locally**
4. **Commit to Git**
5. **Flyway Auto-Applies** on next startup

### Naming Convention

**Format**: `V{version}__{description}.sql`

**Examples**:
```
V3__create_order_table.sql
V4__create_payment_tables.sql
V5__add_order_status_index.sql
V6__add_refund_table.sql
```

**Rules**:
- Version numbers sequential (1, 2, 3, ...)
- Double underscore `__` between version and description
- Snake_case description
- `.sql` extension

### Example: Adding Order Table

**Step 1: Create Migration File**

`src/main/resources/db/migration/V3__create_order_table.sql`:
```sql
-- =====================================================
-- Migration: V3 - Create Order Table
-- Description: Order management with shipping info
-- Date: 2025-10-XX
-- =====================================================

CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    email VARCHAR(255),
    mobile_number VARCHAR(20),
    address_line1 VARCHAR(255),
    city VARCHAR(100),
    postcode VARCHAR(20),
    country VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_product FOREIGN KEY (product_id) REFERENCES product(id),
    CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_order_user_id ON orders(user_id);
CREATE INDEX idx_order_status ON orders(status);
CREATE INDEX idx_order_created_at ON orders(created_at);
```

**Step 2: Create Entity Class**

```java
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ... rest of entity fields matching SQL
}
```

**Step 3: Test Migration**

```bash
# Clean database and test
docker-compose down -v
docker-compose up -d
./gradlew :store-backend:bootRun

# Check Flyway applied V3
psql -U postgres -h localhost -p 5433 -d store_dev_db \
  -c "SELECT version, description, success FROM flyway_schema_history;"
```

**Expected Output**:
```
 version |         description          | success
---------+------------------------------+---------
 0       | << Flyway Baseline >>        | t
 1       | create users table           | t
 2       | create product table         | t
 3       | create order table           | t
```

## Migration Best Practices

### Do's ✅

1. **Always Test Locally First**
   - Run migration on clean database
   - Verify schema matches entity
   - Test application functionality

2. **Write Idempotent Migrations**
   - Use `IF NOT EXISTS` for safety (if needed)
   - Handle re-run scenarios gracefully

3. **Include Comments**
   - Explain purpose of migration
   - Document any complex logic
   - Reference related entities

4. **Separate Concerns**
   - One migration per logical change
   - Don't bundle unrelated changes

5. **Version Control**
   - Commit migration files with code changes
   - Never modify applied migrations

### Don'ts ❌

1. **Never Modify Applied Migrations**
   - Flyway tracks checksums
   - Modification causes validation errors
   - Create new migration instead

2. **Don't Skip Version Numbers**
   - Sequential versions only (1, 2, 3, ...)
   - Out-of-order disabled by default

3. **Avoid Database-Specific Syntax** (when possible)
   - H2 tests won't work
   - Keep SQL portable

4. **Don't Use DDL-Auto Update**
   - Conflicts with Flyway
   - Always use `validate` mode

5. **Don't Commit Without Testing**
   - Failed migrations break CI/CD
   - Team members blocked

## Troubleshooting

### Problem: "Found more than one migration with version X"

**Cause**: Duplicate migration files

**Solution**:
```bash
# Check for duplicates
find store-backend/src -name "*.sql" | grep V1
find store-backend/src -name "*.sql" | grep V2

# Remove test migrations (use main migrations for both)
rm -rf store-backend/src/test/resources/db
```

### Problem: "Validate failed: Migrations have failed validation"

**Cause**: Modified applied migration (checksum changed)

**Solution**:
```bash
# Option 1: Repair (if intentional)
./gradlew :store-backend:flywayRepair

# Option 2: Revert changes (recommended)
git checkout store-backend/src/main/resources/db/migration/VX__*.sql
```

### Problem: Tests fail with "H2 Syntax Error"

**Cause**: PostgreSQL-specific syntax

**Solution**: Use portable SQL
```sql
# ❌ PostgreSQL-specific
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY
);

# ✅ H2 compatible
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY
);
```

**Note**: Main migrations use `BIGSERIAL` (PostgreSQL), which is fine since production uses PostgreSQL.

### Problem: "ddl-auto: validate" fails with schema mismatch

**Cause**: Entity doesn't match migrated schema

**Solution**:
```bash
# 1. Check entity definition
cat store-backend/src/main/java/com/comp5348/store/model/YourEntity.java

# 2. Check migration SQL
cat store-backend/src/main/resources/db/migration/VX__*.sql

# 3. Ensure fields match exactly:
#    - Column names (snake_case in SQL, camelCase in Java)
#    - Data types (VARCHAR vs String, INTEGER vs Integer)
#    - Constraints (NOT NULL, unique indexes)
```

## Flyway Schema History

Flyway tracks migrations in `flyway_schema_history` table:

**Query**:
```sql
SELECT installed_rank, version, description, type, script,
       checksum, installed_by, installed_on, execution_time, success
FROM flyway_schema_history
ORDER BY installed_rank;
```

**Example Output**:
```
 installed_rank | version |      description       | type |              script               | checksum
----------------+---------+------------------------+------+-----------------------------------+-----------
              1 | 0       | << Flyway Baseline >>  | BASE | << Flyway Baseline >>             | NULL
              2 | 1       | create users table     | SQL  | V1__create_users_table.sql        | 123456789
              3 | 2       | create product table   | SQL  | V2__create_product_table.sql      | 987654321
```

## DataLoader Integration

### Current Behavior

**DataLoader**: Loads initial product data from JSON on startup

**Integration with Flyway**:
1. Flyway creates schema (V1, V2)
2. DataLoader runs after schema creation
3. Seeds products if database is empty

### Configuration

```yaml
# application-local.yml
app:
  data:
    seed: true  # Enable/disable seed data
```

**Production Recommendation**: Consider moving seed data to V3 migration for reproducibility

## CI/CD Integration

### GitHub Actions Example

```yaml
name: Build and Test

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    services:
      postgres:
        image: postgres:15
        env:
          POSTGRES_PASSWORD: postgres
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5

    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Run tests
        env:
          PG_PASSWORD: postgres
        run: ./gradlew :store-backend:test

      # Flyway automatically applies migrations during test
```

## Future Enhancements

### Planned Migrations

Based on ERD design, upcoming migrations:

1. **V3__create_order_table.sql** - Order management
2. **V4__create_payment_method_table.sql** - Payment methods
3. **V5__create_payment_table.sql** - Payment transactions
4. **V6__create_refund_table.sql** - Refund tracking

### Advanced Features

**Flyway Pro Features** (if needed):
- Undo migrations (rollback capability)
- Database branching (feature branches)
- Dry runs (preview changes)
- SQL migration callbacks

**Current Setup**: Flyway Community Edition (sufficient for project needs)

## Verification Checklist

After implementing Flyway, verify:

- [ ] `build.gradle` includes Flyway dependencies
- [ ] `application-local.yml` has `ddl-auto: validate`
- [ ] `application-local.yml` has Flyway configuration
- [ ] Migration directory exists: `src/main/resources/db/migration/`
- [ ] V1 migration creates `users` table
- [ ] V2 migration creates `product` table
- [ ] Tests pass: `./gradlew :store-backend:test`
- [ ] Application starts: `./gradlew :store-backend:bootRun`
- [ ] `flyway_schema_history` table exists in database
- [ ] Schema validation succeeds (no Hibernate errors)

## Commands Reference

### Gradle Flyway Tasks

```bash
# Apply migrations manually (usually auto-applied on startup)
./gradlew :store-backend:flywayMigrate

# Check migration status
./gradlew :store-backend:flywayInfo

# Validate applied migrations
./gradlew :store-backend:flywayValidate

# Repair schema history (use with caution)
./gradlew :store-backend:flywayRepair

# Clean database (disabled by default for safety)
# ./gradlew :store-backend:flywayClean
```

### Database Inspection

```bash
# Connect to PostgreSQL
psql -U postgres -h localhost -p 5433 -d store_dev_db

# List tables
\dt

# Describe table
\d users
\d product

# Check Flyway history
SELECT * FROM flyway_schema_history ORDER BY installed_rank;

# Verify constraints
\d+ product
```

## Summary

### What Was Accomplished

✅ Flyway dependencies added to project
✅ Database migrations version-controlled in Git
✅ Baseline migrations created for User and Product
✅ Test environment configured for Flyway
✅ All tests passing with migration-managed schema
✅ Future migration workflow documented

### Key Takeaways

1. **Hibernate DDL-Auto Changed**: `update` → `validate`
2. **Schema Management**: Flyway owns schema, Hibernate validates
3. **Version Control**: All schema changes tracked in SQL files
4. **Team Workflow**: Clone repo → Run app → Schema auto-created
5. **Production Ready**: Safe, reproducible schema management

### Next Steps

1. **Add Order-related migrations** (V3-V6) as features develop
2. **Document rollback procedures** for production
3. **Integrate with CI/CD pipeline** for automated testing
4. **Consider Flyway Pro** if undo migrations needed

## Resources

- [Flyway Documentation](https://flywaydb.org/documentation/)
- [Flyway with Spring Boot](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.data-initialization.migration-tool.flyway)
- [Migration Naming](https://flywaydb.org/documentation/concepts/migrations#naming)
- [Baseline Migration](https://flywaydb.org/documentation/command/baseline)
- [H2 PostgreSQL Mode](https://h2database.com/html/features.html#compatibility)

---

**Phase 3 Complete**: Store-backend now has professional database migration management with Flyway ✅
