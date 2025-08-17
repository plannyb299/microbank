package com.microbank.apigateway.config;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // Only add CORS headers if they don't already exist
        if (!exchange.getResponse().getHeaders().containsKey("Access-Control-Allow-Origin")) {
            exchange.getResponse().getHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponse().getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            exchange.getResponse().getHeaders().add("Access-Control-Allow-Headers", "*");
            exchange.getResponse().getHeaders().add("Access-Control-Max-Age", "3600");
        }
        
        // Handle preflight OPTIONS request
        if (exchange.getRequest().getMethod().name().equals("OPTIONS")) {
            exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.OK);
            return Mono.empty();
        }
        
        return chain.filter(exchange);
    }
}

