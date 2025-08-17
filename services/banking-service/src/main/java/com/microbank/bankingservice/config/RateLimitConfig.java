package com.microbank.bankingservice.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RateLimitConfig {

    @Bean
    public Bucket transactionBucket() {
        // Allow 10 transactions per minute per user
        Bandwidth limit = Bandwidth.classic(10, Refill.greedy(10, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }
    
    @Bean
    public Bucket accountCreationBucket() {
        // Allow 2 account creations per hour per user
        Bandwidth limit = Bandwidth.classic(2, Refill.greedy(2, Duration.ofHours(1)));
        return Bucket.builder().addLimit(limit).build();
    }
}
