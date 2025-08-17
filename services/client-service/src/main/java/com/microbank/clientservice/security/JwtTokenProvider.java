package com.microbank.clientservice.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret:defaultSecretKey}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;

    private SecretKey getSigningKey() {
        // Ensure the secret is properly sized for HS512 (minimum 512 bits = 64 bytes)
        // If the secret is too short, pad it; if too long, truncate it
        byte[] keyBytes = jwtSecret.getBytes();
        if (keyBytes.length < 64) {
            // Pad with zeros to reach 64 bytes (512 bits)
            byte[] paddedKey = new byte[64];
            System.arraycopy(keyBytes, 0, paddedKey, 0, Math.min(keyBytes.length, 64));
            return Keys.hmacShaKeyFor(paddedKey);
        } else {
            // Truncate to 64 bytes if longer
            byte[] truncatedKey = new byte[64];
            System.arraycopy(keyBytes, 0, truncatedKey, 0, 64);
            return Keys.hmacShaKeyFor(truncatedKey);
        }
    }

    public String generateToken(String email, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .setSubject(email)
                .claim("role", role) // Include role in JWT claims
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    // Keep the old method for backward compatibility
    public String generateToken(String email) {
        return generateToken(email, "CLIENT"); // Default to CLIENT role
    }

    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(getSigningKey())
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public long getExpirationTime() {
        return jwtExpiration;
    }
}
