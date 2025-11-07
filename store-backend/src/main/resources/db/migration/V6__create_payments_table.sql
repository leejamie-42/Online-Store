-- =====================================================
-- Migration: V6 - Create Payments Table
-- Description: Payment transaction records
-- =====================================================

CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    amount DECIMAL(10,2) NOT NULL,
    order_id BIGINT NOT NULL UNIQUE,     -- One payment per order
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    method_id BIGINT NOT NULL,

    -- Bank service reference
    bank_payment_id VARCHAR(100),        -- External payment ID from Bank

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,

    -- Foreign keys
    CONSTRAINT fk_payments_order FOREIGN KEY (order_id)
        REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_payments_method FOREIGN KEY (method_id)
        REFERENCES payment_methods(id) ON DELETE RESTRICT,

    -- Constraints
    CONSTRAINT chk_payments_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_payments_status CHECK (status IN
        ('pending', 'processing', 'completed', 'failed', 'refunded'))
);

-- Performance indexes
CREATE INDEX idx_payments_order_id ON payments(order_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_bank_payment_id ON payments(bank_payment_id);
