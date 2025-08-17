package com.microbank.apigateway.domain;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "gateway_audit_logs", indexes = {
    @Index(name = "idx_gateway_user_id", columnList = "user_id"),
    @Index(name = "idx_gateway_service", columnList = "target_service"),
    @Index(name = "idx_gateway_endpoint", columnList = "endpoint"),
    @Index(name = "idx_gateway_method", columnList = "http_method"),
    @Index(name = "idx_gateway_status", columnList = "response_status"),
    @Index(name = "idx_gateway_created_at", columnList = "created_at")
})
@EntityListeners(AuditingEntityListener.class)
public class GatewayAuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "user_email")
    private String userEmail;
    
    @Column(name = "user_role")
    private String userRole;
    
    @Column(name = "target_service", nullable = false)
    private String targetService;
    
    @Column(name = "endpoint", nullable = false)
    private String endpoint;
    
    @Column(name = "http_method", nullable = false)
    private String httpMethod;
    
    @Column(name = "request_headers", columnDefinition = "TEXT")
    private String requestHeaders;
    
    @Column(name = "request_body", columnDefinition = "TEXT")
    private String requestBody;
    
    @Column(name = "response_status")
    private Integer responseStatus;
    
    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "user_agent")
    private String userAgent;
    
    @Column(name = "session_id")
    private String sessionId;
    
    @Column(name = "request_id", nullable = false)
    private String requestId;
    
    @Column(name = "execution_time_ms")
    private Long executionTimeMs;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "rate_limited")
    private Boolean rateLimited = false;
    
    @Column(name = "blocked")
    private Boolean blocked = false;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;
    
    // Constructors
    public GatewayAuditLog() {}
    
    public GatewayAuditLog(String targetService, String endpoint, String httpMethod, String requestId) {
        this.targetService = targetService;
        this.endpoint = endpoint;
        this.httpMethod = httpMethod;
        this.requestId = requestId;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    
    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }
    
    public String getTargetService() { return targetService; }
    public void setTargetService(String targetService) { this.targetService = targetService; }
    
    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    
    public String getHttpMethod() { return httpMethod; }
    public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }
    
    public String getRequestHeaders() { return requestHeaders; }
    public void setRequestHeaders(String requestHeaders) { this.requestHeaders = requestHeaders; }
    
    public String getRequestBody() { return requestBody; }
    public void setRequestBody(String requestBody) { this.requestBody = requestBody; }
    
    public Integer getResponseStatus() { return responseStatus; }
    public void setResponseStatus(Integer responseStatus) { this.responseStatus = responseStatus; }
    
    public String getResponseBody() { return responseBody; }
    public void setResponseBody(String responseBody) { this.responseBody = responseBody; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    
    public Long getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(Long executionTimeMs) { this.executionTimeMs = executionTimeMs; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    public Boolean getRateLimited() { return rateLimited; }
    public void setRateLimited(Boolean rateLimited) { this.rateLimited = rateLimited; }
    
    public Boolean getBlocked() { return blocked; }
    public void setBlocked(Boolean blocked) { this.blocked = blocked; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    // Business methods
    public boolean isSuccessful() {
        return responseStatus != null && responseStatus >= 200 && responseStatus < 300;
    }
    
    public boolean isError() {
        return responseStatus != null && responseStatus >= 400;
    }
    
    public boolean isBlockedOrRateLimited() {
        return Boolean.TRUE.equals(blocked) || Boolean.TRUE.equals(rateLimited);
    }
    
    // Equals and HashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GatewayAuditLog that = (GatewayAuditLog) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "GatewayAuditLog{" +
                "id=" + id +
                ", targetService='" + targetService + '\'' +
                ", endpoint='" + endpoint + '\'' +
                ", httpMethod='" + httpMethod + '\'' +
                ", requestId='" + requestId + '\'' +
                ", responseStatus=" + responseStatus +
                ", executionTimeMs=" + executionTimeMs +
                ", createdAt=" + createdAt +
                '}';
    }
}
