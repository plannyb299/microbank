-- Create accounts table
CREATE TABLE accounts (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL,
    account_number VARCHAR(20) NOT NULL UNIQUE,
    balance DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    account_type VARCHAR(20) NOT NULL DEFAULT 'SAVINGS',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create transactions table
CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL,
    client_id BIGINT NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    balance_after DECIMAL(19,2) NOT NULL,
    reference_number VARCHAR(50) UNIQUE,
    description TEXT,
    from_account VARCHAR(20),
    to_account VARCHAR(20),
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    failure_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX idx_account_client_id ON accounts(client_id);
CREATE INDEX idx_account_number ON accounts(account_number);
CREATE INDEX idx_account_status ON accounts(status);

CREATE INDEX idx_transaction_account_id ON transactions(account_id);
CREATE INDEX idx_transaction_client_id ON transactions(client_id);
CREATE INDEX idx_transaction_type ON transactions(transaction_type);
CREATE INDEX idx_transaction_created_at ON transactions(created_at);
CREATE INDEX idx_transaction_reference ON transactions(reference_number);

-- Create a trigger to automatically update the updated_at column for accounts
CREATE OR REPLACE FUNCTION update_accounts_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_accounts_updated_at 
    BEFORE UPDATE ON accounts 
    FOR EACH ROW 
    EXECUTE FUNCTION update_accounts_updated_at_column();

-- Create a function to generate unique account numbers
CREATE OR REPLACE FUNCTION generate_account_number()
RETURNS VARCHAR(20) AS $$
DECLARE
    new_number VARCHAR(20);
    counter INTEGER := 1;
BEGIN
    LOOP
        new_number := 'ACC' || LPAD(counter::TEXT, 8, '0');
        
        -- Check if this account number already exists
        IF NOT EXISTS (SELECT 1 FROM accounts WHERE account_number = new_number) THEN
            RETURN new_number;
        END IF;
        
        counter := counter + 1;
    END LOOP;
END;
$$ LANGUAGE plpgsql;

-- Insert sample account for testing
INSERT INTO accounts (client_id, account_number, balance, account_type) 
VALUES (1, generate_account_number(), 1000.00, 'SAVINGS');
