package com.microbank.bankingservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);
            
            // Debug logging
            logger.debug("JWT Filter - Request URI: " + request.getRequestURI());
            logger.debug("JWT Filter - Authorization header: " + request.getHeader("Authorization"));
            logger.debug("JWT Filter - Extracted JWT: " + (jwt != null ? jwt.substring(0, Math.min(jwt.length(), 20)) + "..." : "null"));

            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
                logger.debug("JWT Filter - Token validated successfully");
                String email = jwtTokenProvider.getEmailFromToken(jwt);
                logger.debug("JWT Filter - Email from token: " + email);
                
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                logger.debug("JWT Filter - User details loaded: " + userDetails.getUsername() + " with authorities: " + userDetails.getAuthorities());
                
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.debug("JWT Filter - Authentication set in SecurityContext");
            } else {
                logger.debug("JWT Filter - Token validation failed or no token");
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
        } finally {
            // Clean up ThreadLocal after request processing
            jwtTokenProvider.clearCurrentUserRole();
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
