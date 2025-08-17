package com.microbank.apigateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class DestinationLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(DestinationLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Log before processing
        String originalPath = exchange.getRequest().getPath().value();
        String originalUri = exchange.getRequest().getURI().toString();
        
        logger.info("=== BEFORE ROUTING ===");
        logger.info("Original Path: {}", originalPath);
        logger.info("Original URI: {}", originalUri);
        
        return chain.filter(exchange)
            .then(Mono.fromRunnable(() -> {
                // Log after processing
                String finalUri = exchange.getAttribute("GATEWAY_REQUEST_URL_ATTR");
                String routeId = exchange.getAttribute("GATEWAY_ROUTE_ID_ATTR");
                
                // Try alternative attribute names
                if (finalUri == null) {
                    finalUri = exchange.getAttribute("GATEWAY_ROUTE_URI");
                }
                if (routeId == null) {
                    routeId = exchange.getAttribute("GATEWAY_ROUTE_ID");
                }
                
                logger.info("=== AFTER ROUTING ===");
                logger.info("Route ID: {}", routeId);
                logger.info("Final Destination URI: {}", finalUri);
                logger.info("Response Status: {}", exchange.getResponse().getStatusCode());
                logger.info("All Attributes: {}", exchange.getAttributes());
                logger.info("=====================");
            }));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
