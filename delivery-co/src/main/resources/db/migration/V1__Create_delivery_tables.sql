-- Delivery requests table
-- Tracks all the delivery info - customer details, address, status, progress
-- order_id is unique so we don't create duplicates

CREATE TABLE delivery_request (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE,
    customer_name VARCHAR(255) NOT NULL,
    customer_email VARCHAR(255) NOT NULL,
    address TEXT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    progress INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_progress CHECK (progress >= 0 AND progress <= 100),
    CONSTRAINT chk_status CHECK (status IN ('PENDING', 'IN_TRANSIT', 'DELIVERED', 'FAILED'))
);

-- Indexes to speed things up
CREATE INDEX idx_delivery_status ON delivery_request(status, created_at);
CREATE INDEX idx_delivery_order_id ON delivery_request(order_id);
CREATE INDEX idx_delivery_created_at ON delivery_request(created_at);

-- Shipment table
-- Tracks individual warehouse pickups for a delivery
-- One delivery can have multiple shipments from different warehouses
-- e.g. Order needs 5 items but warehouse A only has 3, warehouse B has 2
CREATE TABLE shipment (
    id BIGSERIAL PRIMARY KEY,
    delivery_request_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    progress INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_shipment_delivery FOREIGN KEY (delivery_request_id)
        REFERENCES delivery_request(id) ON DELETE CASCADE,
    CONSTRAINT chk_shipment_progress CHECK (progress >= 0 AND progress <= 100),
    CONSTRAINT chk_shipment_status CHECK (status IN ('PENDING', 'PICKED_UP', 'IN_TRANSIT', 'DELIVERED', 'FAILED'))
);

-- Shipment indexes
CREATE INDEX idx_shipment_delivery_request ON shipment(delivery_request_id);
CREATE INDEX idx_shipment_warehouse ON shipment(warehouse_id);
CREATE INDEX idx_shipment_status ON shipment(status, created_at);
CREATE INDEX idx_shipment_created_at ON shipment(created_at);

-- Email logs table
-- Records every email sent, includes retry_count for failed sends

CREATE TABLE email_log (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    email_type VARCHAR(50) NOT NULL,
    recipient VARCHAR(255) NOT NULL,
    subject VARCHAR(255),
    message_body TEXT,
    sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    error_message TEXT,
    retry_count INT DEFAULT 0,

    CONSTRAINT chk_email_status CHECK (status IN ('PENDING', 'SENT', 'FAILED')),
    CONSTRAINT chk_email_type CHECK (email_type IN (
        'ORDER_CREATED',
        'PAYMENT_SUCCESS',
        'DELIVERY_STARTED',
        'DELIVERY_IN_PROGRESS',
        'DELIVERY_COMPLETED',
        'DELIVERY_FAILED'
    ))
);

-- Email indexes
CREATE INDEX idx_email_order_id ON email_log(order_id, sent_at);
CREATE INDEX idx_email_status ON email_log(status);
CREATE INDEX idx_email_type ON email_log(email_type);
CREATE INDEX idx_email_sent_at ON email_log(sent_at DESC);

-- Processed messages table - for idempotency
-- Check this before processing RabbitMQ messages to avoid duplicates

CREATE TABLE processed_messages (
    id BIGSERIAL PRIMARY KEY,
    message_id VARCHAR(255) NOT NULL UNIQUE,
    service_name VARCHAR(50) NOT NULL,
    processed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_service_name CHECK (service_name IN ('DELIVERY', 'EMAIL'))
);

-- Indexes for message tracking
CREATE INDEX idx_processed_message_id ON processed_messages(message_id);
CREATE INDEX idx_processed_service ON processed_messages(service_name, processed_at);

-- Performance metrics table
-- Logs throughput, response times, error rates etc for monitoring

CREATE TABLE service_metrics (
    id BIGSERIAL PRIMARY KEY,
    service_name VARCHAR(50) NOT NULL,
    metric_type VARCHAR(50) NOT NULL,
    metric_value DECIMAL(10, 2) NOT NULL,
    recorded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_metric_service CHECK (service_name IN ('DELIVERY', 'EMAIL')),
    CONSTRAINT chk_metric_type CHECK (metric_type IN (
        'THROUGHPUT',
        'RESPONSE_TIME',
        'SUCCESS_RATE',
        'QUEUE_DEPTH',
        'ERROR_RATE'
    ))
);

-- Index for querying metrics by service and type
CREATE INDEX idx_metrics_service_type ON service_metrics(service_name, metric_type, recorded_at DESC);
