-- =====================================================
-- Migration: V7 - Create Refunds Table
-- Description: Refund transaction records
-- =====================================================

CREATE TABLE refunds (
    id BIGSERIAL PRIMARY KEY,
    payment_id BIGINT NOT NULL UNIQUE,   -- One refund per payment
    transaction_id BIGINT,               -- Bank transaction reference
    amount DECIMAL(15,2) NOT NULL,
    reason TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'pending',

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    refunded_at TIMESTAMP,

    -- Foreign key
    CONSTRAINT fk_refunds_payment FOREIGN KEY (payment_id)
        REFERENCES payments(id) ON DELETE CASCADE,

    -- Constraints
    CONSTRAINT chk_refunds_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_refunds_status CHECK (status IN
        ('pending', 'processing', 'completed', 'failed'))
);

-- Performance indexes
CREATE INDEX idx_refunds_payment_id ON refunds(payment_id);
CREATE INDEX idx_refunds_status ON refunds(status);
