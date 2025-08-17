package com.microbank.clientservice.dto;

import jakarta.validation.constraints.NotBlank;

public class BlacklistRequest {

    @NotBlank(message = "Blacklist reason is required")
    private String reason;

    // Default constructor
    public BlacklistRequest() {}

    // Constructor with reason
    public BlacklistRequest(String reason) {
        this.reason = reason;
    }

    // Getters and Setters
    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "BlacklistRequest{" +
                "reason='" + reason + '\'' +
                '}';
    }
}
