-- =====================================================
-- Migration: V2 - Create Product Table
-- Description: Creates the product table for product catalog
-- Author: System
-- Date: 2025-10-24
-- =====================================================

-- Create product table
-- Maps to: com.comp5348.store.model.Product
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

-- Create index for product name searches
CREATE INDEX idx_product_name ON product(name);

-- Add check constraints for data integrity (separate statements for H2 compatibility)
ALTER TABLE product ADD CONSTRAINT chk_product_price_positive CHECK (price > 0);
ALTER TABLE product ADD CONSTRAINT chk_product_quantity_non_negative CHECK (quantity >= 0);
