package com.microbank.bankingservice.service;

import com.microbank.bankingservice.domain.TransactionType;
import com.microbank.bankingservice.dto.TransactionRequest;
import com.microbank.bankingservice.dto.TransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionService {

    /**
     * Process a deposit transaction
     */
    TransactionResponse processDeposit(TransactionRequest request);

    /**
     * Process a withdrawal transaction
     */
    TransactionResponse processWithdrawal(TransactionRequest request);

    /**
     * Process a transfer transaction
     */
    TransactionResponse processTransfer(TransactionRequest request);

    /**
     * Get transaction by ID
     */
    TransactionResponse getTransactionById(Long transactionId);

    /**
     * Get transaction by reference number
     */
    TransactionResponse getTransactionByReference(String referenceNumber);

    /**
     * Get transactions by account ID
     */
    List<TransactionResponse> getTransactionsByAccountId(Long accountId);

    /**
     * Get transactions by account ID with pagination
     */
    Page<TransactionResponse> getTransactionsByAccountId(Long accountId, Pageable pageable);

    /**
     * Get transactions by client ID
     */
    List<TransactionResponse> getTransactionsByClientId(Long clientId);

    /**
     * Get transactions by client ID with pagination
     */
    Page<TransactionResponse> getTransactionsByClientId(Long clientId, Pageable pageable);

    /**
     * Get transactions by type
     */
    Page<TransactionResponse> getTransactionsByType(TransactionType transactionType, Pageable pageable);

    /**
     * Get transactions by date range
     */
    List<TransactionResponse> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Get transactions by account and date range
     */
    List<TransactionResponse> getTransactionsByAccountAndDateRange(Long accountId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Get transactions by client and date range
     */
    List<TransactionResponse> getTransactionsByClientAndDateRange(Long clientId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Reverse a transaction
     */
    TransactionResponse reverseTransaction(Long transactionId, String reason);

    /**
     * Get transaction statistics
     */
    long getTotalTransactions();

    /**
     * Get transactions count by type
     */
    long getTransactionsCountByType(TransactionType transactionType);

    /**
     * Get total transaction amount by type
     */
    BigDecimal getTotalTransactionAmountByType(TransactionType transactionType);

    /**
     * Generate unique reference number
     */
    String generateReferenceNumber();
}
