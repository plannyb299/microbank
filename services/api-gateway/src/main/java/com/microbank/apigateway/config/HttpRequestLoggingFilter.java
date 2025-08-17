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
public class HttpRequestLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(HttpRequestLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Log before the request is sent to downstream service
        logger.info("=== HTTP REQUEST LOGGING ===");
        logger.info("Request Path: {}", exchange.getRequest().getPath().value());
        logger.info("Request Method: {}", exchange.getRequest().getMethod().name());
        logger.info("Request Headers: {}", exchange.getRequest().getHeaders());
        
        // Get the route information
        String routeId = exchange.getAttribute("GATEWAY_ROUTE_ID_ATTR");
        String routeUri = exchange.getAttribute("GATEWAY_ROUTE_URI");
        
        logger.info("Route ID: {}", routeId);
        logger.info("Route URI: {}", routeUri);
        
        // Try to get the final URL being called
        String finalUrl = exchange.getAttribute("GATEWAY_REQUEST_URL_ATTR");
        logger.info("Final URL: {}", finalUrl);
        
        // If we can't get the final URL, construct it from the route
        if (finalUrl == null && routeUri != null) {
            String path = exchange.getRequest().getPath().value();
            finalUrl = routeUri + path;
            logger.info("Constructed Final URL: {}", finalUrl);
        }
        
        logger.info("==========================");
        
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1; // After UrlLoggingFilter
    }
}

