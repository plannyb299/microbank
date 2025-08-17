package com.microbank.bankingservice.service.impl;

import com.microbank.bankingservice.domain.Account;
import com.microbank.bankingservice.domain.Transaction;
import com.microbank.bankingservice.domain.TransactionStatus;
import com.microbank.bankingservice.domain.TransactionType;
import com.microbank.bankingservice.dto.TransactionRequest;
import com.microbank.bankingservice.dto.TransactionResponse;
import com.microbank.bankingservice.repository.AccountRepository;
import com.microbank.bankingservice.repository.TransactionRepository;
import com.microbank.bankingservice.service.TransactionService;
import com.microbank.bankingservice.service.ClientValidationService;
import com.microbank.bankingservice.exception.ClientAccessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final ClientValidationService clientValidationService;

    @Autowired
    public TransactionServiceImpl(TransactionRepository transactionRepository, AccountRepository accountRepository, ClientValidationService clientValidationService) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.clientValidationService = clientValidationService;
    }

    @Override
    public TransactionResponse processDeposit(TransactionRequest request) {
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + request.getAccountId()));

        if (account.getStatus() != com.microbank.bankingservice.domain.AccountStatus.ACTIVE) {
            throw new RuntimeException("Account is not active");
        }

        // Validate that client can perform transactions
        clientValidationService.validateClientCanTransact(account.getClientId());

        // Process deposit
        account.deposit(request.getAmount());
        accountRepository.save(account);

        // Create transaction record
        Transaction transaction = new Transaction(
                account.getId(),
                account.getClientId(),
                TransactionType.DEPOSIT,
                request.getAmount(),
                account.getBalance()
        );
        transaction.setDescription(request.getDescription());
        transaction.setReferenceNumber(generateReferenceNumber());

        Transaction savedTransaction = transactionRepository.save(transaction);
        return new TransactionResponse(savedTransaction);
    }

    @Override
    public TransactionResponse processWithdrawal(TransactionRequest request) {
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + request.getAccountId()));

        if (account.getStatus() != com.microbank.bankingservice.domain.AccountStatus.ACTIVE) {
            throw new RuntimeException("Account is not active");
        }

        // Validate that client can perform transactions
        clientValidationService.validateClientCanTransact(account.getClientId());

        if (!account.canWithdraw(request.getAmount())) {
            throw new RuntimeException("Insufficient funds or account inactive");
        }

        // Process withdrawal
        account.withdraw(request.getAmount());
        accountRepository.save(account);

        // Create transaction record
        Transaction transaction = new Transaction(
                account.getId(),
                account.getClientId(),
                TransactionType.WITHDRAWAL,
                request.getAmount(),
                account.getBalance()
        );
        transaction.setDescription(request.getDescription());
        transaction.setReferenceNumber(generateReferenceNumber());

        Transaction savedTransaction = transactionRepository.save(transaction);
        return new TransactionResponse(savedTransaction);
    }

    @Override
    public TransactionResponse processTransfer(TransactionRequest request) {
        // Extract source and destination account IDs from the request
        Long sourceAccountId = request.getAccountId();
        Long destinationAccountId = request.getDestinationAccountId();
        
        if (destinationAccountId == null) {
            throw new RuntimeException("Destination account ID is required for transfers");
        }
        
        if (sourceAccountId.equals(destinationAccountId)) {
            throw new RuntimeException("Cannot transfer to the same account");
        }
        
        Account sourceAccount = accountRepository.findById(sourceAccountId)
                .orElseThrow(() -> new RuntimeException("Source account not found with ID: " + sourceAccountId));
        
        Account destinationAccount = accountRepository.findById(destinationAccountId)
                .orElseThrow(() -> new RuntimeException("Destination account not found with ID: " + destinationAccountId));
        
        if (sourceAccount.getStatus() != com.microbank.bankingservice.domain.AccountStatus.ACTIVE) {
            throw new RuntimeException("Source account is not active");
        }
        
        if (destinationAccount.getStatus() != com.microbank.bankingservice.domain.AccountStatus.ACTIVE) {
            throw new RuntimeException("Destination account is not active");
        }

        // Validate that both clients can perform transactions
        clientValidationService.validateClientCanTransact(sourceAccount.getClientId());
        clientValidationService.validateClientCanTransact(destinationAccount.getClientId());
        
        if (!sourceAccount.canWithdraw(request.getAmount())) {
            throw new RuntimeException("Insufficient funds in source account");
        }
        
        // Process transfer
        sourceAccount.withdraw(request.getAmount());
        destinationAccount.deposit(request.getAmount());
        
        accountRepository.save(sourceAccount);
        accountRepository.save(destinationAccount);
        
        // Create transaction record for source account (debit)
        Transaction sourceTransaction = new Transaction(
                sourceAccount.getId(),
                sourceAccount.getClientId(),
                TransactionType.TRANSFER,
                request.getAmount(),
                sourceAccount.getBalance()
        );
        sourceTransaction.setDescription("Transfer to " + destinationAccount.getAccountNumber() + ": " + request.getDescription());
        sourceTransaction.setReferenceNumber(generateReferenceNumber());
        sourceTransaction.setDestinationAccountId(destinationAccount.getId());
        
        // Create transaction record for destination account (credit)
        Transaction destTransaction = new Transaction(
                destinationAccount.getId(),
                destinationAccount.getClientId(),
                TransactionType.TRANSFER,
                request.getAmount(),
                destinationAccount.getBalance()
        );
        destTransaction.setDescription("Transfer from " + sourceAccount.getAccountNumber() + ": " + request.getDescription());
        destTransaction.setReferenceNumber(generateReferenceNumber());
        destTransaction.setSourceAccountId(sourceAccount.getId());
        
        transactionRepository.save(sourceTransaction);
        transactionRepository.save(destTransaction);
        
        return new TransactionResponse(sourceTransaction);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found with ID: " + transactionId));
        
        // For read-only access, only check if client is blacklisted
        if (clientValidationService.isClientBlacklisted(transaction.getClientId())) {
            throw ClientAccessException.clientBlacklisted(transaction.getClientId());
        }
        
        return new TransactionResponse(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionByReference(String referenceNumber) {
        Transaction transaction = transactionRepository.findByReferenceNumber(referenceNumber)
                .orElseThrow(() -> new RuntimeException("Transaction not found with reference: " + referenceNumber));
        
        // For read-only access, only check if client is blacklisted
        if (clientValidationService.isClientBlacklisted(transaction.getClientId())) {
            throw ClientAccessException.clientBlacklisted(transaction.getClientId());
        }
        
        return new TransactionResponse(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByAccountId(Long accountId) {
        // Get account to validate client can transact
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + accountId));
        
        // For read-only access, only check if client is blacklisted
        if (clientValidationService.isClientBlacklisted(account.getClientId())) {
            throw ClientAccessException.clientBlacklisted(account.getClientId());
        }
        
        List<Transaction> transactions = transactionRepository.findByAccountIdOrderByCreatedAtDesc(accountId);
        return transactions.stream()
                .map(TransactionResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactionsByAccountId(Long accountId, Pageable pageable) {
        // Get account to validate client can transact
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + accountId));
        
        // For read-only access, only check if client is blacklisted
        if (clientValidationService.isClientBlacklisted(account.getClientId())) {
            throw ClientAccessException.clientBlacklisted(account.getClientId());
        }
        
        Page<Transaction> transactions = transactionRepository.findByAccountId(accountId, pageable);
        return transactions.map(TransactionResponse::new);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByClientId(Long clientId) {
        // For read-only access, only check if client is blacklisted
        if (clientValidationService.isClientBlacklisted(clientId)) {
            throw ClientAccessException.clientBlacklisted(clientId);
        }
        
        List<Transaction> transactions = transactionRepository.findByClientIdOrderByCreatedAtDesc(clientId);
        return transactions.stream()
                .map(TransactionResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactionsByClientId(Long clientId, Pageable pageable) {
        // For read-only access, only check if client is blacklisted
        if (clientValidationService.isClientBlacklisted(clientId)) {
            throw ClientAccessException.clientBlacklisted(clientId);
        }
        
        Page<Transaction> transactions = transactionRepository.findByClientId(clientId, pageable);
        return transactions.map(TransactionResponse::new);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactionsByType(TransactionType transactionType, Pageable pageable) {
        Page<Transaction> transactions = transactionRepository.findByTransactionType(transactionType, pageable);
        return transactions.map(TransactionResponse::new);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<Transaction> transactions = transactionRepository.findTransactionsByDateRange(startDate, endDate);
        return transactions.stream()
                .map(TransactionResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByAccountAndDateRange(Long accountId, LocalDateTime startDate, LocalDateTime endDate) {
        // Get account to validate client can transact
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + accountId));
        
        // For read-only access, only check if client is blacklisted
        if (clientValidationService.isClientBlacklisted(account.getClientId())) {
            throw ClientAccessException.clientBlacklisted(account.getClientId());
        }
        
        List<Transaction> transactions = transactionRepository.findTransactionsByAccountAndDateRange(accountId, startDate, endDate);
        return transactions.stream()
                .map(TransactionResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByClientAndDateRange(Long clientId, LocalDateTime startDate, LocalDateTime endDate) {
        // For read-only access, only check if client is blacklisted
        if (clientValidationService.isClientBlacklisted(clientId)) {
            throw ClientAccessException.clientBlacklisted(clientId);
        }
        
        List<Transaction> transactions = transactionRepository.findTransactionsByClientAndDateRange(clientId, startDate, endDate);
        return transactions.stream()
                .map(TransactionResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public TransactionResponse reverseTransaction(Long transactionId, String reason) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found with ID: " + transactionId));

        if (transaction.getStatus() != TransactionStatus.COMPLETED) {
            throw new RuntimeException("Transaction cannot be reversed. Status: " + transaction.getStatus());
        }

        // Validate that client can perform transactions before reversing
        clientValidationService.validateClientCanTransact(transaction.getClientId());

        // Create reversal transaction
        Transaction reversalTransaction = new Transaction(
                transaction.getAccountId(),
                transaction.getClientId(),
                TransactionType.REFUND,
                transaction.getAmount(),
                null // Will be calculated when processing
        );
        reversalTransaction.setDescription("Reversal of transaction " + transaction.getReferenceNumber() + ". Reason: " + reason);
        reversalTransaction.setReferenceNumber(generateReferenceNumber());
        reversalTransaction.setStatus(TransactionStatus.PENDING);

        Transaction savedReversal = transactionRepository.save(reversalTransaction);
        return new TransactionResponse(savedReversal);
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalTransactions() {
        return transactionRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long getTransactionsCountByType(TransactionType transactionType) {
        return transactionRepository.countByTransactionType(transactionType);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalTransactionAmountByType(TransactionType transactionType) {
        // This would require a custom query in the repository
        // For now, return 0
        return BigDecimal.ZERO;
    }

    @Override
    public String generateReferenceNumber() {
        // Simple reference number generation - in production, use a more sophisticated approach
        long timestamp = System.currentTimeMillis();
        String referenceNumber = "TXN" + String.format("%012d", timestamp);
        
        // Ensure uniqueness
        while (transactionRepository.existsByReferenceNumber(referenceNumber)) {
            timestamp = System.currentTimeMillis();
            referenceNumber = "TXN" + String.format("%012d", timestamp);
        }
        
        return referenceNumber;
    }
}
