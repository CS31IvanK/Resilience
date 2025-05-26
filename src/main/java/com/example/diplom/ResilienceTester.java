package com.example.diplom;

import io.github.resilience4j.core.functions.CheckedSupplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
@Component
public class ResilienceTester {
    private final CircuitBreakerService circuitBreakerService;
    private final BulkheadService bulkheadService;
    private final RateLimiterService rateLimiterService;
    private final TimeLimiterService timeLimiterService;
    private final RetryService retryService;

    @Autowired
    public ResilienceTester(CircuitBreakerService circuitBreakerService,
                            BulkheadService bulkheadService,
                            RateLimiterService rateLimiterService,
                            TimeLimiterService timeLimiterService,
                            RetryService retryService) {
        this.circuitBreakerService = circuitBreakerService;
        this.bulkheadService = bulkheadService;
        this.rateLimiterService = rateLimiterService;
        this.timeLimiterService = timeLimiterService;
        this.retryService = retryService;
    }

    @Scheduled(fixedRate = 20000)
    public void runTests() {
        executeConcurrentTest("CircuitBreaker", requestNumber -> {
            try {
                return circuitBreakerService.sendRequest(requestNumber);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
        executeConcurrentTest("Bulkhead", requestNumber -> {
            try {
                return bulkheadService.sendRequest(requestNumber);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
        executeConcurrentTest("RateLimiter", requestNumber -> {
            try {
                return rateLimiterService.sendRequest(requestNumber);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
        executeConcurrentTest("TimeLimiter", requestNumber -> timeLimiterService.sendRequest(requestNumber));
        executeConcurrentTest("Retry", requestNumber -> {
            try {
                return retryService.sendRequest(requestNumber);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void executeConcurrentTest(String patternName, PatternProcessor processor) {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (int i = 1; i <= 10; i++) {
            final int requestNumber = i;
            executorService.submit(() -> executeWithPattern(patternName, processor, requestNumber));
        }
        executorService.shutdown();
    }

    private String executeWithPattern(String patternName, PatternProcessor processor, int requestNumber) {
        try {
            return processor.process(requestNumber);
        } catch (Exception e) {
            return "[" + patternName + "] Помилка: " + e.getMessage();
        }
    }

    @FunctionalInterface
    interface PatternProcessor {
        String process(int requestNumber) throws Exception;
    }
}