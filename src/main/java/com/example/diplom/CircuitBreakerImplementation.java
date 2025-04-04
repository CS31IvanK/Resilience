package com.example.diplom;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.functions.CheckedSupplier;

import java.time.Duration;

public class CircuitBreakerImplementation {
    private CircuitBreaker circuitBreaker;

    public CircuitBreakerImplementation(String name) {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(10))
                .build();

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
        this.circuitBreaker = registry.circuitBreaker(name);
    }

    public <T> T execute(CheckedSupplier<T> supplier) throws Throwable {
        return circuitBreaker.executeCheckedSupplier(supplier);
    }
}
