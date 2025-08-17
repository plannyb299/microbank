package com.microbank.clientservice.service;

import com.microbank.clientservice.domain.Client;
import com.microbank.clientservice.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ClientService {

    /**
     * Register a new client
     */
    ClientResponse registerClient(ClientRegistrationRequest request);

    /**
     * Authenticate client and return JWT token
     */
    LoginResponse loginClient(ClientLoginRequest request);

    /**
     * Get client profile by ID
     */
    ClientResponse getClientProfile(Long clientId);

    /**
     * Get client profile by email
     */
    ClientResponse getClientProfileByEmail(String email);

    /**
     * Get all clients with pagination
     */
    Page<ClientResponse> getAllClients(Pageable pageable);

    /**
     * Search clients by name or email
     */
    Page<ClientResponse> searchClients(String searchTerm, Pageable pageable);

    /**
     * Blacklist a client
     */
    ClientResponse blacklistClient(Long clientId, BlacklistRequest request);

    /**
     * Remove client from blacklist
     */
    ClientResponse removeFromBlacklist(Long clientId);

    /**
     * Get all blacklisted clients
     */
    List<ClientResponse> getBlacklistedClients();

    /**
     * Check if client can transact
     */
    boolean canClientTransact(Long clientId);

    /**
     * Check if client can transact by email
     */
    boolean canClientTransactByEmail(String email);

    /**
     * Update client profile
     */
    ClientResponse updateClientProfile(Long clientId, ClientRegistrationRequest request);

    /**
     * Delete client
     */
    void deleteClient(Long clientId);

    /**
     * Get client statistics
     */
    ClientStatistics getClientStatistics();

    // Admin methods
    /**
     * Get all clients for admin (without pagination)
     */
    List<ClientResponse> getAllClientsForAdmin();

    /**
     * Get client by ID for admin
     */
    ClientResponse getClientByIdForAdmin(Long clientId);

    /**
     * Update client status
     */
    ClientResponse updateClientStatus(Long clientId, String status);

    /**
     * Search clients for admin (without pagination)
     */
    List<ClientResponse> searchClientsForAdmin(String query);
    
    ClientResponse promoteToAdmin(Long clientId);
    
    ClientResponse createFirstAdmin(ClientRegistrationRequest request);
}
