package com.microbank.clientservice.service;

import com.microbank.clientservice.domain.Client;
import com.microbank.clientservice.domain.ClientStatus;
import com.microbank.clientservice.domain.ClientRole;
import com.microbank.clientservice.dto.*;
import com.microbank.clientservice.exception.AuthenticationException;
import com.microbank.clientservice.repository.ClientRepository;
import com.microbank.clientservice.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public ClientServiceImpl(ClientRepository clientRepository, 
                           PasswordEncoder passwordEncoder, 
                           JwtTokenProvider jwtTokenProvider) {
        this.clientRepository = clientRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public ClientResponse registerClient(ClientRegistrationRequest request) {
        if (clientRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Client with email " + request.getEmail() + " already exists");
        }

        Client client = new Client();
        client.setEmail(request.getEmail());
        client.setName(request.getName());
        client.setPassword(passwordEncoder.encode(request.getPassword()));
        client.setStatus(ClientStatus.ACTIVE);
        client.setBlacklisted(false);

        Client savedClient = clientRepository.save(client);
        return new ClientResponse(savedClient);
    }

    @Override
    public LoginResponse loginClient(ClientLoginRequest request) {
        Client client = clientRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthenticationException("Invalid email or password"));

        // Allow both ACTIVE and BLACKLISTED clients to log in
        // Blacklisted clients will be restricted from transactions by the banking service
        if (client.getStatus() != ClientStatus.ACTIVE && client.getStatus() != ClientStatus.BLACKLISTED) {
            throw new AuthenticationException("Account is not active. Status: " + client.getStatus());
        }

        if (!passwordEncoder.matches(request.getPassword(), client.getPassword())) {
            throw new AuthenticationException("Invalid email or password");
        }

        // Log successful login for debugging
        System.out.println("Client login successful: " + client.getEmail() + 
                          ", Status: " + client.getStatus() + 
                          ", Blacklisted: " + client.isBlacklisted());

        String token = jwtTokenProvider.generateToken(client.getEmail(), client.getRole().name());
        long expiresIn = jwtTokenProvider.getExpirationTime();

        return new LoginResponse(token, new ClientResponse(client), expiresIn);
    }

    @Override
    @Transactional(readOnly = true)
    public ClientResponse getClientProfile(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found with ID: " + clientId));
        return new ClientResponse(client);
    }

    @Override
    @Transactional(readOnly = true)
    public ClientResponse getClientProfileByEmail(String email) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Client not found with email: " + email));
        return new ClientResponse(client);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClientResponse> getAllClients(Pageable pageable) {
        Page<Client> clients = clientRepository.findAll(pageable);
        return clients.map(ClientResponse::new);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClientResponse> searchClients(String searchTerm, Pageable pageable) {
        Page<Client> clients = clientRepository.searchByNameOrEmail(searchTerm, pageable);
        return clients.map(ClientResponse::new);
    }

    @Override
    public ClientResponse blacklistClient(Long clientId, BlacklistRequest request) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found with ID: " + clientId));

        client.blacklist(request.getReason());
        Client savedClient = clientRepository.save(client);
        return new ClientResponse(savedClient);
    }

    @Override
    public ClientResponse removeFromBlacklist(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found with ID: " + clientId));

        client.removeFromBlacklist();
        Client savedClient = clientRepository.save(client);
        return new ClientResponse(savedClient);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientResponse> getBlacklistedClients() {
        List<Client> blacklistedClients = clientRepository.findByBlacklistedTrue();
        return blacklistedClients.stream()
                .map(ClientResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canClientTransact(Long clientId) {
        return clientRepository.findById(clientId)
                .map(Client::canTransact)
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canClientTransactByEmail(String email) {
        return clientRepository.findByEmail(email)
                .map(Client::canTransact)
                .orElse(false);
    }

    @Override
    public ClientResponse updateClientProfile(Long clientId, ClientRegistrationRequest request) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found with ID: " + clientId));

        if (!client.getEmail().equals(request.getEmail()) && 
            clientRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email " + request.getEmail() + " is already taken");
        }

        client.setEmail(request.getEmail());
        client.setName(request.getName());
        
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            client.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        Client savedClient = clientRepository.save(client);
        return new ClientResponse(savedClient);
    }

    @Override
    public void deleteClient(Long clientId) {
        if (!clientRepository.existsById(clientId)) {
            throw new RuntimeException("Client not found with ID: " + clientId);
        }
        clientRepository.deleteById(clientId);
    }

    @Override
    @Transactional(readOnly = true)
    public ClientStatistics getClientStatistics() {
        long totalClients = clientRepository.count();
        long activeClients = clientRepository.countByStatus(ClientStatus.ACTIVE);
        long blacklistedClients = clientRepository.countByBlacklistedTrue();
        long inactiveClients = clientRepository.countByStatus(ClientStatus.INACTIVE);
        long suspendedClients = clientRepository.countByStatus(ClientStatus.SUSPENDED);

        return new ClientStatistics(totalClients, activeClients, blacklistedClients, 
                                 inactiveClients, suspendedClients);
    }

    // Admin methods implementation
    @Override
    @Transactional(readOnly = true)
    public List<ClientResponse> getAllClientsForAdmin() {
        List<Client> clients = clientRepository.findAll();
        return clients.stream()
                .map(ClientResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ClientResponse getClientByIdForAdmin(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found with ID: " + clientId));
        return new ClientResponse(client);
    }

    @Override
    @Transactional
    public ClientResponse updateClientStatus(Long clientId, String status) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found with ID: " + clientId));
        
        try {
            ClientStatus newStatus = ClientStatus.valueOf(status.toUpperCase());
            client.setStatus(newStatus);
            Client savedClient = clientRepository.save(client);
            return new ClientResponse(savedClient);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status: " + status);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientResponse> searchClientsForAdmin(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllClientsForAdmin();
        }
        
        String searchTerm = query.trim().toLowerCase();
        List<Client> clients = clientRepository.findAll().stream()
                .filter(client -> 
                    client.getName().toLowerCase().contains(searchTerm) ||
                    client.getEmail().toLowerCase().contains(searchTerm))
                .collect(Collectors.toList());
        
        return clients.stream()
                .map(ClientResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ClientResponse promoteToAdmin(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        
        client.setRole(ClientRole.ADMIN);
        Client savedClient = clientRepository.save(client);
        
        return new ClientResponse(savedClient);
    }

    @Override
    @Transactional
    public ClientResponse createFirstAdmin(ClientRegistrationRequest request) {
        // Check if any admin already exists
        boolean adminExists = clientRepository.findAll().stream()
                .anyMatch(client -> client.getRole() == ClientRole.ADMIN);
        
        if (adminExists) {
            throw new RuntimeException("Admin already exists. Cannot create another admin through this endpoint.");
        }
        
        if (clientRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Client with email " + request.getEmail() + " already exists");
        }

        Client client = new Client();
        client.setEmail(request.getEmail());
        client.setName(request.getName());
        client.setPassword(passwordEncoder.encode(request.getPassword()));
        client.setStatus(ClientStatus.ACTIVE);
        client.setBlacklisted(false);
        client.setRole(ClientRole.ADMIN); // Set as admin

        Client savedClient = clientRepository.save(client);
        return new ClientResponse(savedClient);
    }
}
