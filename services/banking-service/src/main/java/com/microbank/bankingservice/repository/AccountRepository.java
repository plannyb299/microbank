package com.microbank.bankingservice.repository;

import com.microbank.bankingservice.domain.Account;
import com.microbank.bankingservice.domain.AccountStatus;
import com.microbank.bankingservice.domain.AccountType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    /**
     * Find account by account number
     */
    Optional<Account> findByAccountNumber(String accountNumber);

    /**
     * Find accounts by client ID
     */
    List<Account> findByClientId(Long clientId);

    /**
     * Find accounts by client ID with pagination
     */
    Page<Account> findByClientId(Long clientId, Pageable pageable);

    /**
     * Find accounts by client ID and status
     */
    List<Account> findByClientIdAndStatus(Long clientId, AccountStatus status);

    /**
     * Find accounts by status
     */
    Page<Account> findByStatus(AccountStatus status, Pageable pageable);

    /**
     * Find accounts by account type
     */
    Page<Account> findByAccountType(AccountType accountType, Pageable pageable);

    /**
     * Find active accounts by client ID
     */
    List<Account> findByClientIdAndStatusOrderByCreatedAtDesc(Long clientId, AccountStatus status);

    /**
     * Check if account exists by account number
     */
    boolean existsByAccountNumber(String accountNumber);

    /**
     * Count accounts by client ID
     */
    long countByClientId(Long clientId);

    /**
     * Count accounts by status
     */
    long countByStatus(AccountStatus status);

    /**
     * Count accounts by account type
     */
    long countByAccountType(AccountType accountType);

    /**
     * Find accounts with balance greater than specified amount
     */
    @Query("SELECT a FROM Account a WHERE a.balance > :minBalance AND a.status = :status")
    List<Account> findAccountsWithBalanceGreaterThan(@Param("minBalance") java.math.BigDecimal minBalance,
                                                    @Param("status") AccountStatus status);

    /**
     * Find accounts created in the last N days
     */
    @Query("SELECT a FROM Account a WHERE a.createdAt >= :startDate")
    List<Account> findAccountsCreatedAfter(@Param("startDate") java.time.LocalDateTime startDate);
}
