package com.microbank.clientservice.security;

import com.microbank.clientservice.domain.Client;
import com.microbank.clientservice.repository.ClientRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final ClientRepository clientRepository;

    public CustomUserDetailsService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Client not found with email: " + email));

        return User.builder()
                .username(client.getEmail())
                .password(client.getPassword())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + client.getRole().name())))
                .accountExpired(false)
                .accountLocked(false) // Allow blacklisted clients to log in
                .credentialsExpired(false)
                .disabled(client.getStatus() != com.microbank.clientservice.domain.ClientStatus.ACTIVE)
                .build();
    }
}
