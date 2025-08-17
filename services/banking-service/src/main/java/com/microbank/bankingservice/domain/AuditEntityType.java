package com.microbank.bankingservice.domain;

public enum AuditEntityType {
    CLIENT,
    ACCOUNT,
    TRANSACTION,
    USER_SESSION,
    SECURITY_EVENT,
    SYSTEM_EVENT,
    ADMIN_ACTION,
    CONFIGURATION_CHANGE
}
