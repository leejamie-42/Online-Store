CREATE TABLE bpay_transaction_information (
    id BIGSERIAL PRIMARY KEY,
    reference_id VARCHAR(100) NOT NULL UNIQUE,
    biller_code VARCHAR(10) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'pending' 
        CHECK (status IN ('pending', 'paid', 'expired', 'cancelled')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expired_at TIMESTAMP NOT NULL,
    paid_at TIMESTAMP
);
CREATE INDEX idx_bpay_reference ON bpay_transaction_information(reference_id);
CREATE INDEX idx_bpay_status ON bpay_transaction_information(status);
CREATE INDEX idx_bpay_biller_code ON bpay_transaction_information(biller_code);

