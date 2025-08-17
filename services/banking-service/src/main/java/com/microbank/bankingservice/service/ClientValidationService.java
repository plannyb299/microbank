package com.microbank.bankingservice.service;

/**
 * Service for validating client status and permissions
 */
public interface ClientValidationService {
    
    /**
     * Check if a client can perform transactions
     * @param clientId The client ID to validate
     * @return true if client can transact, false otherwise
     * @throws RuntimeException if client is blacklisted or inactive
     */
    void validateClientCanTransact(Long clientId);
    
    /**
     * Check if a client is blacklisted
     * @param clientId The client ID to check
     * @return true if client is blacklisted, false otherwise
     */
    boolean isClientBlacklisted(Long clientId);
    
    /**
     * Check if a client is active
     * @param clientId The client ID to check
     * @return true if client is active, false otherwise
     */
    boolean isClientActive(Long clientId);
}
