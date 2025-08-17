package com.microbank.bankingservice.service.impl;

import com.microbank.bankingservice.domain.Account;
import com.microbank.bankingservice.domain.AccountStatus;
import com.microbank.bankingservice.domain.AccountType;
import com.microbank.bankingservice.dto.AccountResponse;
import com.microbank.bankingservice.repository.AccountRepository;
import com.microbank.bankingservice.service.AccountService;
import com.microbank.bankingservice.service.ClientValidationService;
import com.microbank.bankingservice.exception.ClientAccessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final ClientValidationService clientValidationService;

    @Autowired
    public AccountServiceImpl(AccountRepository accountRepository, ClientValidationService clientValidationService) {
        this.accountRepository = accountRepository;
        this.clientValidationService = clientValidationService;
    }

    @Override
    public AccountResponse createAccount(Long clientId, AccountType accountType) {
        // Validate that client can perform transactions before creating account
        clientValidationService.validateClientCanTransact(clientId);
        
        // Generate unique account number
        String accountNumber = generateAccountNumber();
        
        Account account = new Account(clientId, accountNumber, accountType);
        Account savedAccount = accountRepository.save(account);
        
        return new AccountResponse(savedAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccountById(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + accountId));
        
        // For read-only access, only check if client is active (not blacklisted)
        if (clientValidationService.isClientBlacklisted(account.getClientId())) {
            throw ClientAccessException.clientBlacklisted(account.getClientId());
        }
        
        return new AccountResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccountByNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found with number: " + accountNumber));
        
        // For read-only access, only check if client is active (not blacklisted)
        if (clientValidationService.isClientBlacklisted(account.getClientId())) {
            throw ClientAccessException.clientBlacklisted(account.getClientId());
        }
        
        return new AccountResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountsByClientId(Long clientId) {
        // For read-only access, only check if client is blacklisted
        if (clientValidationService.isClientBlacklisted(clientId)) {
            throw ClientAccessException.clientBlacklisted(clientId);
        }
        
        List<Account> accounts = accountRepository.findByClientIdAndStatusOrderByCreatedAtDesc(clientId, AccountStatus.ACTIVE);
        return accounts.stream()
                .map(AccountResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AccountResponse> getAccountsByClientId(Long clientId, Pageable pageable) {
        // For read-only access, only check if client is blacklisted
        if (clientValidationService.isClientBlacklisted(clientId)) {
            throw ClientAccessException.clientBlacklisted(clientId);
        }
        
        Page<Account> accounts = accountRepository.findByClientId(clientId, pageable);
        return accounts.map(AccountResponse::new);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AccountResponse> getAccountsByStatus(AccountStatus status, Pageable pageable) {
        Page<Account> accounts = accountRepository.findByStatus(status, pageable);
        return accounts.map(AccountResponse::new);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AccountResponse> getAccountsByType(AccountType accountType, Pageable pageable) {
        Page<Account> accounts = accountRepository.findByAccountType(accountType, pageable);
        return accounts.map(AccountResponse::new);
    }

    @Override
    public AccountResponse updateAccountStatus(Long accountId, AccountStatus status) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + accountId));
        
        // Validate that client can perform transactions before updating account status
        clientValidationService.validateClientCanTransact(account.getClientId());
        
        account.setStatus(status);
        Account savedAccount = accountRepository.save(account);
        
        return new AccountResponse(savedAccount);
    }

    @Override
    public AccountResponse closeAccount(Long accountId) {
        // The updateAccountStatus method already includes client validation
        return updateAccountStatus(accountId, AccountStatus.CLOSED);
    }

    @Override
    @Transactional(readOnly = true)
    public java.math.BigDecimal getAccountBalance(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + accountId));
        
        // For read-only access, only check if client is blacklisted
        if (clientValidationService.isClientBlacklisted(account.getClientId())) {
            throw ClientAccessException.clientBlacklisted(account.getClientId());
        }
        
        return account.getBalance();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canAccountTransact(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + accountId));
        
        // Check if client can transact (not blacklisted and active)
        if (!clientValidationService.isClientActive(account.getClientId()) || 
            clientValidationService.isClientBlacklisted(account.getClientId())) {
            return false;
        }
        
        return account.getStatus() == AccountStatus.ACTIVE;
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalAccounts() {
        return accountRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long getActiveAccountsCount() {
        return accountRepository.countByStatus(AccountStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public long getAccountsCountByType(AccountType accountType) {
        return accountRepository.countByAccountType(accountType);
    }

    private String generateAccountNumber() {
        // Simple account number generation - in production, use a more sophisticated approach
        long timestamp = System.currentTimeMillis();
        String accountNumber = "ACC" + String.format("%08d", timestamp % 100000000);
        
        // Ensure uniqueness
        while (accountRepository.existsByAccountNumber(accountNumber)) {
            timestamp = System.currentTimeMillis();
            accountNumber = "ACC" + String.format("%08d", timestamp % 100000000);
        }
        
        return accountNumber;
    }
}
