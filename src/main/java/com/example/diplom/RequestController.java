package com.example.diplom;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.timelimiter.TimeLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RequestController {

    private final CircuitBreaker circuitBreaker;
    private final Bulkhead bulkhead;
    private final RateLimiter rateLimiter;
    private final Retry retry;
    private final TimeLimiter timeLimiter;

    @Autowired
    public RequestController(CircuitBreakerImplementation circuitBreaker,
                             BulkheadImplementation bulkhead,
                             TimeLimiterImplementation timeLimiter,
                             RetryImplementation retry,
                             RateLimiterImplementation rateLimiter) {
        this.circuitBreaker = circuitBreaker.getCircuitBreaker();
        this.bulkhead = bulkhead.getBulkhead();
        this.rateLimiter = rateLimiter.getRateLimiter();
        this.retry = retry.getRetry();
        this.timeLimiter = timeLimiter.getTimeLimiter();
    }

    @GetMapping("/process")
    public String processRequest(@RequestParam String pattern,
                                 @RequestParam int request,
                                 @RequestParam long delay) throws InterruptedException {

        return rateLimiter.executeSupplier(() -> {
            return bulkhead.executeSupplier(() -> {
                try {
                    return circuitBreaker.executeCheckedSupplier(() -> {
                        Thread.sleep(delay);
                        return "[" + pattern + "] Response #" + request;
                    });
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }
}