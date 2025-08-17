package com.microbank.bankingservice.aspect;

import com.microbank.bankingservice.domain.AuditAction;
import com.microbank.bankingservice.domain.AuditEntityType;
import com.microbank.bankingservice.service.AuditService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;

@Aspect
@Component
public class AuditAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(AuditAspect.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    private final AuditService auditService;
    
    @Autowired
    public AuditAspect(AuditService auditService) {
        this.auditService = auditService;
    }
    
    // Pointcut for all service methods
    @Pointcut("execution(* com.microbank.bankingservice.service.*.*(..))")
    public void serviceMethods() {}
    
    // Pointcut for all controller methods
    @Pointcut("execution(* com.microbank.bankingservice.controller.*.*(..))")
    public void controllerMethods() {}
    
    // Pointcut for methods annotated with @Auditable
    @Pointcut("@annotation(com.microbank.bankingservice.annotation.Auditable)")
    public void auditableMethods() {}
    
    // Around advice for auditable methods
    @Around("auditableMethods()")
    public Object auditMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = null;
        boolean success = false;
        String errorMessage = null;
        
        try {
            // Execute the method
            result = joinPoint.proceed();
            success = true;
            return result;
        } catch (Exception e) {
            errorMessage = e.getMessage();
            throw e;
        } finally {
            // Audit the method execution
            try {
                auditMethodExecution(joinPoint, startTime, success, errorMessage, result);
            } catch (Exception auditException) {
                logger.error("Failed to audit method execution: {}", auditException.getMessage(), auditException);
            }
        }
    }
    
    // Around advice for service methods (automatic auditing)
    @Around("serviceMethods() && !auditableMethods()")
    public Object auditServiceMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = null;
        boolean success = false;
        String errorMessage = null;
        
        try {
            result = joinPoint.proceed();
            success = true;
            return result;
        } catch (Exception e) {
            errorMessage = e.getMessage();
            throw e;
        } finally {
            // Only audit critical operations automatically
            if (isCriticalOperation(joinPoint)) {
                try {
                    auditMethodExecution(joinPoint, startTime, success, errorMessage, result);
                } catch (Exception auditException) {
                    logger.error("Failed to audit service method: {}", auditException.getMessage(), auditException);
                }
            }
        }
    }
    
    private void auditMethodExecution(ProceedingJoinPoint joinPoint, long startTime, 
                                    boolean success, String errorMessage, Object result) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            String methodName = method.getName();
            String className = method.getDeclaringClass().getSimpleName();
            
            // Get current user context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = "SYSTEM";
            String userRole = "SYSTEM";
            Long userId = null;
            
            if (authentication != null && authentication.isAuthenticated() && 
                !"anonymousUser".equals(authentication.getName())) {
                userEmail = authentication.getName();
                userRole = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                    .orElse("USER");
                
                // Try to extract user ID from principal if available
                if (authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
                    // This would need to be adapted based on your UserDetails implementation
                    // userId = extractUserId(authentication.getPrincipal());
                }
            }
            
            // Determine entity type and action based on method name and class
            AuditEntityType entityType = determineEntityType(className, methodName);
            AuditAction action = determineAction(methodName);
            
            // Extract entity ID from method parameters if possible
            Long entityId = extractEntityId(joinPoint.getArgs(), methodName);
            
            // Build action details
            StringBuilder actionDetails = new StringBuilder();
            actionDetails.append(className).append(".").append(methodName);
            actionDetails.append(" - Execution time: ").append(System.currentTimeMillis() - startTime).append("ms");
            
            if (!success) {
                actionDetails.append(" - Failed: ").append(errorMessage);
            }
            
            // Log the audit event
            if (success) {
                auditService.logEvent(userId, userEmail, userRole, entityType, entityId, action, actionDetails.toString());
            } else {
                // Log failed operation
                auditService.logEvent(userId, userEmail, userRole, entityType, entityId, action, actionDetails.toString());
            }
            
        } catch (Exception e) {
            logger.error("Failed to audit method execution: {}", e.getMessage(), e);
        }
    }
    
    private boolean isCriticalOperation(ProceedingJoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName().toLowerCase();
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName().toLowerCase();
        
        // Define critical operations that should always be audited
        return methodName.contains("create") || methodName.contains("update") || 
               methodName.contains("delete") || methodName.contains("process") ||
               methodName.contains("transfer") || methodName.contains("withdraw") ||
               methodName.contains("deposit") || className.contains("security") ||
               className.contains("auth");
    }
    
    private AuditEntityType determineEntityType(String className, String methodName) {
        if (className.toLowerCase().contains("account")) {
            return AuditEntityType.ACCOUNT;
        } else if (className.toLowerCase().contains("transaction")) {
            return AuditEntityType.TRANSACTION;
        } else if (className.toLowerCase().contains("client")) {
            return AuditEntityType.CLIENT;
        } else if (className.toLowerCase().contains("security") || className.toLowerCase().contains("auth")) {
            return AuditEntityType.SECURITY_EVENT;
        } else {
            return AuditEntityType.SYSTEM_EVENT;
        }
    }
    
    private AuditAction determineAction(String methodName) {
        String lowerMethodName = methodName.toLowerCase();
        
        if (lowerMethodName.contains("create") || lowerMethodName.contains("save")) {
            return AuditAction.CREATE;
        } else if (lowerMethodName.contains("update") || lowerMethodName.contains("modify")) {
            return AuditAction.UPDATE;
        } else if (lowerMethodName.contains("delete") || lowerMethodName.contains("remove")) {
            return AuditAction.DELETE;
        } else if (lowerMethodName.contains("process")) {
            return AuditAction.UPDATE;
        } else if (lowerMethodName.contains("transfer")) {
            return AuditAction.TRANSFER;
        } else if (lowerMethodName.contains("withdraw")) {
            return AuditAction.WITHDRAWAL;
        } else if (lowerMethodName.contains("deposit")) {
            return AuditAction.DEPOSIT;
        } else if (lowerMethodName.contains("login")) {
            return AuditAction.LOGIN;
        } else if (lowerMethodName.contains("logout")) {
            return AuditAction.LOGOUT;
        } else {
            return AuditAction.READ;
        }
    }
    
    private Long extractEntityId(Object[] args, String methodName) {
        try {
            // Try to find entity ID in method parameters
            for (Object arg : args) {
                if (arg instanceof Long) {
                    return (Long) arg;
                } else if (arg instanceof String && methodName.contains("By")) {
                    // For methods like "findById", "getByAccountNumber", etc.
                    try {
                        return Long.parseLong((String) arg);
                    } catch (NumberFormatException e) {
                        // Not a numeric ID
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Could not extract entity ID from method parameters: {}", e.getMessage());
        }
        return null;
    }
    
    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                
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
        } catch (Exception e) {
            logger.debug("Could not get client IP address: {}", e.getMessage());
        }
        return "unknown";
    }
    
    private String serializeObject(Object obj) {
        try {
            if (obj == null) return null;
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.debug("Could not serialize object: {}", e.getMessage());
            return obj.toString();
        }
    }
}
