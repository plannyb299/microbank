package com.microbank.bankingservice.security;

import com.microbank.bankingservice.security.JwtTokenProvider;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final JwtTokenProvider jwtTokenProvider;

    public CustomUserDetailsService(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Extract role from JWT token context
        String role = jwtTokenProvider.getCurrentUserRole();
        
        // If no role found in token, default to USER
        if (role == null || role.isEmpty()) {
            role = "USER";
        }
        
        return new User(
            email,
            "N/A", // No password needed for JWT authentication
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
        );
    }
}
