-- Add transfer-related columns to transactions table
ALTER TABLE banking_schema.transactions 
ADD COLUMN destination_account_id BIGINT,
ADD COLUMN source_account_id BIGINT;

-- Add indexes for better performance on transfer queries
CREATE INDEX idx_transaction_destination_account_id ON banking_schema.transactions(destination_account_id);
CREATE INDEX idx_transaction_source_account_id ON banking_schema.transactions(source_account_id);

-- Add foreign key constraints for referential integrity
ALTER TABLE banking_schema.transactions 
ADD CONSTRAINT fk_transaction_destination_account 
FOREIGN KEY (destination_account_id) REFERENCES banking_schema.accounts(id);

ALTER TABLE banking_schema.transactions 
ADD CONSTRAINT fk_transaction_source_account 
FOREIGN KEY (source_account_id) REFERENCES banking_schema.accounts(id);
