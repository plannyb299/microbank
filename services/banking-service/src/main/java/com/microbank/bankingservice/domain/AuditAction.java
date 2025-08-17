package com.microbank.bankingservice.domain;

public enum AuditAction {
    // CRUD Operations
    CREATE,
    READ,
    UPDATE,
    DELETE,
    
    // Authentication & Authorization
    LOGIN,
    LOGOUT,
    LOGIN_FAILED,
    PASSWORD_CHANGE,
    ROLE_CHANGE,
    ACCESS_DENIED,
    
    // Banking Operations
    DEPOSIT,
    WITHDRAWAL,
    TRANSFER,
    ACCOUNT_OPEN,
    ACCOUNT_CLOSE,
    ACCOUNT_SUSPEND,
    ACCOUNT_ACTIVATE,
    
    // Security Events
    SUSPICIOUS_ACTIVITY,
    MULTIPLE_FAILED_LOGINS,
    UNUSUAL_TRANSACTION,
    BLACKLIST_ADD,
    BLACKLIST_REMOVE,
    
    // System Events
    SYSTEM_STARTUP,
    SYSTEM_SHUTDOWN,
    MAINTENANCE_MODE,
    BACKUP_CREATED,
    CONFIGURATION_CHANGED,
    
    // Compliance & Reporting
    AUDIT_REPORT_GENERATED,
    COMPLIANCE_CHECK,
    REGULATORY_REPORT
}
