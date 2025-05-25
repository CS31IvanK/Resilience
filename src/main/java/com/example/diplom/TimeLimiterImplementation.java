package com.example.diplom;

import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Component
@Getter
public class TimeLimiterImplementation {
    private TimeLimiter timeLimiter;

    public TimeLimiterImplementation() {
        TimeLimiterConfig config = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(3))
                .build();

        TimeLimiterRegistry registry = TimeLimiterRegistry.of(config);
        this.timeLimiter = registry.timeLimiter("TimeLimiter");
    }

    public <T> T execute(Supplier<CompletableFuture<T>> supplier) throws Throwable {
        return timeLimiter.executeFutureSupplier(supplier);
    }

    public void updateConfig(TimeLimiterConfig newConfig) {
        TimeLimiterRegistry registry = TimeLimiterRegistry.of(newConfig);
        this.timeLimiter = registry.timeLimiter("TimeLimiter");
    }
    public String getConfig() {
        return this.timeLimiter.getTimeLimiterConfig().toString();
    }

}
