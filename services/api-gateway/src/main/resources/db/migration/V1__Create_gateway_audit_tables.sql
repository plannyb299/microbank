-- Create gateway_audit_logs table for API Gateway audit logging
CREATE TABLE gateway_audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    user_email VARCHAR(255),
    user_role VARCHAR(50),
    target_service VARCHAR(100) NOT NULL,
    endpoint VARCHAR(500) NOT NULL,
    http_method VARCHAR(10) NOT NULL,
    request_headers TEXT,
    request_body TEXT,
    response_status INTEGER,
    response_body TEXT,
    ip_address VARCHAR(45), -- IPv6 compatible
    user_agent TEXT,
    session_id VARCHAR(255),
    request_id VARCHAR(255) NOT NULL,
    execution_time_ms BIGINT,
    error_message TEXT,
    rate_limited BOOLEAN DEFAULT FALSE,
    blocked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX idx_gateway_user_id ON gateway_audit_logs(user_id);
CREATE INDEX idx_gateway_service ON gateway_audit_logs(target_service);
CREATE INDEX idx_gateway_endpoint ON gateway_audit_logs(endpoint);
CREATE INDEX idx_gateway_method ON gateway_audit_logs(http_method);
CREATE INDEX idx_gateway_status ON gateway_audit_logs(response_status);
CREATE INDEX idx_gateway_created_at ON gateway_audit_logs(created_at);
CREATE INDEX idx_gateway_request_id ON gateway_audit_logs(request_id);
CREATE INDEX idx_gateway_ip_address ON gateway_audit_logs(ip_address);

-- Create a trigger to automatically update the created_at column
CREATE OR REPLACE FUNCTION update_gateway_audit_logs_created_at_column() RETURNS TRIGGER AS $$
BEGIN
    NEW.created_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_gateway_audit_logs_created_at 
    BEFORE INSERT ON gateway_audit_logs 
    FOR EACH ROW 
    EXECUTE FUNCTION update_gateway_audit_logs_created_at_column();

-- Create a function to get API usage statistics
CREATE OR REPLACE FUNCTION get_api_usage_stats(
    p_start_date TIMESTAMP DEFAULT CURRENT_DATE - INTERVAL '30 days',
    p_end_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) RETURNS TABLE (
    service_name VARCHAR(100),
    total_requests BIGINT,
    successful_requests BIGINT,
    failed_requests BIGINT,
    avg_response_time NUMERIC,
    rate_limited_requests BIGINT,
    blocked_requests BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        gal.target_service as service_name,
        COUNT(*) as total_requests,
        COUNT(CASE WHEN gal.response_status >= 200 AND gal.response_status < 300 THEN 1 END) as successful_requests,
        COUNT(CASE WHEN gal.response_status >= 400 THEN 1 END) as failed_requests,
        AVG(gal.execution_time_ms) as avg_response_time,
        COUNT(CASE WHEN gal.rate_limited = TRUE THEN 1 END) as rate_limited_requests,
        COUNT(CASE WHEN gal.blocked = TRUE THEN 1 END) as blocked_requests
    FROM gateway_audit_logs gal
    WHERE gal.created_at BETWEEN p_start_date AND p_end_date
    GROUP BY gal.target_service
    ORDER BY total_requests DESC;
END;
$$ LANGUAGE plpgsql;

-- Create a function to get user activity summary
CREATE OR REPLACE FUNCTION get_user_activity_summary(
    p_user_id BIGINT,
    p_days INTEGER DEFAULT 30
) RETURNS TABLE (
    endpoint VARCHAR(500),
    http_method VARCHAR(10),
    total_requests BIGINT,
    last_accessed TIMESTAMP,
    avg_response_time NUMERIC
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        gal.endpoint,
        gal.http_method,
        COUNT(*) as total_requests,
        MAX(gal.created_at) as last_accessed,
        AVG(gal.execution_time_ms) as avg_response_time
    FROM gateway_audit_logs gal
    WHERE gal.user_id = p_user_id
    AND gal.created_at >= CURRENT_DATE - INTERVAL '1 day' * p_days
    GROUP BY gal.endpoint, gal.http_method
    ORDER BY total_requests DESC;
END;
$$ LANGUAGE plpgsql;

-- Create a view for security monitoring
CREATE OR REPLACE VIEW security_monitoring_view AS
SELECT 
    DATE(created_at) as date,
    target_service,
    COUNT(*) as total_requests,
    COUNT(CASE WHEN response_status >= 400 THEN 1 END) as failed_requests,
    COUNT(CASE WHEN rate_limited = TRUE THEN 1 END) as rate_limited,
    COUNT(CASE WHEN blocked = TRUE THEN 1 END) as blocked,
    COUNT(CASE WHEN ip_address IS NOT NULL THEN 1 END) as requests_with_ip,
    AVG(execution_time_ms) as avg_response_time
FROM gateway_audit_logs
WHERE created_at >= CURRENT_DATE - INTERVAL '7 days'
GROUP BY DATE(created_at), target_service
ORDER BY date DESC, total_requests DESC;

-- Create a view for endpoint performance
CREATE OR REPLACE VIEW endpoint_performance_view AS
SELECT 
    target_service,
    endpoint,
    http_method,
    COUNT(*) as total_requests,
    COUNT(CASE WHEN response_status >= 200 AND response_status < 300 THEN 1 END) as successful_requests,
    COUNT(CASE WHEN response_status >= 400 THEN 1 END) as failed_requests,
    AVG(execution_time_ms) as avg_response_time,
    MAX(execution_time_ms) as max_response_time,
    MIN(execution_time_ms) as min_response_time
FROM gateway_audit_logs
WHERE created_at >= CURRENT_DATE - INTERVAL '24 hours'
GROUP BY target_service, endpoint, http_method
ORDER BY total_requests DESC;

-- Insert initial system audit log
INSERT INTO gateway_audit_logs (
    target_service, 
    endpoint, 
    http_method, 
    request_id, 
    response_status, 
    created_at
) VALUES (
    'SYSTEM', 
    '/api/gateway/startup', 
    'GET', 
    'system-startup-' || EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)::TEXT, 
    200, 
    CURRENT_TIMESTAMP
);

-- Note: Role permissions removed for local development
-- In production, you would create the microbank role and grant appropriate permissions
