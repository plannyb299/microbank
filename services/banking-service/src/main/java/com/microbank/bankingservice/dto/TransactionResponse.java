package com.microbank.bankingservice.dto;

import com.microbank.bankingservice.domain.Transaction;
import com.microbank.bankingservice.domain.TransactionStatus;
import com.microbank.bankingservice.domain.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponse {

    private Long id;
    private Long accountId;
    private Long clientId;
    private TransactionType transactionType;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String referenceNumber;
    private String description;
    private String fromAccount;
    private String toAccount;
    private TransactionStatus status;
    private String failureReason;
    private LocalDateTime createdAt;

    // Default constructor
    public TransactionResponse() {}

    // Constructor from Transaction entity
    public TransactionResponse(Transaction transaction) {
        this.id = transaction.getId();
        this.accountId = transaction.getAccountId();
        this.clientId = transaction.getClientId();
        this.transactionType = transaction.getTransactionType();
        this.amount = transaction.getAmount();
        this.balanceAfter = transaction.getBalanceAfter();
        this.referenceNumber = transaction.getReferenceNumber();
        this.description = transaction.getDescription();
        this.fromAccount = transaction.getFromAccount();
        this.toAccount = transaction.getToAccount();
        this.status = transaction.getStatus();
        this.failureReason = transaction.getFailureReason();
        this.createdAt = transaction.getCreatedAt();
    }

    // Constructor with all fields
    public TransactionResponse(Long id, Long accountId, Long clientId, TransactionType transactionType,
                             BigDecimal amount, BigDecimal balanceAfter, String referenceNumber,
                             String description, String fromAccount, String toAccount,
                             TransactionStatus status, String failureReason, LocalDateTime createdAt) {
        this.id = id;
        this.accountId = accountId;
        this.clientId = clientId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.referenceNumber = referenceNumber;
        this.description = description;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.status = status;
        this.failureReason = failureReason;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(BigDecimal balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(String fromAccount) {
        this.fromAccount = fromAccount;
    }

    public String getToAccount() {
        return toAccount;
    }

    public void setToAccount(String toAccount) {
        this.toAccount = toAccount;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "TransactionResponse{" +
                "id=" + id +
                ", accountId=" + accountId +
                ", clientId=" + clientId +
                ", transactionType=" + transactionType +
                ", amount=" + amount +
                ", balanceAfter=" + balanceAfter +
                ", referenceNumber='" + referenceNumber + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}
