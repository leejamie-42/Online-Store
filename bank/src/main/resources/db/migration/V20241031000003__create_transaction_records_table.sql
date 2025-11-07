CREATE TABLE transaction_records (
    id BIGSERIAL PRIMARY KEY,
    amount DECIMAL(15,2) NOT NULL,
    memo TEXT,
    to_account BIGINT,
    from_account BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'initiated' 
        CHECK (status IN ('initiated', 'processing', 'completed', 'failed')),
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transaction_to_account FOREIGN KEY (to_account) 
        REFERENCES accounts(id) ON DELETE SET NULL,
    CONSTRAINT fk_transaction_from_account FOREIGN KEY (from_account) 
        REFERENCES accounts(id) ON DELETE SET NULL
);
CREATE INDEX idx_transaction_to_account ON transaction_records(to_account);
CREATE INDEX idx_transaction_from_account ON transaction_records(from_account);

