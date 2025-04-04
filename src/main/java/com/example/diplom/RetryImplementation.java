package com.example.diplom;

import io.github.resilience4j.core.functions.CheckedSupplier;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;

import java.time.Duration;

public class RetryImplementation {
    private Retry retry;

    public RetryImplementation(String name) {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(500))
                .build();

        RetryRegistry registry = RetryRegistry.of(config);
        this.retry = registry.retry(name);
    }

    public <T> T execute(CheckedSupplier<T> supplier) throws Throwable {
        return retry.executeCheckedSupplier(supplier);
    }
}
