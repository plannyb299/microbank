package com.microbank.bankingservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${jwt.secret:defaultSecretKey}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;

    // ThreadLocal to store current user role
    private static final ThreadLocal<String> currentUserRole = new ThreadLocal<>();

    private SecretKey getSigningKey() {
        // Use HS512 algorithm to match the client service
        byte[] keyBytes = jwtSecret.getBytes();
        if (keyBytes.length < 64) {
            // Pad with zeros to reach 64 bytes (512 bits) for HS512
            byte[] paddedKey = new byte[64];
            System.arraycopy(keyBytes, 0, paddedKey, 0, Math.min(keyBytes.length, 64));
            return new SecretKeySpec(paddedKey, "HmacSHA512");
        } else {
            // Truncate to 64 bytes if longer
            byte[] truncatedKey = new byte[64];
            System.arraycopy(keyBytes, 0, truncatedKey, 0, 64);
            return new SecretKeySpec(truncatedKey, "HmacSHA512");
        }
    }

    public boolean validateToken(String token) {
        try {
            logger.debug("JWT Provider - Validating token: " + token.substring(0, Math.min(token.length(), 20)) + "...");
            
            Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
            
            logger.debug("JWT Provider - Token parsed successfully, claims: " + claims);
            
            // Extract role from claims and store in ThreadLocal
            String role = claims.get("role", String.class);
            logger.debug("JWT Provider - Role from claims: " + role);
            
            if (role != null) {
                currentUserRole.set(role);
                logger.debug("JWT Provider - Role stored in ThreadLocal: " + role);
            }
            
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            logger.error("JWT Provider - Token validation failed: " + e.getMessage(), e);
            return false;
        }
    }

    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
        return claims.getSubject();
    }

    public String getCurrentUserRole() {
        return currentUserRole.get();
    }

    public void clearCurrentUserRole() {
        currentUserRole.remove();
    }

    public long getExpirationTime() {
        return jwtExpiration;
    }
}
