package com.microbank.bankingservice.repository;

import com.microbank.bankingservice.domain.AuditAction;
import com.microbank.bankingservice.domain.AuditEntityType;
import com.microbank.bankingservice.domain.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    // Find by entity type and ID
    List<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
            AuditEntityType entityType, Long entityId);
    
    // Find by user ID
    List<AuditLog> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    // Find by client ID
    List<AuditLog> findByClientIdOrderByCreatedAtDesc(Long clientId);
    
    // Find by action
    List<AuditLog> findByActionOrderByCreatedAtDesc(AuditAction action);
    
    // Find by date range
    List<AuditLog> findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime startDate, LocalDateTime endDate);
    
    // Find by user email
    List<AuditLog> findByUserEmailOrderByCreatedAtDesc(String userEmail);
    
    // Find by entity type
    List<AuditLog> findByEntityTypeOrderByCreatedAtDesc(AuditEntityType entityType);
    
    // Find by status
    List<AuditLog> findByStatusOrderByCreatedAtDesc(String status);
    
    // Find by IP address
    List<AuditLog> findByIpAddressOrderByCreatedAtDesc(String ipAddress);
    
    // Find by session ID
    List<AuditLog> findBySessionIdOrderByCreatedAtDesc(String sessionId);
    
    // Find by request ID
    List<AuditLog> findByRequestIdOrderByCreatedAtDesc(String requestId);
    
    // Complex search query
    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:searchTerm IS NULL OR LOWER(a.actionDetails) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.userEmail) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "(:entityType IS NULL OR a.entityType = :entityType) AND " +
           "(:action IS NULL OR a.action = :action) AND " +
           "(:startDate IS NULL OR a.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR a.createdAt <= :endDate)")
    Page<AuditLog> searchAuditLogs(
            @Param("searchTerm") String searchTerm,
            @Param("entityType") AuditEntityType entityType,
            @Param("action") AuditAction action,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
    
    // Count by action and date range
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.action = :action AND a.createdAt BETWEEN :startDate AND :endDate")
    long countByActionAndDateRange(
            @Param("action") AuditAction action,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    // Count by entity type and date range
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.entityType = :entityType AND a.createdAt BETWEEN :startDate AND :endDate")
    long countByEntityTypeAndDateRange(
            @Param("entityType") AuditEntityType entityType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    // Find by user role
    List<AuditLog> findByUserRoleOrderByCreatedAtDesc(String userRole);
    
    // Find recent audit logs
    List<AuditLog> findTop100ByOrderByCreatedAtDesc();
    
    // Find audit logs older than specified date (for cleanup)
    List<AuditLog> findByCreatedAtBefore(LocalDateTime date);
    
    // Delete audit logs older than specified date
    void deleteByCreatedAtBefore(LocalDateTime date);
}
