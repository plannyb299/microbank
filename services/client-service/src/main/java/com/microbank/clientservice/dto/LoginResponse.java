package com.microbank.clientservice.dto;

public class LoginResponse {

    private String token;
    private String tokenType = "Bearer";
    private ClientResponse client;
    private long expiresIn;

    // Default constructor
    public LoginResponse() {}

    // Constructor with all fields
    public LoginResponse(String token, ClientResponse client, long expiresIn) {
        this.token = token;
        this.client = client;
        this.expiresIn = expiresIn;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public ClientResponse getClient() {
        return client;
    }

    public void setClient(ClientResponse client) {
        this.client = client;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    @Override
    public String toString() {
        return "LoginResponse{" +
                "tokenType='" + tokenType + '\'' +
                ", client=" + client +
                ", expiresIn=" + expiresIn +
                '}';
    }
}
