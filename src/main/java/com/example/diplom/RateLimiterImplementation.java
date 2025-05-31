package com.example.diplom;

import io.github.resilience4j.core.functions.CheckedSupplier;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.time.Duration;
@Component
@Getter
public class RateLimiterImplementation {
    private RateLimiter rateLimiter;

    public RateLimiterImplementation() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .limitForPeriod(10)
                .timeoutDuration(Duration.ofMillis(800))
                .build();

        RateLimiterRegistry registry = RateLimiterRegistry.of(config);
        this.rateLimiter = registry.rateLimiter("RateLimiter");
    }

    public <T> T execute(CheckedSupplier<T> supplier) throws Throwable {
        return rateLimiter.executeCheckedSupplier(supplier);
    }
    public void updateConfig(RateLimiterConfig newConfig) {
        RateLimiterRegistry registry = RateLimiterRegistry.of(newConfig);
        this.rateLimiter = registry.rateLimiter("RateLimiter");
    }
    public RateLimiter getRateLimiter() {
        return this.rateLimiter;
    }
    public String getConfig() {
        return this.rateLimiter.getRateLimiterConfig().toString();
    }
}

