-- V20251029030234__create_shipments_table.sql
-- Generated via: ./gradlew generateMigration -PmgName=create_shipments_table
CREATE TABLE IF NOT EXISTS shipments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE,
    shipment_id VARCHAR(100) NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL,
    carrier VARCHAR(100),
    tracking_number VARCHAR(100),
    estimated_delivery TIMESTAMP,
    actual_delivery TIMESTAMP,
    current_warehouse_id BIGINT,
    pickup_path TEXT,  -- Format: "WH-1->WH-2->WH-3" (tracks multi-warehouse pickup journey)
    delivery_address TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),

    CONSTRAINT fk_shipments_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT chk_shipment_status CHECK (status IN ('PROCESSING', 'PICKED_UP', 'IN_TRANSIT', 'DELIVERED', 'LOST'))
);

CREATE INDEX idx_shipments_order_id ON shipments(order_id);
CREATE INDEX idx_shipments_shipment_id ON shipments(shipment_id);
CREATE INDEX idx_shipments_status ON shipments(status);
CREATE INDEX idx_shipments_current_warehouse ON shipments(current_warehouse_id);
CREATE INDEX idx_shipments_created_at ON shipments(created_at);

COMMENT ON TABLE shipments IS 'Tracks delivery shipments for orders via DeliveryCo service';
COMMENT ON COLUMN shipments.shipment_id IS 'External DeliveryCo shipment identifier';
COMMENT ON COLUMN shipments.status IS 'Current delivery status from DeliveryCo webhooks';
COMMENT ON COLUMN shipments.current_warehouse_id IS 'Next warehouse to pick up from (for multi-warehouse fulfillment)';
COMMENT ON COLUMN shipments.pickup_path IS 'Warehouse pickup journey path (e.g., "WH-1->WH-2->WH-3")';
