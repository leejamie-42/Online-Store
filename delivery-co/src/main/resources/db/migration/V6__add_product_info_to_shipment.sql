-- Add product_id and quantity columns to shipment table
-- Each shipment now tracks one specific product with its quantity
ALTER TABLE shipment ADD COLUMN product_id VARCHAR(100);
ALTER TABLE shipment ADD COLUMN quantity INTEGER;

-- Add index for faster lookups by product_id
CREATE INDEX idx_shipment_product_id ON shipment(product_id);
