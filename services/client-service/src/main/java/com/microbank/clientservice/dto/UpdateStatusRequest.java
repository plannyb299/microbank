package com.microbank.clientservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class UpdateStatusRequest {
    
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(ACTIVE|SUSPENDED|INACTIVE)$", message = "Status must be ACTIVE, SUSPENDED, or INACTIVE")
    private String status;
    
    // Default constructor
    public UpdateStatusRequest() {}
    
    // Constructor with status
    public UpdateStatusRequest(String status) {
        this.status = status;
    }
    
    // Getters and Setters
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    @Override
    public String toString() {
        return "UpdateStatusRequest{" +
                "status='" + status + '\'' +
                '}';
    }
}
