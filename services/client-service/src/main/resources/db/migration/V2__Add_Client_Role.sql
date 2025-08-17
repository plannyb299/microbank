-- Add role column to clients table
ALTER TABLE clients ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'CLIENT';

-- Create index on role column for better performance
CREATE INDEX idx_client_role ON clients(role);

-- Update existing clients to have CLIENT role (they already do by default, but explicit)
UPDATE clients SET role = 'CLIENT' WHERE role IS NULL;

-- Update the existing admin user to have ADMIN role
UPDATE clients SET role = 'ADMIN' WHERE email = 'admin@microbank.com';
