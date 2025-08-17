package com.microbank.bankingservice.dto;

import com.microbank.bankingservice.domain.Account;
import com.microbank.bankingservice.domain.AccountStatus;
import com.microbank.bankingservice.domain.AccountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AccountResponse {

    private Long id;
    private Long clientId;
    private String accountNumber;
    private BigDecimal balance;
    private AccountStatus status;
    private AccountType accountType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default constructor
    public AccountResponse() {}

    // Constructor from Account entity
    public AccountResponse(Account account) {
        this.id = account.getId();
        this.clientId = account.getClientId();
        this.accountNumber = account.getAccountNumber();
        this.balance = account.getBalance();
        this.status = account.getStatus();
        this.accountType = account.getAccountType();
        this.createdAt = account.getCreatedAt();
        this.updatedAt = account.getUpdatedAt();
    }

    // Constructor with all fields
    public AccountResponse(Long id, Long clientId, String accountNumber, BigDecimal balance,
                         AccountStatus status, AccountType accountType,
                         LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.clientId = clientId;
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.status = status;
        this.accountType = accountType;
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

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
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
        return "AccountResponse{" +
                "id=" + id +
                ", clientId=" + clientId +
                ", accountNumber='" + accountNumber + '\'' +
                ", balance=" + balance +
                ", status=" + status +
                ", accountType=" + accountType +
                ", createdAt=" + createdAt +
                '}';
    }
}
