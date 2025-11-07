-- ============================================================================
-- store-backend service seed data
-- ============================================================================
-- this file provides initial seed data for the product catalog
-- aligned with warehouse service's warehouse_product and inventory
--
-- data source: warehouse/src/main/resources/data.sql
-- product ids: 1-10 (matching warehouse_product ids)
-- quantities: aggregated totals from all warehouse inventories
--
-- usage:
--   1. via gradle task: ./gradlew :store-backend:reloadSeedData
--   2. manually in pgadmin: paste entire file contents and execute
--
-- both methods will clear existing products and reload fresh seed data
-- note: CASCADE automatically removes dependent records (orders, payments, etc.)
-- ============================================================================

-- ============================================================================
-- clear existing data (required for idempotent execution)
-- ============================================================================
-- CASCADE automatically removes dependent records from orders, payments,
-- shipments, refunds, and order_items tables
TRUNCATE TABLE products CASCADE;

-- ============================================================================
-- products (aligned with warehouse service)
-- ============================================================================
-- table name: products (after V3__rename_product_to_products.sql migration)
-- quantities represent total stock across all warehouses (sydney + melbourne + brisbane)

-- electronics category
-- product id: 1 - wireless bluetooth headphones
-- total stock: 280 units (sydney: 150 + melbourne: 80 + brisbane: 50)
INSERT INTO products (id, name, description, price, image_url, quantity, created_at, updated_at)
VALUES (1, 'wireless bluetooth headphones', 
        'premium over-ear headphones with active noise cancellation, 30-hour battery life, and studio-quality sound.',
        149.99, 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500', 
        280, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- product id: 2 - smart watch pro
-- total stock: 200 units (sydney: 100 + melbourne: 60 + brisbane: 40)
INSERT INTO products (id, name, description, price, image_url, quantity, created_at, updated_at)
VALUES (2, 'smart watch pro', 
        'advanced fitness tracker with heart rate monitor, gps, sleep tracking, and 7-day battery life.',
        299.99, 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=500', 
        200, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- product id: 3 - portable bluetooth speaker
-- total stock: 400 units (sydney: 200 + melbourne: 120 + brisbane: 80)
INSERT INTO products (id, name, description, price, image_url, quantity, created_at, updated_at)
VALUES (3, 'portable bluetooth speaker', 
        'waterproof wireless speaker with 360-degree sound, 12-hour battery, and rugged design.',
        89.99, 'https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?w=500', 
        400, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- home & living category
-- product id: 4 - stainless steel coffee maker
-- total stock: 160 units (sydney: 80 + melbourne: 50 + brisbane: 30)
INSERT INTO products (id, name, description, price, image_url, quantity, created_at, updated_at)
VALUES (4, 'stainless steel coffee maker', 
        'programmable 12-cup coffee maker with thermal carafe, auto-brew feature, and pause-and-serve function.',
        79.99, 'https://images.unsplash.com/photo-1517668808822-9ebb02f2a0e6?w=500', 
        160, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- product id: 5 - led desk lamp
-- total stock: 240 units (sydney: 120 + melbourne: 70 + brisbane: 50)
INSERT INTO products (id, name, description, price, image_url, quantity, created_at, updated_at)
VALUES (5, 'led desk lamp', 
        'adjustable led desk lamp with touch control, usb charging port, and multiple brightness levels.',
        45.99, 'https://images.unsplash.com/photo-1507473885765-e6ed057f782c?w=500', 
        240, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- fashion & accessories category
-- product id: 6 - leather crossbody bag
-- total stock: 125 units (sydney: 60 + melbourne: 40 + brisbane: 25)
INSERT INTO products (id, name, description, price, image_url, quantity, created_at, updated_at)
VALUES (6, 'leather crossbody bag', 
        'genuine leather crossbody bag with adjustable strap, multiple compartments, and elegant design.',
        129.99, 'https://images.unsplash.com/photo-1548036328-c9fa89d128fa?w=500', 
        125, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- product id: 7 - polarized sunglasses
-- total stock: 300 units (sydney: 150 + melbourne: 90 + brisbane: 60)
INSERT INTO products (id, name, description, price, image_url, quantity, created_at, updated_at)
VALUES (7, 'polarized sunglasses', 
        'uv400 protection polarized sunglasses with lightweight frame and anti-glare coating.',
        59.99, 'https://images.unsplash.com/photo-1572635196237-14b3f281503f?w=500', 
        300, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- sports & fitness category
-- product id: 8 - yoga mat premium
-- total stock: 190 units (sydney: 90 + melbourne: 60 + brisbane: 40)
INSERT INTO products (id, name, description, price, image_url, quantity, created_at, updated_at)
VALUES (8, 'yoga mat premium', 
        'extra-thick non-slip yoga mat with carrying strap, eco-friendly material, and cushioned support.',
        39.99, 'https://images.unsplash.com/photo-1601925260368-ae2f83cf8b7f?w=500', 
        190, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- product id: 9 - resistance bands set
-- total stock: 350 units (sydney: 180 + melbourne: 100 + brisbane: 70)
INSERT INTO products (id, name, description, price, image_url, quantity, created_at, updated_at)
VALUES (9, 'resistance bands set', 
        '5-piece resistance band set with varying resistance levels, door anchor, and carry bag.',
        29.99, 'https://images.unsplash.com/photo-1598971639058-fab3c3109a00?w=500', 
        350, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- product id: 10 - stainless steel water bottle
-- total stock: 280 units (sydney: 140 + melbourne: 80 + brisbane: 60)
INSERT INTO products (id, name, description, price, image_url, quantity, created_at, updated_at)
VALUES (10, 'stainless steel water bottle', 
        'vacuum-insulated water bottle keeps drinks cold for 24 hours or hot for 12 hours. bpa-free.',
        34.99, 'https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=500', 
        280, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- home & living category (multi-warehouse demo product)
-- product id: 11 - premium laptop stand
-- total stock: 9 units (sydney: 4 + melbourne: 3 + brisbane: 2)
-- designed to demonstrate multi-warehouse fulfillment when ordering 7-8 units
INSERT INTO products (id, name, description, price, image_url, quantity, created_at, updated_at)
VALUES (11, 'premium laptop stand', 
        'adjustable aluminum laptop stand with ergonomic design, ventilation slots, and foldable construction. fits laptops 10-17 inches.',
        79.99, 'https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=500', 
        9, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ============================================================================
-- reset sequence (postgresql)
-- ============================================================================
-- this ensures that future inserts will use ids starting after the seed data
-- note: sequence is named 'product_id_seq' (singular) because table was
-- originally 'product' and sequences don't auto-rename with ALTER TABLE RENAME
SELECT setval('product_id_seq', 10, true);

-- ============================================================================
-- seed data summary
-- ============================================================================
-- products: 10 (matching warehouse_product ids 1-10)
-- categories: electronics (3), home & living (2), fashion (2), sports & fitness (3)
--
-- product stock quantities:
-- 1. wireless bluetooth headphones: 280 units
-- 2. smart watch pro: 200 units
-- 3. portable bluetooth speaker: 400 units
-- 4. stainless steel coffee maker: 160 units
-- 5. led desk lamp: 240 units
-- 6. leather crossbody bag: 125 units
-- 7. polarized sunglasses: 300 units
-- 8. yoga mat premium: 190 units
-- 9. resistance bands set: 350 units
-- 10. stainless steel water bottle: 280 units
--
-- note: quantities represent aggregated totals from warehouse inventory
-- across all warehouses (sydney + melbourne + brisbane)
-- runtime sync handled by rabbitmq ProductUpdateEvent from warehouse service
-- ============================================================================

