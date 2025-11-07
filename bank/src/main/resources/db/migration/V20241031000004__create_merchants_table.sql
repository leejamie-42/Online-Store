CREATE TABLE merchants (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    biller_code VARCHAR(10) NOT NULL UNIQUE,
    account_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_merchants_customer FOREIGN KEY (customer_id)
        REFERENCES customers(id) ON DELETE RESTRICT,
    CONSTRAINT fk_merchants_account FOREIGN KEY (account_id) 
        REFERENCES accounts(id) ON DELETE RESTRICT
);
CREATE UNIQUE INDEX ux_merchants_biller_code ON merchants(biller_code);
CREATE INDEX idx_merchants_account_id ON merchants(account_id);
CREATE INDEX idx_merchants_customer_id ON merchants(customer_id);

