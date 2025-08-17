package com.microbank.bankingservice.controller;

import com.microbank.bankingservice.domain.AuditAction;
import com.microbank.bankingservice.domain.AuditEntityType;
import com.microbank.bankingservice.domain.AuditLog;
import com.microbank.bankingservice.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageImpl;

@RestController
@RequestMapping("/api/v1/audit")
@Tag(name = "Audit Management", description = "APIs for audit logging and compliance reporting")
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {

    private static final Logger logger = LoggerFactory.getLogger(AuditController.class);
    
    private final AuditService auditService;

    @Autowired
    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/logs")
    @Operation(summary = "Get all audit logs with pagination")
    public ResponseEntity<Page<AuditLog>> getAllAuditLogs(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> auditLogs = auditService.getAllAuditLogs(pageable);
        return ResponseEntity.ok(auditLogs);
    }

    @GetMapping("/logs/search")
    @Operation(summary = "Search audit logs with filters")
    public ResponseEntity<Page<AuditLog>> searchAuditLogs(
            @Parameter(description = "Search term for action details or user email")
            @RequestParam(required = false) String searchTerm,
            @Parameter(description = "Entity type filter")
            @RequestParam(required = false) AuditEntityType entityType,
            @Parameter(description = "Action type filter")
            @RequestParam(required = false) AuditAction action,
            @Parameter(description = "Start date (ISO format)")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date (ISO format)")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> auditLogs = auditService.searchAuditLogs(
                searchTerm, entityType, action, startDate, endDate, pageable);
        return ResponseEntity.ok(auditLogs);
    }

    @GetMapping("/logs/entity/{entityType}/{entityId}")
    @Operation(summary = "Get audit logs for a specific entity")
    public ResponseEntity<List<AuditLog>> getAuditLogsForEntity(
            @Parameter(description = "Entity type")
            @PathVariable AuditEntityType entityType,
            @Parameter(description = "Entity ID")
            @PathVariable Long entityId) {
        
        List<AuditLog> auditLogs = auditService.getAuditLogsForEntity(entityType, entityId);
        return ResponseEntity.ok(auditLogs);
    }

    @GetMapping("/logs/user/{userId}")
    @Operation(summary = "Get audit logs for a specific user")
    public ResponseEntity<List<AuditLog>> getAuditLogsForUser(
            @Parameter(description = "User ID")
            @PathVariable Long userId) {
        
        List<AuditLog> auditLogs = auditService.getAuditLogsForUser(userId);
        return ResponseEntity.ok(auditLogs);
    }

    @GetMapping("/logs/client/{clientId}")
    @Operation(summary = "Get audit logs for a specific client")
    public ResponseEntity<List<AuditLog>> getAuditLogsForClient(
            @Parameter(description = "Client ID")
            @PathVariable Long clientId) {
        
        List<AuditLog> auditLogs = auditService.getAuditLogsForClient(clientId);
        return ResponseEntity.ok(auditLogs);
    }

    @GetMapping("/logs/action/{action}")
    @Operation(summary = "Get audit logs by action type")
    public ResponseEntity<List<AuditLog>> getAuditLogsByAction(
            @Parameter(description = "Action type")
            @PathVariable AuditAction action) {
        
        List<AuditLog> auditLogs = auditService.getAuditLogsByAction(action);
        return ResponseEntity.ok(auditLogs);
    }

    @GetMapping("/logs/date-range")
    @Operation(summary = "Get audit logs within a date range")
    public ResponseEntity<List<AuditLog>> getAuditLogsByDateRange(
            @Parameter(description = "Start date (ISO format)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date (ISO format)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        List<AuditLog> auditLogs = auditService.getAuditLogsByDateRange(startDate, endDate);
        return ResponseEntity.ok(auditLogs);
    }

    @GetMapping("/logs/recent")
    @Operation(summary = "Get recent audit logs (last 100)")
    public ResponseEntity<List<AuditLog>> getRecentAuditLogs() {
        // This will be implemented in the service to get recent logs
        Pageable pageable = PageRequest.of(0, 100);
        Page<AuditLog> auditLogs = auditService.getAllAuditLogs(pageable);
        return ResponseEntity.ok(auditLogs.getContent());
    }

    @GetMapping("/logs/security")
    @Operation(summary = "Get security-related audit logs")
    public ResponseEntity<Page<AuditLog>> getSecurityAuditLogs(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            
            // First try to get all audit logs and filter in memory as a fallback
            Page<AuditLog> allLogs = auditService.getAllAuditLogs(pageable);
            
            // Filter for security events
            List<AuditLog> securityLogs = allLogs.getContent().stream()
                    .filter(log -> log.getEntityType() == AuditEntityType.SECURITY_EVENT)
                    .collect(Collectors.toList());
            
            // Create a new page with filtered content
            Page<AuditLog> securityPage = new PageImpl<>(
                    securityLogs, 
                    pageable, 
                    allLogs.getTotalElements()
            );
            
            return ResponseEntity.ok(securityPage);
            
        } catch (Exception e) {
            logger.error("Error retrieving security audit logs: {}", e.getMessage(), e);
            Pageable pageable = PageRequest.of(page, size);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Page.empty(pageable));
        }
    }

    @GetMapping("/logs/failed")
    @Operation(summary = "Get failed audit events")
    public ResponseEntity<Page<AuditLog>> getFailedAuditLogs(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        // This will be implemented to get failed events
        Page<AuditLog> auditLogs = auditService.searchAuditLogs(
                null, null, null, null, null, pageable);
        return ResponseEntity.ok(auditLogs);
    }

    @GetMapping("/report/compliance")
    @Operation(summary = "Generate compliance audit report")
    public ResponseEntity<String> generateComplianceReport(
            @Parameter(description = "Start date (ISO format)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date (ISO format)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        try {
            String report = auditService.generateComplianceReport(startDate, endDate);
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(report);
        } catch (Exception e) {
            logger.error("Error generating compliance report: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error generating compliance report: " + e.getMessage());
        }
    }

    @GetMapping("/export/csv")
    @Operation(summary = "Export audit logs to CSV")
    public ResponseEntity<byte[]> exportAuditLogsToCsv(
            @Parameter(description = "Start date (ISO format)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date (ISO format)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        try {
            byte[] csvData = auditService.exportAuditLogsToCsv(startDate, endDate);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", 
                    "audit_logs_" + startDate.toLocalDate() + "_to_" + endDate.toLocalDate() + ".csv");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(csvData);
        } catch (Exception e) {
            logger.error("Error exporting audit logs to CSV: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error exporting audit logs".getBytes());
        }
    }

    @GetMapping("/statistics/summary")
    @Operation(summary = "Get audit statistics summary")
    public ResponseEntity<AuditStatistics> getAuditStatistics(
            @Parameter(description = "Start date (ISO format)")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date (ISO format)")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        try {
            // Set default date range if not provided (last 30 days)
            if (startDate == null) {
                startDate = LocalDateTime.now().minusDays(30);
            }
            if (endDate == null) {
                endDate = LocalDateTime.now();
            }
            
            List<AuditLog> logs = auditService.getAuditLogsByDateRange(startDate, endDate);
            
            AuditStatistics statistics = new AuditStatistics();
            statistics.setTotalEvents(logs.size());
            statistics.setStartDate(startDate);
            statistics.setEndDate(endDate);
            statistics.setGeneratedAt(LocalDateTime.now());
            
            // Calculate statistics
            long securityEvents = logs.stream()
                    .filter(l -> l.getEntityType() == AuditEntityType.SECURITY_EVENT)
                    .count();
            long failedEvents = logs.stream()
                    .filter(l -> "FAILED".equals(l.getStatus()))
                    .count();
            
            statistics.setSecurityEvents(securityEvents);
            statistics.setFailedEvents(failedEvents);
            
            // Avoid division by zero
            if (logs.size() > 0) {
                statistics.setSuccessRate((double)(logs.size() - failedEvents) / logs.size() * 100);
            } else {
                statistics.setSuccessRate(0.0);
            }
            
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            logger.error("Error generating audit statistics: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/logs/cleanup")
    @Operation(summary = "Clean up old audit logs (admin only)")
    public ResponseEntity<String> cleanupOldAuditLogs(
            @Parameter(description = "Retention period in days")
            @RequestParam(defaultValue = "365") int retentionDays) {
        
        try {
            auditService.cleanupOldAuditLogs(retentionDays);
            return ResponseEntity.ok("Cleanup completed successfully. Retention period: " + retentionDays + " days");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Cleanup failed: " + e.getMessage());
        }
    }

    // DTO for audit statistics
    public static class AuditStatistics {
        private long totalEvents;
        private long securityEvents;
        private long failedEvents;
        private double successRate;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private LocalDateTime generatedAt;

        // Getters and Setters
        public long getTotalEvents() { return totalEvents; }
        public void setTotalEvents(long totalEvents) { this.totalEvents = totalEvents; }

        public long getSecurityEvents() { return securityEvents; }
        public void setSecurityEvents(long securityEvents) { this.securityEvents = securityEvents; }

        public long getFailedEvents() { return failedEvents; }
        public void setFailedEvents(long failedEvents) { this.failedEvents = failedEvents; }

        public double getSuccessRate() { return successRate; }
        public void setSuccessRate(double successRate) { this.successRate = successRate; }

        public LocalDateTime getStartDate() { return startDate; }
        public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

        public LocalDateTime getEndDate() { return endDate; }
        public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

        public LocalDateTime getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    }
}
