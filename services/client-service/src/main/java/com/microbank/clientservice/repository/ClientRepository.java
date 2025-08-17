package com.microbank.clientservice.repository;

import com.microbank.clientservice.domain.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    /**
     * Find client by email
     */
    Optional<Client> findByEmail(String email);

    /**
     * Check if client exists by email
     */
    boolean existsByEmail(String email);

    /**
     * Find all blacklisted clients
     */
    List<Client> findByBlacklistedTrue();

    /**
     * Find all active clients
     */
    List<Client> findByStatusAndBlacklistedFalse(com.microbank.clientservice.domain.ClientStatus status);

    /**
     * Find clients by status
     */
    Page<Client> findByStatus(com.microbank.clientservice.domain.ClientStatus status, Pageable pageable);

    /**
     * Find clients by blacklist status
     */
    Page<Client> findByBlacklisted(boolean blacklisted, Pageable pageable);

    /**
     * Search clients by name or email (case-insensitive)
     */
    @Query("SELECT c FROM Client c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Client> searchByNameOrEmail(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Count clients by status
     */
    long countByStatus(com.microbank.clientservice.domain.ClientStatus status);

    /**
     * Count blacklisted clients
     */
    long countByBlacklistedTrue();

    /**
     * Find clients created in the last N days
     */
    @Query("SELECT c FROM Client c WHERE c.createdAt >= :startDate")
    List<Client> findClientsCreatedAfter(@Param("startDate") java.time.LocalDateTime startDate);
}
