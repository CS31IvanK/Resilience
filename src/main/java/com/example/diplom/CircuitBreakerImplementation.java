package com.example.diplom;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.functions.CheckedSupplier;
import lombok.Getter;
import org.springframework.stereotype.Component;
import java.time.Duration;

@Getter
@Component
public class CircuitBreakerImplementation {
    private CircuitBreaker circuitBreaker;

    public CircuitBreakerImplementation() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(30) // need to check this smh
                .waitDurationInOpenState(Duration.ofSeconds(10))
                .build();

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
        this.circuitBreaker = registry.circuitBreaker("CircuitBreaker");
    }

    public <T> T execute(CheckedSupplier<T> supplier) throws Throwable {
        return circuitBreaker.executeCheckedSupplier(supplier);
    }
    public CircuitBreaker getCircuitBreaker() {
        return this.circuitBreaker;
    }
    public void updateConfig(CircuitBreakerConfig newConfig) {
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(newConfig);
        this.circuitBreaker = registry.circuitBreaker("CircuitBreaker");
    }
    public String getConfig() {
        return this.circuitBreaker.getCircuitBreakerConfig().toString();
    }
}
