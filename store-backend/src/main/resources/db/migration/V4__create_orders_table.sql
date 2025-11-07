-- =====================================================
-- Migration: V4 - Create Orders Table
-- Description: Order management with user relationships and shipping information
-- Author: Store Backend Team
-- Date: 2025-10-27
-- =====================================================

-- Create orders table
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,

    -- Foreign key relationships
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,

    -- Order details
    quantity INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    total_amount DECIMAL(10, 2) NOT NULL,

    -- Shipping information
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    mobile_number VARCHAR(20) NOT NULL,
    address_line1 VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(3) NOT NULL,
    postcode VARCHAR(4) NOT NULL,
    country VARCHAR(100) NOT NULL DEFAULT 'Australia',

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign key constraints
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

-- Composite index for user's order history queries
CREATE INDEX idx_orders_user_created ON orders(user_id, created_at DESC);

-- Data integrity constraints
ALTER TABLE orders ADD CONSTRAINT chk_orders_quantity_positive
    CHECK (quantity > 0);

ALTER TABLE orders ADD CONSTRAINT chk_orders_total_positive
    CHECK (total_amount > 0);

-- Australian state validation
ALTER TABLE orders ADD CONSTRAINT chk_orders_state_valid
    CHECK (state IN ('NSW', 'VIC', 'QLD', 'SA', 'WA', 'TAS', 'NT', 'ACT'));

-- Australian postcode validation (4 digits)
ALTER TABLE orders ADD CONSTRAINT chk_orders_postcode_valid
    CHECK (postcode ~ '^\d{4}$');

-- Order status validation
ALTER TABLE orders ADD CONSTRAINT chk_orders_status_valid
    CHECK (status IN ('PENDING', 'PROCESSING', 'PICKED_UP', 'DELIVERING', 'DELIVERED', 'CANCELLED'));

-- Comments for documentation
COMMENT ON TABLE orders IS 'Customer orders with shipping information and status tracking';
COMMENT ON COLUMN orders.status IS 'Order status: PENDING → PROCESSING → PICKED_UP → DELIVERING → DELIVERED | CANCELLED';
COMMENT ON COLUMN orders.total_amount IS 'Total order amount calculated from product price * quantity';
COMMENT ON COLUMN orders.state IS 'Australian state abbreviation (NSW, VIC, QLD, SA, WA, TAS, NT, ACT)';
COMMENT ON COLUMN orders.postcode IS 'Australian postcode (4 digits)';
