package com.microbank.bankingservice.service;

import com.microbank.bankingservice.domain.AccountStatus;
import com.microbank.bankingservice.domain.AccountType;
import com.microbank.bankingservice.dto.AccountResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {

    /**
     * Create a new account for a client
     */
    AccountResponse createAccount(Long clientId, AccountType accountType);

    /**
     * Get account by ID
     */
    AccountResponse getAccountById(Long accountId);

    /**
     * Get account by account number
     */
    AccountResponse getAccountByNumber(String accountNumber);

    /**
     * Get all accounts for a client
     */
    List<AccountResponse> getAccountsByClientId(Long clientId);

    /**
     * Get accounts by client ID with pagination
     */
    Page<AccountResponse> getAccountsByClientId(Long clientId, Pageable pageable);

    /**
     * Get accounts by status
     */
    Page<AccountResponse> getAccountsByStatus(AccountStatus status, Pageable pageable);

    /**
     * Get accounts by type
     */
    Page<AccountResponse> getAccountsByType(AccountType accountType, Pageable pageable);

    /**
     * Update account status
     */
    AccountResponse updateAccountStatus(Long accountId, AccountStatus status);

    /**
     * Close account
     */
    AccountResponse closeAccount(Long accountId);

    /**
     * Get account balance
     */
    BigDecimal getAccountBalance(Long accountId);

    /**
     * Check if account can perform transactions
     */
    boolean canAccountTransact(Long accountId);

    /**
     * Get account statistics
     */
    long getTotalAccounts();

    /**
     * Get active accounts count
     */
    long getActiveAccountsCount();

    /**
     * Get accounts count by type
     */
    long getAccountsCountByType(AccountType accountType);
}
