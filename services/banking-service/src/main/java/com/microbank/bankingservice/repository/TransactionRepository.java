package com.microbank.bankingservice.repository;

import com.microbank.bankingservice.domain.Transaction;
import com.microbank.bankingservice.domain.TransactionStatus;
import com.microbank.bankingservice.domain.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Find transaction by reference number
     */
    Optional<Transaction> findByReferenceNumber(String referenceNumber);

    /**
     * Find transactions by account ID
     */
    List<Transaction> findByAccountIdOrderByCreatedAtDesc(Long accountId);

    /**
     * Find transactions by client ID
     */
    List<Transaction> findByClientIdOrderByCreatedAtDesc(Long clientId);

    /**
     * Find transactions by account ID with pagination
     */
    Page<Transaction> findByAccountId(Long accountId, Pageable pageable);

    /**
     * Find transactions by client ID with pagination
     */
    Page<Transaction> findByClientId(Long clientId, Pageable pageable);

    /**
     * Find transactions by type
     */
    Page<Transaction> findByTransactionType(TransactionType transactionType, Pageable pageable);

    /**
     * Find transactions by status
     */
    Page<Transaction> findByStatus(TransactionStatus status, Pageable pageable);

    /**
     * Find transactions by account ID and type
     */
    List<Transaction> findByAccountIdAndTransactionTypeOrderByCreatedAtDesc(Long accountId, TransactionType transactionType);

    /**
     * Find transactions by client ID and type
     */
    List<Transaction> findByClientIdAndTransactionTypeOrderByCreatedAtDesc(Long clientId, TransactionType transactionType);

    /**
     * Find transactions by date range
     */
    @Query("SELECT t FROM Transaction t WHERE t.createdAt BETWEEN :startDate AND :endDate")
    List<Transaction> findTransactionsByDateRange(@Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);

    /**
     * Find transactions by account ID and date range
     */
    @Query("SELECT t FROM Transaction t WHERE t.accountId = :accountId AND t.createdAt BETWEEN :startDate AND :endDate")
    List<Transaction> findTransactionsByAccountAndDateRange(@Param("accountId") Long accountId,
                                                           @Param("startDate") LocalDateTime startDate,
                                                           @Param("endDate") LocalDateTime endDate);

    /**
     * Find transactions by client ID and date range
     */
    @Query("SELECT t FROM Transaction t WHERE t.clientId = :clientId AND t.createdAt BETWEEN :startDate AND :endDate")
    List<Transaction> findTransactionsByClientAndDateRange(@Param("clientId") Long clientId,
                                                          @Param("startDate") LocalDateTime startDate,
                                                          @Param("endDate") LocalDateTime endDate);

    /**
     * Count transactions by account ID
     */
    long countByAccountId(Long accountId);

    /**
     * Count transactions by client ID
     */
    long countByClientId(Long clientId);

    /**
     * Count transactions by type
     */
    long countByTransactionType(TransactionType transactionType);

    /**
     * Count transactions by status
     */
    long countByStatus(TransactionStatus status);

    /**
     * Find failed transactions
     */
    List<Transaction> findByStatusOrderByCreatedAtDesc(TransactionStatus status);

    /**
     * Check if reference number exists
     */
    boolean existsByReferenceNumber(String referenceNumber);
}
