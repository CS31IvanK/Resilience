package com.example.diplom;

import io.github.resilience4j.core.functions.CheckedSupplier;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;

import java.time.Duration;

public class RateLimiterImplementation {
    private RateLimiter rateLimiter;

    public RateLimiterImplementation(String name) {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .limitForPeriod(10)
                .timeoutDuration(Duration.ofMillis(100))
                .build();

        RateLimiterRegistry registry = RateLimiterRegistry.of(config);
        this.rateLimiter = registry.rateLimiter(name);
    }

    public <T> T execute(CheckedSupplier<T> supplier) throws Throwable {
        return rateLimiter.executeCheckedSupplier(supplier);
    }
}

