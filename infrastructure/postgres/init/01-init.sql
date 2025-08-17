-- Create the microbank user with password (if it doesn't exist)
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'microbank') THEN
        CREATE USER microbank WITH PASSWORD 'microbank123';
    END IF;
END
$$;

-- Grant all privileges on the database to the microbank user
GRANT ALL PRIVILEGES ON DATABASE microbank TO microbank;

-- Grant all privileges on all tables in the public schema
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO microbank;

-- Grant all privileges on all sequences in the public schema
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO microbank;

-- Grant all privileges on the public schema
GRANT ALL PRIVILEGES ON SCHEMA public TO microbank;

-- Set the default privileges for future tables and sequences
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO microbank;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO microbank;
