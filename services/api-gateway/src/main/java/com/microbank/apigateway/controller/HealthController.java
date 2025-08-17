package com.microbank.apigateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("service", "API Gateway");
        health.put("port", 8083);
        health.put("version", "1.0.0");
        
        return ResponseEntity.ok(health);
    }

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> root() {
        Map<String, Object> info = new HashMap<>();
        info.put("service", "Microbank API Gateway");
        info.put("status", "Running");
        info.put("port", 8083);
        info.put("timestamp", LocalDateTime.now());
        info.put("endpoints", Map.of(
            "health", "/health",
            "client-service", "/api/v1/clients/**",
            "banking-service", "/api/v1/banking/**",
            "audit-service", "/api/v1/audit/**"
        ));
        
        return ResponseEntity.ok(info);
    }

    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> test() {
        Map<String, Object> test = new HashMap<>();
        test.put("message", "API Gateway is working!");
        test.put("timestamp", LocalDateTime.now());
        test.put("cors", "This should work from any origin");
        return ResponseEntity.ok(test);
    }
}
