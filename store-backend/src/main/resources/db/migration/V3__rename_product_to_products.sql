-- =====================================================
-- Migration: V3 - Rename product table to products
-- Description: Follow plural naming convention for tables
-- Author: System
-- Date: 2025-10-24
-- =====================================================

-- Rename table from product to products
ALTER TABLE product RENAME TO products;

-- Rename index to match new table name
ALTER INDEX idx_product_name RENAME TO idx_products_name;

-- Rename constraints to match new table name
ALTER TABLE products RENAME CONSTRAINT chk_product_price_positive TO chk_products_price_positive;
ALTER TABLE products RENAME CONSTRAINT chk_product_quantity_non_negative TO chk_products_quantity_non_negative;
