package com.microbank.bankingservice.exception;

/**
 * Custom exception for client access and permission issues
 */
public class ClientAccessException extends RuntimeException {
    
    private final String errorCode;
    private final String userMessage;
    
    public ClientAccessException(String message, String errorCode, String userMessage) {
        super(message);
        this.errorCode = errorCode;
        this.userMessage = userMessage;
    }
    
    public ClientAccessException(String message, String errorCode) {
        this(message, errorCode, message);
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public String getUserMessage() {
        return userMessage;
    }
    
    // Predefined exceptions for common scenarios
    public static ClientAccessException clientBlacklisted(Long clientId) {
        return new ClientAccessException(
            "Client " + clientId + " is blacklisted and cannot access this service",
            "CLIENT_BLACKLISTED",
            "Your account has been temporarily suspended. Please contact customer support for assistance."
        );
    }
    
    public static ClientAccessException clientInactive(Long clientId) {
        return new ClientAccessException(
            "Client " + clientId + " is inactive",
            "CLIENT_INACTIVE",
            "Your account is currently inactive. Please contact customer support to reactivate your account."
        );
    }
    
    public static ClientAccessException clientCannotTransact(Long clientId) {
        return new ClientAccessException(
            "Client " + clientId + " is not authorized to perform transactions",
            "CLIENT_TRANSACTION_BLOCKED",
            "You are not authorized to perform this transaction. Please contact customer support for assistance."
        );
    }
    
    public static ClientAccessException clientNotFound(Long clientId) {
        return new ClientAccessException(
            "Client " + clientId + " not found",
            "CLIENT_NOT_FOUND",
            "Client account not found. Please contact customer support for assistance."
        );
    }
    
    public static ClientAccessException unauthorizedAccess(Long clientId) {
        return new ClientAccessException(
            "Unauthorized access to client " + clientId,
            "UNAUTHORIZED_ACCESS",
            "You are not authorized to access this service. Please contact customer support for assistance."
        );
    }
    
    public static ClientAccessException serviceUnavailable(Long clientId) {
        return new ClientAccessException(
            "Service temporarily unavailable for client " + clientId,
            "SERVICE_UNAVAILABLE",
            "Service is temporarily unavailable. Please try again later or contact customer support."
        );
    }
}
