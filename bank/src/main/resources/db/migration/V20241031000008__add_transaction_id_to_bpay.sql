-- =====================================================
-- Migration: V20241031000008 - Add Transaction ID to BPAY
-- Description: Establish One-to-One relationship between
--              BPAY payments and transaction records
-- =====================================================

-- Add transaction_id column to bpay_transaction_information
ALTER TABLE bpay_transaction_information
ADD COLUMN transaction_id BIGINT;

-- Create foreign key constraint to transaction_records
ALTER TABLE bpay_transaction_information
ADD CONSTRAINT fk_bpay_transaction
    FOREIGN KEY (transaction_id)
    REFERENCES transaction_records(id)
    ON DELETE SET NULL;

-- Create index for query performance
CREATE INDEX idx_bpay_transaction_id ON bpay_transaction_information(transaction_id);

-- Add comment for documentation
COMMENT ON COLUMN bpay_transaction_information.transaction_id IS
'References the transaction_record created when customer pays via BPAY. Used for refund reversal.';
