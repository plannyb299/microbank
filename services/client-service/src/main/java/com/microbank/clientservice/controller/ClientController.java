package com.microbank.clientservice.controller;

import com.microbank.clientservice.dto.*;
import com.microbank.clientservice.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Client Management", description = "APIs for managing client accounts")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping("/clients/register")
    @Operation(summary = "Register a new client")
    public ResponseEntity<ClientResponse> registerClient(@Valid @RequestBody ClientRegistrationRequest request) {
        ClientResponse response = clientService.registerClient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/clients/login")
    @Operation(summary = "Authenticate client and get JWT token")
    public ResponseEntity<LoginResponse> loginClient(@Valid @RequestBody ClientLoginRequest request) {
        LoginResponse response = clientService.loginClient(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/clients/profile")
    @Operation(summary = "Get current client profile")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ClientResponse> getCurrentProfile() {
        // This would be implemented to get the current authenticated user's profile
        // For now, returning a placeholder
        return ResponseEntity.ok().build();
    }

    @GetMapping("/clients/{clientId}")
    @Operation(summary = "Get client profile by ID")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClientResponse> getClientProfile(@PathVariable Long clientId) {
        ClientResponse response = clientService.getClientProfile(clientId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/clients")
    @Operation(summary = "Get all clients with pagination")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ClientResponse>> getAllClients(Pageable pageable) {
        Page<ClientResponse> clients = clientService.getAllClients(pageable);
        return ResponseEntity.ok(clients);
    }

    @GetMapping("/clients/search")
    @Operation(summary = "Search clients by name or email")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ClientResponse>> searchClients(
            @RequestParam String searchTerm, Pageable pageable) {
        Page<ClientResponse> clients = clientService.searchClients(searchTerm, pageable);
        return ResponseEntity.ok(clients);
    }

    @PutMapping("/clients/{clientId}/blacklist")
    @Operation(summary = "Blacklist a client")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClientResponse> blacklistClient(
            @PathVariable Long clientId, @Valid @RequestBody BlacklistRequest request) {
        ClientResponse response = clientService.blacklistClient(clientId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/clients/{clientId}/blacklist")
    @Operation(summary = "Remove client from blacklist")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClientResponse> removeFromBlacklist(@PathVariable Long clientId) {
        ClientResponse response = clientService.removeFromBlacklist(clientId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/clients/blacklisted")
    @Operation(summary = "Get all blacklisted clients")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ClientResponse>> getBlacklistedClients() {
        List<ClientResponse> clients = clientService.getBlacklistedClients();
        return ResponseEntity.ok(clients);
    }

    @GetMapping("/clients/{clientId}/can-transact")
    @Operation(summary = "Check if client can perform transactions")
    public ResponseEntity<Boolean> canClientTransact(@PathVariable Long clientId) {
        boolean canTransact = clientService.canClientTransact(clientId);
        return ResponseEntity.ok(canTransact);
    }

    @GetMapping("/clients/{clientId}/blacklist-status")
    @Operation(summary = "Get client blacklist status")
    public ResponseEntity<Map<String, Object>> getClientBlacklistStatus(@PathVariable Long clientId) {
        ClientResponse client = clientService.getClientProfile(clientId);
        Map<String, Object> status = Map.of(
            "blacklisted", client.isBlacklisted(),
            "blacklistReason", client.getBlacklistReason(),
            "status", client.getStatus()
        );
        return ResponseEntity.ok(status);
    }

    @GetMapping("/clients/{clientId}/status")
    @Operation(summary = "Get client status")
    public ResponseEntity<Map<String, Object>> getClientStatus(@PathVariable Long clientId) {
        ClientResponse client = clientService.getClientProfile(clientId);
        Map<String, Object> status = Map.of(
            "status", client.getStatus(),
            "blacklisted", client.isBlacklisted()
        );
        return ResponseEntity.ok(status);
    }

    // Admin endpoints
    @GetMapping("/admin/clients")
    @Operation(summary = "Get all clients for admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ClientResponse>> getAllClientsForAdmin() {
        List<ClientResponse> clients = clientService.getAllClientsForAdmin();
        return ResponseEntity.ok(clients);
    }

    @GetMapping("/admin/clients/{clientId}")
    @Operation(summary = "Get client by ID for admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClientResponse> getClientByIdForAdmin(@PathVariable Long clientId) {
        ClientResponse response = clientService.getClientByIdForAdmin(clientId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/clients/{clientId}/blacklist")
    @Operation(summary = "Blacklist a client (admin)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClientResponse> adminBlacklistClient(
            @PathVariable Long clientId, @Valid @RequestBody BlacklistRequest request) {
        ClientResponse response = clientService.blacklistClient(clientId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/clients/{clientId}/unblacklist")
    @Operation(summary = "Unblacklist a client (admin)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClientResponse> adminUnblacklistClient(@PathVariable Long clientId) {
        ClientResponse response = clientService.removeFromBlacklist(clientId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/clients/{clientId}/promote")
    @Operation(summary = "Promote client to admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClientResponse> promoteToAdmin(@PathVariable Long clientId) {
        ClientResponse response = clientService.promoteToAdmin(clientId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/clients/setup/first-admin")
    @Operation(summary = "Create first admin (only works if no admin exists)")
    public ResponseEntity<ClientResponse> createFirstAdmin(@Valid @RequestBody ClientRegistrationRequest request) {
        ClientResponse response = clientService.createFirstAdmin(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/admin/clients/{clientId}/status")
    @Operation(summary = "Update client status (admin)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClientResponse> updateClientStatus(
            @PathVariable Long clientId, @RequestBody UpdateStatusRequest request) {
        ClientResponse response = clientService.updateClientStatus(clientId, request.getStatus());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/statistics/clients")
    @Operation(summary = "Get client statistics for admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClientStatistics> getClientStatistics() {
        ClientStatistics stats = clientService.getClientStatistics();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/admin/clients/search")
    @Operation(summary = "Search clients for admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ClientResponse>> adminSearchClients(@RequestParam String q) {
        List<ClientResponse> clients = clientService.searchClientsForAdmin(q);
        return ResponseEntity.ok(clients);
    }



    @PutMapping("/clients/{clientId}")
    @Operation(summary = "Update client profile")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClientResponse> updateClientProfile(
            @PathVariable Long clientId, @Valid @RequestBody ClientRegistrationRequest request) {
        ClientResponse response = clientService.updateClientProfile(clientId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/clients/{clientId}")
    @Operation(summary = "Delete client")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteClient(@PathVariable Long clientId) {
        clientService.deleteClient(clientId);
        return ResponseEntity.noContent().build();
    }


}
