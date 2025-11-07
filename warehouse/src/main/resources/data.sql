-- ============================================================================
-- warehouse service seed data
-- ============================================================================
-- this file provides initial seed data for end-to-end testing from
-- frontend -> store-backend -> warehouse
--
-- usage:
--   1. via gradle task: ./gradlew :warehouse:reloadSeedData
--   2. manually in pgadmin: paste entire file contents and execute
--
-- both methods will clear existing data and reload fresh seed data
-- ============================================================================

-- ============================================================================
-- clear existing data (required for idempotent execution)
-- ============================================================================
-- truncate in correct order to avoid foreign key constraint violations
-- CASCADE automatically removes dependent records from related tables
TRUNCATE TABLE reservations CASCADE;
TRUNCATE TABLE inventory_transaction_record CASCADE;
TRUNCATE TABLE inventory CASCADE;
TRUNCATE TABLE warehouse_product CASCADE;
TRUNCATE TABLE warehouse CASCADE;

-- ============================================================================
-- warehouses
-- ============================================================================
-- sydney warehouse (primary)
insert into warehouse (id, version, name, description, address_line1, address_line2, country, city, suburb, postcode, created_at, updated_at)
values (1, 0, 'sydney distribution center', 'main distribution center serving nsw and act', 
        '123 george street', 'building a', 'australia', 'sydney', 'cbd', '2000', 
        current_timestamp, current_timestamp);

-- melbourne warehouse (secondary)
insert into warehouse (id, version, name, description, address_line1, address_line2, country, city, suburb, postcode, created_at, updated_at)
values (2, 0, 'melbourne distribution center', 'distribution center serving vic and tas', 
        '456 collins street', 'level 5', 'australia', 'melbourne', 'cbd', '3000', 
        current_timestamp, current_timestamp);

-- brisbane warehouse (tertiary)
insert into warehouse (id, version, name, description, address_line1, address_line2, country, city, suburb, postcode, created_at, updated_at)
values (3, 0, 'brisbane distribution center', 'distribution center serving qld and nt', 
        '789 queen street', 'suite 12', 'australia', 'brisbane', 'cbd', '4000', 
        current_timestamp, current_timestamp);

-- ============================================================================
-- warehouse products
-- ============================================================================
-- electronics category
insert into warehouse_product (id, version, name, description, price, image_url, published, created_at, updated_at)
values (1, 0, 'wireless bluetooth headphones', 
        'premium over-ear headphones with active noise cancellation, 30-hour battery life, and studio-quality sound.',
        149.99, 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500', 
        true, current_timestamp, current_timestamp);

insert into warehouse_product (id, version, name, description, price, image_url, published, created_at, updated_at)
values (2, 0, 'smart watch pro', 
        'advanced fitness tracker with heart rate monitor, gps, sleep tracking, and 7-day battery life.',
        299.99, 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=500', 
        true, current_timestamp, current_timestamp);

insert into warehouse_product (id, version, name, description, price, image_url, published, created_at, updated_at)
values (3, 0, 'portable bluetooth speaker', 
        'waterproof wireless speaker with 360-degree sound, 12-hour battery, and rugged design.',
        89.99, 'https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?w=500', 
        true, current_timestamp, current_timestamp);

-- home & living category
insert into warehouse_product (id, version, name, description, price, image_url, published, created_at, updated_at)
values (4, 0, 'stainless steel coffee maker', 
        'programmable 12-cup coffee maker with thermal carafe, auto-brew feature, and pause-and-serve function.',
        79.99, 'https://images.unsplash.com/photo-1517668808822-9ebb02f2a0e6?w=500', 
        true, current_timestamp, current_timestamp);

insert into warehouse_product (id, version, name, description, price, image_url, published, created_at, updated_at)
values (5, 0, 'led desk lamp', 
        'adjustable led desk lamp with touch control, usb charging port, and multiple brightness levels.',
        45.99, 'https://images.unsplash.com/photo-1507473885765-e6ed057f782c?w=500', 
        true, current_timestamp, current_timestamp);

-- fashion & accessories category
insert into warehouse_product (id, version, name, description, price, image_url, published, created_at, updated_at)
values (6, 0, 'leather crossbody bag', 
        'genuine leather crossbody bag with adjustable strap, multiple compartments, and elegant design.',
        129.99, 'https://images.unsplash.com/photo-1548036328-c9fa89d128fa?w=500', 
        true, current_timestamp, current_timestamp);

insert into warehouse_product (id, version, name, description, price, image_url, published, created_at, updated_at)
values (7, 0, 'polarized sunglasses', 
        'uv400 protection polarized sunglasses with lightweight frame and anti-glare coating.',
        59.99, 'https://images.unsplash.com/photo-1572635196237-14b3f281503f?w=500', 
        true, current_timestamp, current_timestamp);

-- sports & fitness category
insert into warehouse_product (id, version, name, description, price, image_url, published, created_at, updated_at)
values (8, 0, 'yoga mat premium', 
        'extra-thick non-slip yoga mat with carrying strap, eco-friendly material, and cushioned support.',
        39.99, 'https://images.unsplash.com/photo-1601925260368-ae2f83cf8b7f?w=500', 
        true, current_timestamp, current_timestamp);

insert into warehouse_product (id, version, name, description, price, image_url, published, created_at, updated_at)
values (9, 0, 'resistance bands set', 
        '5-piece resistance band set with varying resistance levels, door anchor, and carry bag.',
        29.99, 'https://images.unsplash.com/photo-1598971639058-fab3c3109a00?w=500', 
        true, current_timestamp, current_timestamp);

insert into warehouse_product (id, version, name, description, price, image_url, published, created_at, updated_at)
values (10, 0, 'stainless steel water bottle', 
        'vacuum-insulated water bottle keeps drinks cold for 24 hours or hot for 12 hours. bpa-free.',
        34.99, 'https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=500', 
        true, current_timestamp, current_timestamp);

-- home & living category (multi-warehouse demo product)
-- product id: 11 - premium laptop stand
-- distributed across all warehouses with limited stock per warehouse
-- designed to demonstrate multi-warehouse fulfillment when ordering 50+ units
insert into warehouse_product (id, version, name, description, price, image_url, published, created_at, updated_at)
values (11, 0, 'premium laptop stand', 
        'adjustable aluminum laptop stand with ergonomic design, ventilation slots, and foldable construction. fits laptops 10-17 inches.',
        79.99, 'https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=500', 
        true, current_timestamp, current_timestamp);

-- ============================================================================
-- inventory
-- ============================================================================
-- sydney warehouse inventory (warehouse id: 1)
-- electronics - high stock in sydney
insert into inventory (id, version, warehouse_id, product_id, quantity, created_at, updated_at)
values (1, 0, 1, 1, 150, current_timestamp, current_timestamp);  -- headphones

insert into inventory (id, version, warehouse_id, product_id, quantity, created_at, updated_at)
values (2, 0, 1, 2, 100, current_timestamp, current_timestamp);  -- smart watch

insert into inventory (id, version, warehouse_id, product_id, quantity, created_at, updated_at)
values (3, 0, 1, 3, 200, current_timestamp, current_timestamp);  -- bluetooth speaker

-- home & living
insert into inventory (id, version, warehouse_id, product_id, quantity, created_at, updated_at)
values (4, 0, 1, 4, 80, current_timestamp, current_timestamp);   -- coffee maker

insert into inventory (id, version, warehouse_id, product_id, quantity, created_at, updated_at)
values (5, 0, 1, 5, 120, current_timestamp, current_timestamp);  -- desk lamp

-- fashion & accessories
insert into inventory (id, version, warehouse_id, product_id, quantity, created_at, updated_at)
values (6, 0, 1, 6, 60, current_timestamp, current_timestamp);   -- leather bag

insert into inventory (id, version, warehouse_id, product_id, quantity, created_at, updated_at)
values (7, 0, 1, 7, 150, current_timestamp, current_timestamp);  -- sunglasses

-- sports & fitness
insert into inventory (id, version, warehouse_id, product_id, quantity, created_at, updated_at)
values (8, 0, 1, 8, 90, current_timestamp, current_timestamp);   -- yoga mat

insert into inventory (id, version, warehouse_id, product_id, quantity, created_at, updated_at)
values (9, 0, 1, 9, 180, current_timestamp, current_timestamp);  -- resistance bands

insert into inventory (id, version, warehouse_id, product_id, quantity, created_at, updated_at)
values (10, 0, 1, 10, 140, current_timestamp, current_timestamp); -- water bottle

-- melbourne warehouse inventory (warehouse id: 2)
-- electronics - medium stock in melbourne
insert into inventory (id, version, warehouse_id, product_id, quantity, created_at, updated_at)
values (11, 0, 2, 1, 80, current_timestamp, current_timestamp);  -- headphones

insert into inventory (id, version, warehouse_id, product_id, quantity, created_at, updated_at)
values (12, 0, 2, 2, 60, current_timestamp, current_timestamp);  -- smart watch

insert into inventory (id, version, warehouse_id, product_id, quantity, created_at, updated_at)
values (13, 0, 2, 3, 120, current_timestamp, current_timestamp); -- bluetooth speaker

-- home & living
insert into inventory (id, version, warehouse_id, product_id, quantity, created_at, updated_at)
values (14, 0, 2, 4, 50, current_timestamp, current_timestamp);  -- coffee maker

insert into inventory (id, version, warehouse_id, product_id, quantity, created_at, updated_at)
values (15, 0, 2, 5, 70, current_timestamp, current_timestamp);  -- desk lamp

-- fashion & accessories
insert into inventory (id, version, warehouse_id, product_id, quantity, created_at, updated_at)
values (16, 0, 2, 6, 40, current_timestamp, current_timestamp);  -- leather bag

insert into inventory (id, version, warehouse_id, product_id, quantity, created_at, updated_at)
values (17, 0, 2, 7, 90, current_timestamp, current_timestamp);  -- sunglasses

-- sports & fitness
insert into inventory (id, version, warehouse_id, product_id, quantity, created_at, updated_at)
values (18, 0, 2, 8, 60, current_timestamp, current_timestamp);  -- yoga mat

insert into inventory (id, version, warehouse_id, product_id, quantity, created_at, updated_at)
values (19, 0, 2, 9, 100, current_timestamp, current_timestamp); -- resistance bands

insert into inventory (id, version, warehouse_id, product_id, quantity, created_at, updated_at)
values (20, 0, 2, 10, 80, current_timestamp, current_timestamp); -- water bottle

-- brisbane warehouse inventory (warehouse id: 3)
-- electronics - lower stock in brisbane (backup warehouse)
insert into inventory (id, version, warehouse_id, product_id, quantity, created_at, updated_at)
values (21, 0, 3, 1, 50, current_timestamp, current_timestamp);  -- headphones

insert into inventory (id, version, warehouse_id, product_id, quantity, created_at, updated_at)
values (22, 0, 3, 2, 40, current_timestamp, current_timestamp);  -- smart watch

insert into inventory (id, version, warehouse_id, product_id, quantity, created_at, updated_at)
values (23, 0, 3, 3, 80, current_timestamp, current_timestamp);  -- bluetooth speaker

-- home & living
insert into inventory (id, version, warehouse_id, product_id, quantity, created_at, updated_at)
values (24, 0, 3, 4, 30, current_timestamp, current_timestamp);  -- coffee maker

insert into inventory (id, version, warehouse_id, product_id, quantity, created_at, updated_at)
values (25, 0, 3, 5, 50, current_timestamp, current_timestamp);  -- desk lamp

-- fashion & accessories
insert into inventory (id, version, warehouse_id, product_id, quantity, created_at, updated_at)
values (26, 0, 3, 6, 25, current_timestamp, current_timestamp);  -- leather bag

insert into inventory (id, version, warehouse_id, product_id, quantity, created_at, updated_at)
values (27, 0, 3, 7, 60, current_timestamp, current_timestamp);  -- sunglasses

-- sports & fitness
insert into inventory (id, version, warehouse_id, product_id, quantity, created_at, updated_at)
values (28, 0, 3, 8, 40, current_timestamp, current_timestamp);  -- yoga mat

insert into inventory (id, version, warehouse_id, product_id, quantity, created_at, updated_at)
values (29, 0, 3, 9, 70, current_timestamp, current_timestamp);  -- resistance bands

insert into inventory (id, version, warehouse_id, product_id, quantity, created_at, updated_at)
values (30, 0, 3, 10, 60, current_timestamp, current_timestamp); -- water bottle

-- home & living - multi-warehouse demo product (product id: 11)
-- premium laptop stand: distributed with limited stock per warehouse
-- total stock: 9 units (sydney: 4 + melbourne: 3 + brisbane: 2)
-- order 7-8 units will require fulfillment from all 3 warehouses
insert into inventory (id, version, warehouse_id, product_id, quantity, created_at, updated_at)
values (31, 0, 1, 11, 4, current_timestamp, current_timestamp);  -- sydney: 4 units

insert into inventory (id, version, warehouse_id, product_id, quantity, created_at, updated_at)
values (32, 0, 2, 11, 3, current_timestamp, current_timestamp);  -- melbourne: 3 units

insert into inventory (id, version, warehouse_id, product_id, quantity, created_at, updated_at)
values (33, 0, 3, 11, 2, current_timestamp, current_timestamp);  -- brisbane: 2 units

-- ============================================================================
-- reset sequences (postgresql)
-- ============================================================================
-- this ensures that future inserts will use ids starting after the seed data
select setval('warehouse_seq', 3, true);
select setval('warehouse_product_seq', 11, true);
select setval('inventory_seq', 33, true);

-- ============================================================================
-- seed data summary
-- ============================================================================
-- warehouses: 3 (sydney, melbourne, brisbane)
-- products: 11 (across electronics, home, fashion, sports categories)
-- inventory records: 33 (11 products × 3 warehouses)
--
-- total stock by product:
-- 1. wireless bluetooth headphones: 280 units (150+80+50)
-- 2. smart watch pro: 200 units (100+60+40)
-- 3. portable bluetooth speaker: 400 units (200+120+80)
-- 4. stainless steel coffee maker: 160 units (80+50+30)
-- 5. led desk lamp: 240 units (120+70+50)
-- 6. leather crossbody bag: 125 units (60+40+25)
-- 7. polarized sunglasses: 300 units (150+90+60)
-- 8. yoga mat premium: 190 units (90+60+40)
-- 9. resistance bands set: 350 units (180+100+70)
-- 10. stainless steel water bottle: 280 units (140+80+60)
-- 11. premium laptop stand: 9 units (4+3+2) [MULTI-WAREHOUSE DEMO]
--    └─ order 7-8 units demonstrates fulfillment from all 3 warehouses
-- ============================================================================

