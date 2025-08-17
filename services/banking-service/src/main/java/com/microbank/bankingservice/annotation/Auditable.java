package com.microbank.bankingservice.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods that should be audited.
 * Methods annotated with @Auditable will be automatically logged
 * with detailed information about the execution.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    
    /**
     * Optional description of what this method does for audit purposes
     */
    String value() default "";
    
    /**
     * Whether to capture method parameters in the audit log
     */
    boolean captureParameters() default true;
    
    /**
     * Whether to capture the return value in the audit log
     */
    boolean captureReturnValue() default false;
    
    /**
     * Whether to capture execution time
     */
    boolean captureExecutionTime() default true;
    
    /**
     * Custom entity type for this auditable method
     */
    String entityType() default "";
    
    /**
     * Custom action type for this auditable method
     */
    String action() default "";
}
