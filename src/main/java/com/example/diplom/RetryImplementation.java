package com.example.diplom;

import io.github.resilience4j.core.functions.CheckedSupplier;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.time.Duration;
@Getter
@Component
public class RetryImplementation {
    private Retry retry;

    public RetryImplementation() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(800))
                .build();

        RetryRegistry registry = RetryRegistry.of(config);
        this.retry = registry.retry("Retry ");
    }

    public <T> T execute(CheckedSupplier<T> supplier) throws Throwable {
        return retry.executeCheckedSupplier(supplier);
    }
    public void updateConfig(RetryConfig newConfig) {
        RetryRegistry registry = RetryRegistry.of(newConfig);
        this.retry= registry.retry("Retry");
    }
    public Retry getRetry() {
        return this.retry;
    }
    public String getConfig() {
        return this.retry.getRetryConfig().toString();
    }

}
