package com.example.diplom;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.core.functions.CheckedSupplier;
import java.time.Duration;

public class BulkheadImplementation {
    private Bulkhead bulkhead;

    public BulkheadImplementation(String name) {
        BulkheadConfig config = BulkheadConfig.custom()
                .maxConcurrentCalls(10)
                .maxWaitDuration(Duration.ofMillis(100))
                .build();

        BulkheadRegistry registry = BulkheadRegistry.of(config);
        this.bulkhead = registry.bulkhead(name);
    }

    public <T> T execute(CheckedSupplier<T> supplier) throws Throwable {
        return bulkhead.executeCheckedSupplier(supplier);
    }
}
