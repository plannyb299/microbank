package com.microbank.bankingservice.service.impl;

import com.microbank.bankingservice.service.ClientValidationService;
import com.microbank.bankingservice.exception.ClientAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.util.Map;

@Service
public class ClientValidationServiceImpl implements ClientValidationService {
    
    private static final Logger logger = LoggerFactory.getLogger(ClientValidationServiceImpl.class);
    
    private final RestTemplate restTemplate;
    
    @Value("${client.service.url}")
    private String clientServiceUrl;
    
    public ClientValidationServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    @Override
    public void validateClientCanTransact(Long clientId) {
        try {
            // Call client service to check if client can transact
            String url = clientServiceUrl + "/api/v1/clients/" + clientId + "/can-transact";
            ResponseEntity<Boolean> response = restTemplate.getForEntity(url, Boolean.class);
            
            if (response.getStatusCode() == HttpStatus.OK && Boolean.TRUE.equals(response.getBody())) {
                // Client can transact
                return;
            } else {
                throw ClientAccessException.clientCannotTransact(clientId);
            }
            
        } catch (HttpClientErrorException.Forbidden e) {
            // Client service returned 403 - this could be authentication failure or actual restriction
            logger.warn("Client {} access forbidden (403): {}", clientId, e.getMessage());
            // For now, we'll assume this is an authentication issue
            // In production, you should implement proper service-to-service authentication
            throw ClientAccessException.unauthorizedAccess(clientId);
            
        } catch (HttpClientErrorException.NotFound e) {
            // Client not found
            logger.warn("Client {} not found: {}", clientId, e.getMessage());
            throw ClientAccessException.clientNotFound(clientId);
            
        } catch (HttpClientErrorException.Unauthorized e) {
            // Authentication/authorization issue
            logger.error("Unauthorized access to client service: {}", e.getMessage());
            throw ClientAccessException.unauthorizedAccess(clientId);
            
        } catch (HttpServerErrorException e) {
            // Client service internal error
            logger.error("Client service error for client {}: {}", clientId, e.getMessage());
            throw ClientAccessException.serviceUnavailable(clientId);
            
        } catch (ResourceAccessException e) {
            // Connection/timeout issues
            logger.error("Failed to connect to client service: {}", e.getMessage());
            throw ClientAccessException.serviceUnavailable(clientId);
            
        } catch (Exception e) {
            // Unexpected errors
            logger.error("Unexpected error validating client {} transaction permission: {}", clientId, e.getMessage());
            throw ClientAccessException.serviceUnavailable(clientId);
        }
    }
    
    @Override
    public boolean isClientBlacklisted(Long clientId) {
        try {
            // Call client service to check blacklist status
            String url = clientServiceUrl + "/api/v1/clients/" + clientId + "/blacklist-status";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return Boolean.TRUE.equals(response.getBody().get("blacklisted"));
            }
            
            return false;
            
        } catch (HttpClientErrorException.Forbidden e) {
            // Client service returned 403 - this could be authentication failure or actual blacklist
            // We need to distinguish between these cases
            logger.warn("Client {} access forbidden (403): {}", clientId, e.getMessage());
            // For now, we'll assume this is an authentication issue and return false
            // In production, you should implement proper service-to-service authentication
            return false;
            
        } catch (HttpClientErrorException.NotFound e) {
            // Client not found
            logger.warn("Client {} not found: {}", clientId, e.getMessage());
            return false;
            
        } catch (HttpClientErrorException.Unauthorized e) {
            // Authentication/authorization issue
            logger.error("Unauthorized access to client service: {}", e.getMessage());
            return false;
            
        } catch (HttpServerErrorException e) {
            // Client service internal error
            logger.error("Client service error for client {}: {}", clientId, e.getMessage());
            return false;
            
        } catch (ResourceAccessException e) {
            // Connection/timeout issues
            logger.error("Failed to connect to client service: {}", e.getMessage());
            return false;
            
        } catch (Exception e) {
            // Unexpected errors
            logger.error("Unexpected error checking blacklist status for client {}: {}", clientId, e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean isClientActive(Long clientId) {
        try {
            // Call client service to check active status
            String url = clientServiceUrl + "/api/v1/clients/" + clientId + "/status";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String status = (String) response.getBody().get("status");
                return "ACTIVE".equals(status);
            }
            
            return false;
            
        } catch (HttpClientErrorException.Forbidden e) {
            // Client service returned 403 - this could be authentication failure or actual restriction
            logger.warn("Client {} access forbidden (403): {}", clientId, e.getMessage());
            // For now, we'll assume this is an authentication issue and return false
            // In production, you should implement proper service-to-service authentication
            return false;
            
        } catch (HttpClientErrorException.NotFound e) {
            // Client not found
            logger.warn("Client {} not found: {}", clientId, e.getMessage());
            return false;
            
        } catch (HttpClientErrorException.Unauthorized e) {
            // Authentication/authorization issue
            logger.error("Unauthorized access to client service: {}", e.getMessage());
            return false;
            
        } catch (HttpServerErrorException e) {
            // Client service internal error
            logger.error("Client service error for client {}: {}", clientId, e.getMessage());
            return false;
            
        } catch (ResourceAccessException e) {
            // Connection/timeout issues
            logger.error("Failed to connect to client service: {}", e.getMessage());
            return false;
            
        } catch (Exception e) {
            // Unexpected errors
            logger.error("Unexpected error checking active status for client {}: {}", clientId, e.getMessage());
            return false;
        }
    }
}
