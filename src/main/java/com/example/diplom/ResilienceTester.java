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

    private final RequestService CBService;
    private final RequestService BHService;
    private final RequestService RLService;
    private final RequestService TLService;
    private final RequestService REService;

    private final CircuitBreakerImplementation circuitBreaker;
    private final BulkheadImplementation bulkhead;
    private final TimeLimiterImplementation timeLimiter;
    private final RateLimiterImplementation rateLimiter;
    private final RetryImplementation retry;

    @Autowired
    public ResilienceTester(RequestService CBService, RequestService BHService,
                            RequestService RLService, RequestService TLService,
                            RequestService REService, CircuitBreakerImplementation circuitBreaker,
                            BulkheadImplementation bulkhead, TimeLimiterImplementation timeLimiter,
                            RateLimiterImplementation rateLimiter, RetryImplementation retry) {
        this.CBService = CBService;
        this.BHService = BHService;
        this.RLService = RLService;
        this.TLService = TLService;
        this.REService = REService;
        this.circuitBreaker = circuitBreaker;
        this.bulkhead = bulkhead;
        this.timeLimiter = timeLimiter;
        this.rateLimiter = rateLimiter;
        this.retry = retry;
    }

    @Scheduled(fixedRate = 20000)
    public void runTests() {
        System.out.println("\n--- Розпочато тестування ---");

        executeConcurrentTest("CircuitBreaker", requestNumber -> safeExecute("CircuitBreaker",
                () -> circuitBreaker.execute(() -> CBService.sendRequest("CircuitBreaker", requestNumber))));

        executeConcurrentTest("Bulkhead", requestNumber -> safeExecute("Bulkhead",
                () -> bulkhead.execute(() -> BHService.sendRequest("Bulkhead", requestNumber))));

        executeConcurrentTest("TimeLimiter", requestNumber -> safeExecute("TimeLimiter",
                () -> timeLimiter.execute(() -> CompletableFuture.supplyAsync(() -> {
                    try {
                        return TLService.sendRequest("TimeLimiter", requestNumber);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }))));
        executeConcurrentTest("RateLimiter", requestNumber -> safeExecute("RateLimiter",
                () -> rateLimiter.execute(() -> RLService.sendRequest("RateLimiter", requestNumber))));

        executeConcurrentTest("Retry", requestNumber -> safeExecute("Retry",
                () -> retry.execute(() -> REService.sendRequest("Retry", requestNumber))));
    }

    private void executeConcurrentTest(String patternName, PatternProcessor processor) {
        //System.out.println("Тестуємо паттерн: " + patternName);

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (int i = 1; i <= 10; i++) {
            final int requestNumber = i;
            executorService.submit(() -> {
                /*String response =*/ executeWithPattern(patternName, processor, requestNumber);
                //System.out.println(response);
            });
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
    private String safeExecute(String patternName, CheckedSupplier<String> supplier) {
        try {
            return supplier.get();
        } catch (Throwable e) {
            return "[" + patternName + "] Помилка: " + e.getMessage();
        }
    }
    @FunctionalInterface
    interface PatternProcessor {
        String process(int requestNumber) throws Exception;
    }
}