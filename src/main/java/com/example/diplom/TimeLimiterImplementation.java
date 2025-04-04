package com.example.diplom;

import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class TimeLimiterImplementation {
    private TimeLimiter timeLimiter;

    public TimeLimiterImplementation(String name) {
        TimeLimiterConfig config = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(3))
                .build();

        TimeLimiterRegistry registry = TimeLimiterRegistry.of(config);
        this.timeLimiter = registry.timeLimiter(name);
    }

    public <T> T execute(Supplier<CompletableFuture<T>> supplier) throws Throwable {
        return timeLimiter.executeFutureSupplier(supplier);
    }
}
