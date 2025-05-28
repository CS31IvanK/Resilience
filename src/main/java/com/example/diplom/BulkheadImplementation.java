package com.example.diplom;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.core.functions.CheckedSupplier;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.time.Duration;
@Getter
@Component
public class BulkheadImplementation {
    private Bulkhead bulkhead;

    public BulkheadImplementation() {
        BulkheadConfig config = BulkheadConfig.custom()
                .maxConcurrentCalls(500)
                .maxWaitDuration(Duration.ofSeconds(1))
                .build();

        BulkheadRegistry registry = BulkheadRegistry.of(config);
        this.bulkhead = registry.bulkhead("Bulkhead");
    }
    public Bulkhead getBulkhead() {
        return this.bulkhead;
    }
    public <T> T execute(CheckedSupplier<T> supplier) throws Throwable {
        return bulkhead.executeCheckedSupplier(supplier);
    }
    public void updateConfig(BulkheadConfig newConfig) {
        BulkheadRegistry registry = BulkheadRegistry.of(newConfig);
        this.bulkhead = registry.bulkhead("Bulkhead");
    }
    public String getConfig() {
        return this.bulkhead.getBulkheadConfig().toString();
    }
}
