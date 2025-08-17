package com.microbank.bankingservice.controller;

import com.microbank.bankingservice.domain.AccountStatus;
import com.microbank.bankingservice.domain.AccountType;
import com.microbank.bankingservice.domain.TransactionType;
import com.microbank.bankingservice.dto.AccountResponse;
import com.microbank.bankingservice.dto.TransactionRequest;
import com.microbank.bankingservice.dto.TransactionResponse;
import com.microbank.bankingservice.service.AccountService;
import com.microbank.bankingservice.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Banking Operations", description = "APIs for banking operations")
public class BankingController {

    private final AccountService accountService;
    private final TransactionService transactionService;

    @Autowired
    public BankingController(AccountService accountService, TransactionService transactionService) {
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    // Account Management Endpoints

    @PostMapping("/accounts")
    @Operation(summary = "Create a new account")
    public ResponseEntity<AccountResponse> createAccount(
            @RequestParam Long clientId,
            @RequestParam AccountType accountType) {
        AccountResponse account = accountService.createAccount(clientId, accountType);
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @GetMapping("/accounts/{accountId}")
    @Operation(summary = "Get account by ID")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable Long accountId) {
        AccountResponse account = accountService.getAccountById(accountId);
        return ResponseEntity.ok(account);
    }

    @GetMapping("/accounts/number/{accountNumber}")
    @Operation(summary = "Get account by account number")
    public ResponseEntity<AccountResponse> getAccountByNumber(@PathVariable String accountNumber) {
        AccountResponse account = accountService.getAccountByNumber(accountNumber);
        return ResponseEntity.ok(account);
    }

    @GetMapping("/accounts/client/{clientId}")
    @Operation(summary = "Get all accounts for a client")
    public ResponseEntity<List<AccountResponse>> getAccountsByClient(@PathVariable Long clientId) {
        List<AccountResponse> accounts = accountService.getAccountsByClientId(clientId);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/accounts/client/{clientId}/page")
    @Operation(summary = "Get accounts for a client with pagination")
    public ResponseEntity<Page<AccountResponse>> getAccountsByClientPage(
            @PathVariable Long clientId, Pageable pageable) {
        Page<AccountResponse> accounts = accountService.getAccountsByClientId(clientId, pageable);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/accounts/status/{status}")
    @Operation(summary = "Get accounts by status")
    public ResponseEntity<Page<AccountResponse>> getAccountsByStatus(
            @PathVariable AccountStatus status, Pageable pageable) {
        Page<AccountResponse> accounts = accountService.getAccountsByStatus(status, pageable);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/accounts/type/{accountType}")
    @Operation(summary = "Get accounts by type")
    public ResponseEntity<Page<AccountResponse>> getAccountsByType(
            @PathVariable AccountType accountType, Pageable pageable) {
        Page<AccountResponse> accounts = accountService.getAccountsByType(accountType, pageable);
        return ResponseEntity.ok(accounts);
    }

    @PutMapping("/accounts/{accountId}/status")
    @Operation(summary = "Update account status")
    public ResponseEntity<AccountResponse> updateAccountStatus(
            @PathVariable Long accountId, @RequestParam AccountStatus status) {
        AccountResponse account = accountService.updateAccountStatus(accountId, status);
        return ResponseEntity.ok(account);
    }

    @DeleteMapping("/accounts/{accountId}")
    @Operation(summary = "Close account")
    public ResponseEntity<AccountResponse> closeAccount(@PathVariable Long accountId) {
        AccountResponse account = accountService.closeAccount(accountId);
        return ResponseEntity.ok(account);
    }

    @GetMapping("/accounts/{accountId}/balance")
    @Operation(summary = "Get account balance")
    public ResponseEntity<BigDecimal> getAccountBalance(@PathVariable Long accountId) {
        BigDecimal balance = accountService.getAccountBalance(accountId);
        return ResponseEntity.ok(balance);
    }

    @GetMapping("/accounts/{accountId}/can-transact")
    @Operation(summary = "Check if account can transact")
    public ResponseEntity<Boolean> canAccountTransact(@PathVariable Long accountId) {
        boolean canTransact = accountService.canAccountTransact(accountId);
        return ResponseEntity.ok(canTransact);
    }

    // Transaction Endpoints

    @PostMapping("/transactions/deposit")
    @Operation(summary = "Process a deposit transaction")
    public ResponseEntity<TransactionResponse> processDeposit(@Valid @RequestBody TransactionRequest request) {
        TransactionResponse transaction = transactionService.processDeposit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @PostMapping("/transactions/withdraw")
    @Operation(summary = "Process a withdrawal transaction")
    public ResponseEntity<TransactionResponse> processWithdrawal(@Valid @RequestBody TransactionRequest request) {
        TransactionResponse transaction = transactionService.processWithdrawal(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @PostMapping("/transactions/transfer")
    @Operation(summary = "Process a transfer transaction")
    public ResponseEntity<TransactionResponse> processTransfer(@Valid @RequestBody TransactionRequest request) {
        TransactionResponse transaction = transactionService.processTransfer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @GetMapping("/transactions/{transactionId}")
    @Operation(summary = "Get transaction by ID")
    public ResponseEntity<TransactionResponse> getTransaction(@PathVariable Long transactionId) {
        TransactionResponse transaction = transactionService.getTransactionById(transactionId);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/transactions/reference/{referenceNumber}")
    @Operation(summary = "Get transaction by reference number")
    public ResponseEntity<TransactionResponse> getTransactionByReference(@PathVariable String referenceNumber) {
        TransactionResponse transaction = transactionService.getTransactionByReference(referenceNumber);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/transactions/account/{accountId}")
    @Operation(summary = "Get transactions by account ID")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByAccount(@PathVariable Long accountId) {
        List<TransactionResponse> transactions = transactionService.getTransactionsByAccountId(accountId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/transactions/account/{accountId}/page")
    @Operation(summary = "Get transactions by account ID with pagination")
    public ResponseEntity<Page<TransactionResponse>> getTransactionsByAccountPage(
            @PathVariable Long accountId, Pageable pageable) {
        Page<TransactionResponse> transactions = transactionService.getTransactionsByAccountId(accountId, pageable);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/transactions/client/{clientId}")
    @Operation(summary = "Get transactions by client ID")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByClient(@PathVariable Long clientId) {
        List<TransactionResponse> transactions = transactionService.getTransactionsByClientId(clientId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/transactions/client/{clientId}/page")
    @Operation(summary = "Get transactions by client ID with pagination")
    public ResponseEntity<Page<TransactionResponse>> getTransactionsByClientPage(
            @PathVariable Long clientId, Pageable pageable) {
        Page<TransactionResponse> transactions = transactionService.getTransactionsByClientId(clientId, pageable);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/transactions/type/{transactionType}")
    @Operation(summary = "Get transactions by type")
    public ResponseEntity<Page<TransactionResponse>> getTransactionsByType(
            @PathVariable TransactionType transactionType, Pageable pageable) {
        Page<TransactionResponse> transactions = transactionService.getTransactionsByType(transactionType, pageable);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/transactions/date-range")
    @Operation(summary = "Get transactions by date range")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByDateRange(
            @RequestParam LocalDateTime startDate, @RequestParam LocalDateTime endDate) {
        List<TransactionResponse> transactions = transactionService.getTransactionsByDateRange(startDate, endDate);
        return ResponseEntity.ok(transactions);
    }

    @PostMapping("/transactions/{transactionId}/reverse")
    @Operation(summary = "Reverse a transaction")
    public ResponseEntity<TransactionResponse> reverseTransaction(
            @PathVariable Long transactionId, @RequestParam String reason) {
        TransactionResponse transaction = transactionService.reverseTransaction(transactionId, reason);
        return ResponseEntity.ok(transaction);
    }

    // Statistics Endpoints

    @GetMapping("/statistics/accounts/total")
    @Operation(summary = "Get total accounts count")
    public ResponseEntity<Long> getTotalAccounts() {
        long total = accountService.getTotalAccounts();
        return ResponseEntity.ok(total);
    }

    @GetMapping("/statistics/accounts/active")
    @Operation(summary = "Get active accounts count")
    public ResponseEntity<Long> getActiveAccountsCount() {
        long active = accountService.getActiveAccountsCount();
        return ResponseEntity.ok(active);
    }

    @GetMapping("/statistics/accounts/type/{accountType}")
    @Operation(summary = "Get accounts count by type")
    public ResponseEntity<Long> getAccountsCountByType(@PathVariable AccountType accountType) {
        long count = accountService.getAccountsCountByType(accountType);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/statistics/transactions/total")
    @Operation(summary = "Get total transactions count")
    public ResponseEntity<Long> getTotalTransactions() {
        long total = transactionService.getTotalTransactions();
        return ResponseEntity.ok(total);
    }

    @GetMapping("/statistics/transactions/type/{transactionType}")
    @Operation(summary = "Get transactions count by type")
    public ResponseEntity<Long> getTransactionsCountByType(@PathVariable TransactionType transactionType) {
        long count = transactionService.getTransactionsCountByType(transactionType);
        return ResponseEntity.ok(count);
    }
}
