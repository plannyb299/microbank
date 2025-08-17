package com.microbank.clientservice.dto;

import com.microbank.clientservice.domain.Client;
import com.microbank.clientservice.domain.ClientStatus;
import com.microbank.clientservice.domain.ClientRole;

import java.time.LocalDateTime;

public class ClientResponse {

    private Long id;
    private String email;
    private String name;
    private boolean blacklisted;
    private String blacklistReason;
    private ClientStatus status;
    private ClientRole role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default constructor
    public ClientResponse() {}

    // Constructor from Client entity
    public ClientResponse(Client client) {
        this.id = client.getId();
        this.email = client.getEmail();
        this.name = client.getName();
        this.blacklisted = client.isBlacklisted();
        this.blacklistReason = client.getBlacklistReason();
        this.status = client.getStatus();
        this.role = client.getRole();
        this.createdAt = client.getCreatedAt();
        this.updatedAt = client.getUpdatedAt();
    }

    // Constructor with all fields
    public ClientResponse(Long id, String email, String name, boolean blacklisted, 
                        String blacklistReason, ClientStatus status, ClientRole role,
                        LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.blacklisted = blacklisted;
        this.blacklistReason = blacklistReason;
        this.status = status;
        this.role = role;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isBlacklisted() {
        return blacklisted;
    }

    public void setBlacklisted(boolean blacklisted) {
        this.blacklisted = blacklisted;
    }

    public String getBlacklistReason() {
        return blacklistReason;
    }

    public void setBlacklistReason(String blacklistReason) {
        this.blacklistReason = blacklistReason;
    }

    public ClientStatus getStatus() {
        return status;
    }

    public void setStatus(ClientStatus status) {
        this.status = status;
    }

    public ClientRole getRole() {
        return role;
    }

    public void setRole(ClientRole role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "ClientResponse{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", blacklisted=" + blacklisted +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}
