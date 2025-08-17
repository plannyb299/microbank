package com.microbank.bankingservice.service.impl;

import com.microbank.bankingservice.domain.AuditAction;
import com.microbank.bankingservice.domain.AuditEntityType;
import com.microbank.bankingservice.domain.AuditLog;
import com.microbank.bankingservice.repository.AuditLogRepository;
import com.microbank.bankingservice.service.AuditService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AuditServiceImpl implements AuditService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuditServiceImpl.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    private final AuditLogRepository auditLogRepository;
    
    @Autowired
    public AuditServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }
    
    @Override
    public AuditLog logEvent(Long userId, String userEmail, String userRole, 
                           AuditEntityType entityType, Long entityId, 
                           AuditAction action, String actionDetails) {
        try {
            AuditLog auditLog = new AuditLog(userId, userEmail, userRole, entityType, entityId, action, actionDetails);
            
            // Set request context if available
            setRequestContext(auditLog);
            
            // Set success status
            auditLog.setSuccess();
            
            AuditLog savedLog = auditLogRepository.save(auditLog);
            logger.debug("Audit event logged: {} - {} - {}", action, entityType, entityId);
            
            return savedLog;
        } catch (Exception e) {
            logger.error("Failed to log audit event: {}", e.getMessage(), e);
            return null;
        }
    }
    
    @Override
    public AuditLog logEventWithChanges(Long userId, String userEmail, String userRole,
                                      AuditEntityType entityType, Long entityId,
                                      AuditAction action, String actionDetails,
                                      String oldValues, String newValues) {
        try {
            AuditLog auditLog = new AuditLog(userId, userEmail, userRole, entityType, entityId, action, actionDetails);
            
            // Set change details
            auditLog.setChangeDetails(oldValues, newValues);
            
            // Set request context if available
            setRequestContext(auditLog);
            
            // Set success status
            auditLog.setSuccess();
            
            AuditLog savedLog = auditLogRepository.save(auditLog);
            logger.debug("Audit event with changes logged: {} - {} - {}", action, entityType, entityId);
            
            return savedLog;
        } catch (Exception e) {
            logger.error("Failed to log audit event with changes: {}", e.getMessage(), e);
            return null;
        }
    }
    
    @Override
    public AuditLog logSecurityEvent(String userEmail, String ipAddress, 
                                   AuditAction action, String details) {
        try {
            AuditLog auditLog = new AuditLog(null, userEmail, "SYSTEM", 
                                           AuditEntityType.SECURITY_EVENT, null, action, details);
            
            // Set IP address
            auditLog.setIpAddress(ipAddress);
            
            // Set request context if available
            setRequestContext(auditLog);
            
            // Set success status
            auditLog.setSuccess();
            
            AuditLog savedLog = auditLogRepository.save(auditLog);
            logger.warn("Security event logged: {} - {} - {}", action, userEmail, ipAddress);
            
            return savedLog;
        } catch (Exception e) {
            logger.error("Failed to log security event: {}", e.getMessage(), e);
            return null;
        }
    }
    
    @Override
    public AuditLog logSystemEvent(AuditAction action, String details) {
        try {
            AuditLog auditLog = new AuditLog(null, "SYSTEM", "SYSTEM", 
                                           AuditEntityType.SYSTEM_EVENT, null, action, details);
            
            // Set request context if available
            setRequestContext(auditLog);
            
            // Set success status
            auditLog.setSuccess();
            
            AuditLog savedLog = auditLogRepository.save(auditLog);
            logger.info("System event logged: {} - {}", action, details);
            
            return savedLog;
        } catch (Exception e) {
            logger.error("Failed to log system event: {}", e.getMessage(), e);
            return null;
        }
    }
    
    @Override
    public List<AuditLog> getAuditLogsForEntity(AuditEntityType entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId);
    }
    
    @Override
    public List<AuditLog> getAuditLogsForUser(Long userId) {
        return auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    @Override
    public List<AuditLog> getAuditLogsForClient(Long clientId) {
        return auditLogRepository.findByClientIdOrderByCreatedAtDesc(clientId);
    }
    
    @Override
    public List<AuditLog> getAuditLogsByAction(AuditAction action) {
        return auditLogRepository.findByActionOrderByCreatedAtDesc(action);
    }
    
    @Override
    public List<AuditLog> getAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(startDate, endDate);
    }
    
    @Override
    public Page<AuditLog> getAllAuditLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }
    
    @Override
    public Page<AuditLog> searchAuditLogs(String searchTerm, AuditEntityType entityType, 
                                         AuditAction action, LocalDateTime startDate, 
                                         LocalDateTime endDate, Pageable pageable) {
        return auditLogRepository.searchAuditLogs(searchTerm, entityType, action, startDate, endDate, pageable);
    }
    
    @Override
    public String generateComplianceReport(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            List<AuditLog> logs = getAuditLogsByDateRange(startDate, endDate);
            
            StringBuilder report = new StringBuilder();
            report.append("COMPLIANCE AUDIT REPORT\n");
            report.append("======================\n");
            report.append("Period: ").append(startDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                  .append(" to ").append(endDate.format(DateTimeFormatter.ISO_LOCAL_DATE)).append("\n");
            report.append("Generated: ").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("\n\n");
            
            // Summary statistics
            long totalEvents = logs.size();
            long securityEvents = logs.stream().filter(l -> l.getEntityType() == AuditEntityType.SECURITY_EVENT).count();
            long failedEvents = logs.stream().filter(l -> "FAILED".equals(l.getStatus())).count();
            
            report.append("SUMMARY STATISTICS\n");
            report.append("------------------\n");
            report.append("Total Events: ").append(totalEvents).append("\n");
            report.append("Security Events: ").append(securityEvents).append("\n");
            report.append("Failed Events: ").append(failedEvents).append("\n");
            report.append("Success Rate: ").append(String.format("%.2f%%", (double)(totalEvents - failedEvents) / totalEvents * 100)).append("\n\n");
            
            // Action breakdown
            report.append("ACTION BREAKDOWN\n");
            report.append("----------------\n");
            logs.stream()
                .collect(java.util.stream.Collectors.groupingBy(AuditLog::getAction, java.util.stream.Collectors.counting()))
                .forEach((action, count) -> report.append(action).append(": ").append(count).append("\n"));
            
            report.append("\n");
            
            // Entity type breakdown
            report.append("ENTITY TYPE BREAKDOWN\n");
            report.append("--------------------\n");
            logs.stream()
                .collect(java.util.stream.Collectors.groupingBy(AuditLog::getEntityType, java.util.stream.Collectors.counting()))
                .forEach((entityType, count) -> report.append(entityType).append(": ").append(count).append("\n"));
            
            report.append("\n");
            
            // Recent security events
            report.append("RECENT SECURITY EVENTS\n");
            report.append("----------------------\n");
            logs.stream()
                .filter(l -> l.getEntityType() == AuditEntityType.SECURITY_EVENT)
                .limit(10)
                .forEach(log -> report.append(log.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .append(" - ").append(log.getAction()).append(" - ").append(log.getUserEmail())
                    .append(" - ").append(log.getActionDetails()).append("\n"));
            
            return report.toString();
        } catch (Exception e) {
            logger.error("Failed to generate compliance report: {}", e.getMessage(), e);
            return "Error generating compliance report: " + e.getMessage();
        }
    }
    
    @Override
    public byte[] exportAuditLogsToCsv(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            List<AuditLog> logs = getAuditLogsByDateRange(startDate, endDate);
            
            StringBuilder csv = new StringBuilder();
            csv.append("ID,User ID,User Email,User Role,Client ID,Entity Type,Entity ID,Action,Action Details,Old Values,New Values,IP Address,User Agent,Session ID,Request ID,Status,Failure Reason,Created At\n");
            
            for (AuditLog log : logs) {
                csv.append(String.format("%d,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                    log.getId(),
                    log.getUserId() != null ? log.getUserId().toString() : "",
                    escapeCsvField(log.getUserEmail()),
                    escapeCsvField(log.getUserRole()),
                    log.getClientId() != null ? log.getClientId().toString() : "",
                    log.getEntityType(),
                    log.getEntityId() != null ? log.getEntityId().toString() : "",
                    log.getAction(),
                    escapeCsvField(log.getActionDetails()),
                    escapeCsvField(log.getOldValues()),
                    escapeCsvField(log.getNewValues()),
                    escapeCsvField(log.getIpAddress()),
                    escapeCsvField(log.getUserAgent()),
                    escapeCsvField(log.getSessionId()),
                    escapeCsvField(log.getRequestId()),
                    log.getStatus(),
                    escapeCsvField(log.getFailureReason()),
                    log.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                ));
            }
            
            return csv.toString().getBytes("UTF-8");
        } catch (Exception e) {
            logger.error("Failed to export audit logs to CSV: {}", e.getMessage(), e);
            return new byte[0];
        }
    }
    
    @Override
    @Scheduled(cron = "0 0 2 * * ?") // Run at 2 AM daily
    public void cleanupOldAuditLogs() {
        try {
            // Default retention period: 365 days
            int retentionDays = 365;
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
            List<AuditLog> oldLogs = auditLogRepository.findByCreatedAtBefore(cutoffDate);
            
            if (!oldLogs.isEmpty()) {
                auditLogRepository.deleteByCreatedAtBefore(cutoffDate);
                logger.info("Cleaned up {} old audit logs older than {} days", oldLogs.size(), retentionDays);
                
                // Log the cleanup operation
                logSystemEvent(AuditAction.SYSTEM_STARTUP, 
                    String.format("Cleaned up %d old audit logs older than %d days", oldLogs.size(), retentionDays));
            }
        } catch (Exception e) {
            logger.error("Failed to cleanup old audit logs: {}", e.getMessage(), e);
        }
    }
    
    // Manual cleanup method with configurable retention period
    public void cleanupOldAuditLogs(int retentionDays) {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
            List<AuditLog> oldLogs = auditLogRepository.findByCreatedAtBefore(cutoffDate);
            
            if (!oldLogs.isEmpty()) {
                auditLogRepository.deleteByCreatedAtBefore(cutoffDate);
                logger.info("Cleaned up {} old audit logs older than {} days", oldLogs.size(), retentionDays);
                
                // Log the cleanup operation
                logSystemEvent(AuditAction.SYSTEM_STARTUP, 
                    String.format("Cleaned up %d old audit logs older than %d days", oldLogs.size(), retentionDays));
            }
        } catch (Exception e) {
            logger.error("Failed to cleanup old audit logs: {}", e.getMessage(), e);
        }
    }
    
    // Helper methods
    private void setRequestContext(AuditLog auditLog) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                
                String ipAddress = getClientIpAddress(request);
                String userAgent = request.getHeader("User-Agent");
                String sessionId = request.getSession() != null ? request.getSession().getId() : null;
                String requestId = UUID.randomUUID().toString();
                
                auditLog.setRequestContext(ipAddress, userAgent, sessionId, requestId);
            }
        } catch (Exception e) {
            logger.debug("Could not set request context: {}", e.getMessage());
        }
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    private String escapeCsvField(String field) {
        if (field == null) return "";
        return field.replace("\"", "\"\"").replace("\n", " ").replace("\r", " ");
    }
    
    // Utility methods for common audit scenarios
    public void logAccountCreation(Long userId, String userEmail, String userRole, Long accountId, Long clientId) {
        logEvent(userId, userEmail, userRole, AuditEntityType.ACCOUNT, accountId, 
                AuditAction.ACCOUNT_OPEN, "Account created for client " + clientId);
    }
    
    public void logTransaction(Long userId, String userEmail, String userRole, Long transactionId, 
                             Long accountId, AuditAction action, String details) {
        logEvent(userId, userEmail, userRole, AuditEntityType.TRANSACTION, transactionId, 
                action, details);
    }
    
    public void logLoginSuccess(String userEmail, String ipAddress) {
        logSecurityEvent(userEmail, ipAddress, AuditAction.LOGIN, "Successful login");
    }
    
    public void logLoginFailure(String userEmail, String ipAddress, String reason) {
        logSecurityEvent(userEmail, ipAddress, AuditAction.LOGIN_FAILED, "Login failed: " + reason);
    }
    
    public void logSuspiciousActivity(String userEmail, String ipAddress, String details) {
        logSecurityEvent(userEmail, ipAddress, AuditAction.SUSPICIOUS_ACTIVITY, details);
    }
}
