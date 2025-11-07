-- Add warehouse_address column to shipment table
-- Delivery drivers need actual addresses, not just warehouse IDs
ALTER TABLE shipment ADD COLUMN warehouse_address VARCHAR(500);

-- Add index for faster lookups by warehouse address
CREATE INDEX idx_shipment_warehouse_address ON shipment(warehouse_address);
