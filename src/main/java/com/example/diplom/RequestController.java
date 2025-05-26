package com.example.diplom;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/process")
public class RequestController {

    private final RetryService retryService;
    private final CircuitBreakerService circuitBreakerService;
    private final BulkheadService bulkheadService;
    private final RateLimiterService rateLimiterService;
    private final TimeLimiterService timeLimiterService;
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    public RequestController(RetryService retryService,
                             CircuitBreakerService circuitBreakerService,
                             BulkheadService bulkheadService,
                             RateLimiterService rateLimiterService,
                             TimeLimiterService timeLimiterService) {
        this.retryService = retryService;
        this.circuitBreakerService = circuitBreakerService;
        this.bulkheadService = bulkheadService;
        this.rateLimiterService = rateLimiterService;
        this.timeLimiterService = timeLimiterService;
    }

    @GetMapping
    public CompletableFuture<List<String>> processRequest(@RequestParam int requestNumber) {
        CompletableFuture<String> retryFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return retryService.sendRequest(requestNumber);
            } catch (Throwable e) {
                return "Retry failed: " + e.getMessage();
            }
        }, executorService);

        CompletableFuture<String> bulkheadFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return bulkheadService.sendRequest(requestNumber);
            } catch (Throwable e) {
                return "Bulkhead failed: " + e.getMessage();
            }
        }, executorService);

        CompletableFuture<String> circuitBreakerFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return circuitBreakerService.sendRequest(requestNumber);
            } catch (Throwable e) {
                return "Circuit Breaker failed: " + e.getMessage();
            }
        }, executorService);

        CompletableFuture<String> rateLimiterFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return rateLimiterService.sendRequest(requestNumber);
            } catch (Throwable e) {
                return "Rate Limiter failed: " + e.getMessage();
            }
        }, executorService);

        CompletableFuture<String> timeLimiterFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return timeLimiterService.sendRequest(requestNumber);
            } catch (Throwable e) {
                return "Time Limiter failed: " + e.getMessage();
            }
        }, executorService);

        return CompletableFuture.allOf(retryFuture, bulkheadFuture, circuitBreakerFuture, rateLimiterFuture, timeLimiterFuture)
                .thenApply(v -> List.of(
                        retryFuture.join(),
                        bulkheadFuture.join(),
                        circuitBreakerFuture.join(),
                        rateLimiterFuture.join(),
                        timeLimiterFuture.join()
                ));
    }
}