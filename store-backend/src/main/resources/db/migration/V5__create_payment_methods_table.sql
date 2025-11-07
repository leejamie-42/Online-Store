-- =====================================================
-- Migration: V5 - Create Payment Methods Table
-- Description: Payment method types and metadata
-- =====================================================

CREATE TABLE payment_methods (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(20) NOT NULL,           -- 'BPAY'
    payload ${jsonb_type},               -- BPAY metadata (biller_code, reference)
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_payment_method_type CHECK (type IN ('BPAY'))
);

-- Performance indexes
CREATE INDEX idx_payment_methods_type ON payment_methods(type);
