package com.microbank.bankingservice.service;

import com.microbank.bankingservice.domain.AuditAction;
import com.microbank.bankingservice.domain.AuditEntityType;
import com.microbank.bankingservice.domain.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditService {
    
    /**
     * Log an audit event
     */
    AuditLog logEvent(Long userId, String userEmail, String userRole, 
                      AuditEntityType entityType, Long entityId, 
                      AuditAction action, String actionDetails);
    
    /**
     * Log an audit event with change details
     */
    AuditLog logEventWithChanges(Long userId, String userEmail, String userRole,
                                AuditEntityType entityType, Long entityId,
                                AuditAction action, String actionDetails,
                                String oldValues, String newValues);
    
    /**
     * Log a security event
     */
    AuditLog logSecurityEvent(String userEmail, String ipAddress, 
                             AuditAction action, String details);
    
    /**
     * Log a system event
     */
    AuditLog logSystemEvent(AuditAction action, String details);
    
    /**
     * Get audit logs for a specific entity
     */
    List<AuditLog> getAuditLogsForEntity(AuditEntityType entityType, Long entityId);
    
    /**
     * Get audit logs for a specific user
     */
    List<AuditLog> getAuditLogsForUser(Long userId);
    
    /**
     * Get audit logs for a specific client
     */
    List<AuditLog> getAuditLogsForClient(Long clientId);
    
    /**
     * Get audit logs by action type
     */
    List<AuditLog> getAuditLogsByAction(AuditAction action);
    
    /**
     * Get audit logs within a date range
     */
    List<AuditLog> getAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Get all audit logs with pagination
     */
    Page<AuditLog> getAllAuditLogs(Pageable pageable);
    
    /**
     * Search audit logs with filters
     */
    Page<AuditLog> searchAuditLogs(String searchTerm, AuditEntityType entityType, 
                                   AuditAction action, LocalDateTime startDate, 
                                   LocalDateTime endDate, Pageable pageable);
    
    /**
     * Generate audit report for compliance
     */
    String generateComplianceReport(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Export audit logs to CSV
     */
    byte[] exportAuditLogsToCsv(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Clean up old audit logs (retention policy) - default 365 days
     */
    void cleanupOldAuditLogs();
    
    /**
     * Clean up old audit logs with custom retention period
     */
    void cleanupOldAuditLogs(int retentionDays);
}
