-- Create audit_logs table for comprehensive auditing
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    user_email VARCHAR(255),
    user_role VARCHAR(50),
    client_id BIGINT,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT,
    action VARCHAR(50) NOT NULL,
    action_details TEXT,
    old_values TEXT,
    new_values TEXT,
    ip_address VARCHAR(45), -- IPv6 compatible
    user_agent TEXT,
    session_id VARCHAR(255),
    request_id VARCHAR(255),
    status VARCHAR(20) DEFAULT 'SUCCESS',
    failure_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX idx_audit_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_entity_type ON audit_logs(entity_type);
CREATE INDEX idx_audit_action ON audit_logs(action);
CREATE INDEX idx_audit_created_at ON audit_logs(created_at);
CREATE INDEX idx_audit_client_id ON audit_logs(client_id);
CREATE INDEX idx_audit_user_email ON audit_logs(user_email);
CREATE INDEX idx_audit_status ON audit_logs(status);
CREATE INDEX idx_audit_ip_address ON audit_logs(ip_address);
CREATE INDEX idx_audit_session_id ON audit_logs(session_id);
CREATE INDEX idx_audit_request_id ON audit_logs(request_id);

-- Create composite indexes for common query patterns
CREATE INDEX idx_audit_entity_type_id ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_user_date ON audit_logs(user_id, created_at);
CREATE INDEX idx_audit_client_date ON audit_logs(client_id, created_at);
CREATE INDEX idx_audit_action_date ON audit_logs(action, created_at);
CREATE INDEX idx_audit_type_action_date ON audit_logs(entity_type, action, created_at);

-- Create a trigger to automatically update the created_at column
CREATE OR REPLACE FUNCTION update_audit_logs_created_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.created_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_audit_logs_created_at 
    BEFORE INSERT ON audit_logs 
    FOR EACH ROW 
    EXECUTE FUNCTION update_audit_logs_created_at_column();

-- Create a function to get audit trail for an entity
CREATE OR REPLACE FUNCTION get_entity_audit_trail(
    p_entity_type VARCHAR(50),
    p_entity_id BIGINT,
    p_limit INTEGER DEFAULT 100
)
RETURNS TABLE (
    action VARCHAR(50),
    action_details TEXT,
    user_email VARCHAR(255),
    user_role VARCHAR(50),
    ip_address VARCHAR(45),
    created_at TIMESTAMP
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        al.action,
        al.action_details,
        al.user_email,
        al.user_role,
        al.ip_address,
        al.created_at
    FROM audit_logs al
    WHERE al.entity_type = p_entity_type 
      AND al.entity_id = p_entity_id
    ORDER BY al.created_at DESC
    LIMIT p_limit;
END;
$$ LANGUAGE plpgsql;

-- Create a function to get user activity summary
CREATE OR REPLACE FUNCTION get_user_activity_summary(
    p_user_id BIGINT,
    p_days INTEGER DEFAULT 30
)
RETURNS TABLE (
    action VARCHAR(50),
    entity_type VARCHAR(50),
    count BIGINT,
    last_activity TIMESTAMP
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        al.action,
        al.entity_type,
        COUNT(*) as count,
        MAX(al.created_at) as last_activity
    FROM audit_logs al
    WHERE al.user_id = p_user_id 
      AND al.created_at >= CURRENT_TIMESTAMP - INTERVAL '1 day' * p_days
    GROUP BY al.action, al.entity_type
    ORDER BY count DESC;
END;
$$ LANGUAGE plpgsql;

-- Create a function to get security events summary
CREATE OR REPLACE FUNCTION get_security_events_summary(
    p_days INTEGER DEFAULT 7
)
RETURNS TABLE (
    event_type VARCHAR(50),
    count BIGINT,
    last_occurrence TIMESTAMP,
    affected_users BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        al.action as event_type,
        COUNT(*) as count,
        MAX(al.created_at) as last_occurrence,
        COUNT(DISTINCT al.user_email) as affected_users
    FROM audit_logs al
    WHERE al.entity_type = 'SECURITY_EVENT'
      AND al.created_at >= CURRENT_TIMESTAMP - INTERVAL '1 day' * p_days
    GROUP BY al.action
    ORDER BY count DESC;
END;
$$ LANGUAGE plpgsql;

-- Insert initial system audit log
INSERT INTO audit_logs (
    user_email, 
    user_role, 
    entity_type, 
    action, 
    action_details, 
    status, 
    created_at
) VALUES (
    'SYSTEM', 
    'SYSTEM', 
    'SYSTEM_EVENT', 
    'SYSTEM_STARTUP', 
    'Audit system initialized and audit_logs table created', 
    'SUCCESS', 
    CURRENT_TIMESTAMP
);

-- Create a view for recent audit activity
CREATE VIEW recent_audit_activity AS
SELECT 
    al.created_at,
    al.user_email,
    al.user_role,
    al.entity_type,
    al.action,
    al.action_details,
    al.ip_address,
    al.status
FROM audit_logs al
WHERE al.created_at >= CURRENT_TIMESTAMP - INTERVAL '24 hours'
ORDER BY al.created_at DESC;

-- Create a view for failed audit events
CREATE VIEW failed_audit_events AS
SELECT 
    al.created_at,
    al.user_email,
    al.user_role,
    al.entity_type,
    al.action,
    al.action_details,
    al.failure_reason,
    al.ip_address
FROM audit_logs al
WHERE al.status = 'FAILED'
ORDER BY al.created_at DESC;

-- Note: Role permissions removed for local development
-- In production, you would create the microbank role and grant appropriate permissions
