# 🔍 Microbank Audit System Documentation

## Overview

The Microbank platform now includes a comprehensive auditing system that provides complete visibility into all system activities, user actions, and data changes. This system ensures compliance with regulatory requirements and provides essential security monitoring capabilities.

## 🏗️ Architecture

### Core Components

1. **AuditLog Entity** - Central audit record storage
2. **AuditService** - Business logic for audit operations
3. **AuditAspect** - AOP-based automatic auditing
4. **AuditController** - REST API for audit management
5. **AuditDashboard** - React frontend for audit visualization

### Database Schema

```sql
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
    ip_address VARCHAR(45),
    user_agent TEXT,
    session_id VARCHAR(255),
    request_id VARCHAR(255),
    status VARCHAR(20) DEFAULT 'SUCCESS',
    failure_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

## 🔐 What Gets Audited

### Automatic Auditing (AOP)

The system automatically audits:

- **All Service Methods** - Critical operations are automatically logged
- **Security Events** - Login/logout, access attempts, failures
- **Data Changes** - Create, update, delete operations
- **System Events** - Startup, shutdown, maintenance

### Manual Auditing

Developers can manually audit specific operations using:

```java
@Auditable("Custom audit message")
public void customMethod() {
    // Method implementation
}
```

### Audit Entity Types

- `CLIENT` - Client-related operations
- `ACCOUNT` - Account management
- `TRANSACTION` - Financial transactions
- `USER_SESSION` - User session management
- `SECURITY_EVENT` - Security-related events
- `SYSTEM_EVENT` - System operations
- `ADMIN_ACTION` - Administrative actions
- `CONFIGURATION_CHANGE` - System configuration changes

### Audit Actions

- **CRUD Operations**: CREATE, READ, UPDATE, DELETE
- **Authentication**: LOGIN, LOGOUT, LOGIN_FAILED, PASSWORD_CHANGE
- **Banking Operations**: DEPOSIT, WITHDRAWAL, TRANSFER, ACCOUNT_OPEN
- **Security Events**: SUSPICIOUS_ACTIVITY, ACCESS_DENIED, BLACKLIST_ADD
- **System Events**: SYSTEM_STARTUP, MAINTENANCE_MODE, BACKUP_CREATED

## 📊 Audit Dashboard Features

### Overview Tab
- **Statistics Cards**: Total events, security events, failed events, success rate
- **Recent Activity**: Latest audit entries with visual indicators
- **Performance Metrics**: System health and audit system performance

### Audit Logs Tab
- **Comprehensive Table**: All audit events with detailed information
- **Advanced Filtering**: By entity type, action, status, date range
- **Search Functionality**: Full-text search across audit details
- **Pagination**: Efficient browsing of large audit datasets

### Security Events Tab
- **Security Monitoring**: Dedicated view for security-related events
- **Threat Detection**: Identification of suspicious activities
- **Access Control**: Monitoring of authentication and authorization events

### Reports Tab
- **Compliance Reports**: Regulatory compliance documentation
- **Security Reports**: Security event analysis and trends
- **Data Export**: CSV export for external analysis tools

## 🚀 API Endpoints

### Audit Management

```
GET    /api/v1/audit/logs                    - Get all audit logs (paginated)
GET    /api/v1/audit/logs/search             - Search audit logs with filters
GET    /api/v1/audit/logs/entity/{type}/{id} - Get logs for specific entity
GET    /api/v1/audit/logs/user/{userId}      - Get logs for specific user
GET    /api/v1/audit/logs/client/{clientId}  - Get logs for specific client
GET    /api/v1/audit/logs/action/{action}    - Get logs by action type
GET    /api/v1/audit/logs/date-range         - Get logs within date range
GET    /api/v1/audit/logs/recent             - Get recent audit logs
GET    /api/v1/audit/logs/security           - Get security-related logs
GET    /api/v1/audit/logs/failed             - Get failed audit events
```

### Reporting & Export

```
GET    /api/v1/audit/report/compliance       - Generate compliance report
GET    /api/v1/audit/export/csv              - Export audit logs to CSV
GET    /api/v1/audit/statistics/summary      - Get audit statistics
DELETE /api/v1/audit/logs/cleanup            - Clean up old audit logs
```

## 🔧 Configuration

### Application Properties

```yaml
# Audit System Configuration
audit:
  retention:
    days: 365  # How long to keep audit logs
  cleanup:
    enabled: true
    schedule: "0 0 2 * * ?"  # Daily at 2 AM
  security:
    capture-ip: true
    capture-user-agent: true
    capture-session: true
```

### AOP Configuration

```java
@Configuration
@EnableAspectJAutoProxy
@EnableScheduling
@EnableMethodSecurity(prePostEnabled = true)
public class AuditConfig {
    // Configuration beans
}
```

## 📈 Performance & Scalability

### Database Optimization

- **Indexes**: Comprehensive indexing for fast queries
- **Partitioning**: Support for table partitioning by date
- **Archiving**: Automatic cleanup of old audit logs
- **Views**: Pre-built views for common query patterns

### Caching Strategy

- **Recent Events**: In-memory cache for recent audit entries
- **Statistics**: Cached statistics with configurable TTL
- **User Context**: Cached user information for audit operations

### Batch Operations

- **Bulk Export**: Efficient CSV export for large datasets
- **Batch Cleanup**: Scheduled cleanup of old audit data
- **Async Logging**: Non-blocking audit log creation

## 🛡️ Security Features

### Access Control

- **Admin-Only Access**: Audit dashboard restricted to administrators
- **Role-Based Permissions**: Different access levels for different roles
- **Audit Trail**: All access to audit data is itself audited

### Data Protection

- **Sensitive Data Masking**: PII and sensitive information protection
- **Encrypted Storage**: Audit logs stored with encryption
- **Secure Transmission**: HTTPS-only access to audit APIs

### Compliance Features

- **Regulatory Compliance**: Meets banking and financial regulations
- **Data Retention**: Configurable retention policies
- **Audit Reports**: Standardized compliance reporting

## 🔍 Monitoring & Alerting

### Real-Time Monitoring

- **Live Dashboard**: Real-time audit event display
- **Alert System**: Notifications for critical security events
- **Performance Metrics**: Audit system performance monitoring

### Automated Alerts

- **Failed Operations**: Alerts for failed audit events
- **Security Threats**: Detection of suspicious activities
- **System Issues**: Monitoring of audit system health

## 📋 Implementation Examples

### Adding Audit to a Service Method

```java
@Service
public class AccountService {
    
    @Auditable("Account creation with initial balance")
    public AccountResponse createAccount(Long clientId, AccountType accountType) {
        // Method implementation
        // Audit is automatically captured
    }
    
    @Auditable("Account balance update")
    public void updateBalance(Long accountId, BigDecimal newBalance) {
        // Method implementation
        // Audit is automatically captured
    }
}
```

### Manual Audit Logging

```java
@Service
public class SecurityService {
    
    @Autowired
    private AuditService auditService;
    
    public void logSuspiciousActivity(String userEmail, String ipAddress, String details) {
        auditService.logSecurityEvent(userEmail, ipAddress, 
            AuditAction.SUSPICIOUS_ACTIVITY, details);
    }
}
```

### Custom Audit Annotations

```java
@Auditable(
    value = "Critical financial operation",
    captureParameters = true,
    captureReturnValue = false,
    entityType = "TRANSACTION",
    action = "TRANSFER"
)
public TransactionResponse processTransfer(TransferRequest request) {
    // Method implementation
}
```

## 🚀 Getting Started

### 1. Enable Auditing

The audit system is automatically enabled when you start the application. No additional configuration is required.

### 2. Access Audit Dashboard

Navigate to `/audit` in the application (admin users only).

### 3. View Audit Logs

Use the dashboard to explore audit data, generate reports, and monitor system activity.

### 4. Customize Auditing

Add `@Auditable` annotations to methods that need custom audit behavior.

## 📚 Best Practices

### Audit Design

1. **Comprehensive Coverage**: Audit all critical operations
2. **Meaningful Messages**: Use descriptive audit messages
3. **Performance Impact**: Minimize audit overhead on business operations
4. **Data Retention**: Implement appropriate retention policies

### Security Considerations

1. **Access Control**: Restrict audit access to authorized personnel
2. **Data Protection**: Protect sensitive audit information
3. **Tamper Prevention**: Ensure audit logs cannot be modified
4. **Backup Strategy**: Regular backup of audit data

### Performance Optimization

1. **Efficient Queries**: Use appropriate database indexes
2. **Batch Operations**: Group audit operations when possible
3. **Async Processing**: Use asynchronous logging for non-critical events
4. **Regular Cleanup**: Implement automated cleanup of old data

## 🔮 Future Enhancements

### Planned Features

- **Machine Learning**: AI-powered anomaly detection
- **Advanced Analytics**: Predictive analytics for security threats
- **Integration**: Third-party SIEM system integration
- **Real-Time Streaming**: Kafka-based real-time audit streaming
- **Advanced Reporting**: Custom report builder and scheduling

### Scalability Improvements

- **Horizontal Scaling**: Support for multiple audit service instances
- **Database Sharding**: Distributed audit data storage
- **Microservice Architecture**: Dedicated audit microservice
- **Event Sourcing**: Event-sourced audit architecture

## 📞 Support

For questions about the audit system or assistance with implementation, please refer to:

- **Documentation**: This document and inline code comments
- **API Reference**: Swagger documentation at `/swagger-ui.html`
- **Code Examples**: Sample implementations in the codebase
- **Configuration**: Application properties and configuration classes

---

**Note**: The audit system is designed to be production-ready and compliant with financial industry standards. Regular monitoring and maintenance are recommended to ensure optimal performance and security.
