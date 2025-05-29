package com.example.diplom;

import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class DynamicConfigurationService {

    private final long startTime = System.currentTimeMillis();

    private double historicalResponseTime1m = 0.0;
    private double alpha = 0.3;

    private final PrometheusClientService prometheusClientService;
    private final TimeLimiterImplementation timeLimiterImplementation;
    private final CircuitBreakerImplementation circuitBreakerImplementation;
    private final RateLimiterImplementation rateLimiterImplementation;
    private final BulkheadImplementation bulkheadImplementation;
    private final RetryImplementation retryImplementation;

    @Autowired
    public DynamicConfigurationService(PrometheusClientService prometheusClientService,
                                       TimeLimiterImplementation timeLimiterImplementation,
                                       CircuitBreakerImplementation circuitBreakerImplementation,
                                       RateLimiterImplementation rateLimiterImplementation,
                                       BulkheadImplementation bulkheadImplementation,
                                       RetryImplementation retryImplementation) {
        this.prometheusClientService = prometheusClientService;
        this.timeLimiterImplementation = timeLimiterImplementation;
        this.circuitBreakerImplementation = circuitBreakerImplementation;
        this.rateLimiterImplementation = rateLimiterImplementation;
        this.bulkheadImplementation = bulkheadImplementation;
        this.retryImplementation = retryImplementation;
    }

    @Scheduled(fixedRate = 60000)
    public void updateDynamicConfigurations() {


        double currentResponseTime1m = prometheusClientService.getAverageResponseTime("1m");
        double avgResponseTime5m = prometheusClientService.getAverageResponseTime("5m");
        double cpuUsage = prometheusClientService.getCpuUsage();
        double memoryUsage = prometheusClientService.getMemoryUsage();
        double p90m1 = prometheusClientService.getPercentileResponseTime(90.0, "1m");
        double p50m1 = prometheusClientService.getPercentileResponseTime(50.0, "1m");


        if (historicalResponseTime1m == 0.0) {
            historicalResponseTime1m = currentResponseTime1m;
        } else {
            historicalResponseTime1m = alpha * currentResponseTime1m + (1 - alpha) * historicalResponseTime1m;
        }

        System.out.println("=== Prometheus Metrics ===");
        System.out.println("Current Avg Response Time (1m): " + currentResponseTime1m + " sec");
        System.out.println("Historical (smoothed) Response Time (1m): " + historicalResponseTime1m + " sec");
        System.out.println("Avg Response Time (5m): " + avgResponseTime5m + " sec");
        System.out.println("CPU Usage: " + cpuUsage);
        System.out.println("Memory Usage: " + memoryUsage + " bytes");
        System.out.println("==========================");


        long elapsedTime = System.currentTimeMillis() - startTime;
        if (elapsedTime < 10 * 60 * 1000) {
            // System.out.println("Dynamic config disabled during observation period. Elapsed: " + (elapsedTime/60000) + " min");
            return;
        }


        long newTimeoutMillis = (long) (historicalResponseTime1m * 1500);
        if (newTimeoutMillis < 500) {
            newTimeoutMillis = 500;
        }
        Duration newTimeLimiterTimeout = Duration.ofMillis(newTimeoutMillis);
        TimeLimiterConfig newTimeLimiterConfig = TimeLimiterConfig.custom()
                .timeoutDuration(newTimeLimiterTimeout)
                .build();
        timeLimiterImplementation.updateConfig(newTimeLimiterConfig);

        //alternative
        int newDuration = (int) ((historicalResponseTime1m > avgResponseTime5m) ? (historicalResponseTime1m-avgResponseTime5m)/2 : avgResponseTime5m)+1;
        CircuitBreakerConfig newCircuitBreakerConfig = CircuitBreakerConfig.custom()
                .waitDurationInOpenState(Duration.ofSeconds(newDuration))
                .build();
        circuitBreakerImplementation.updateConfig(newCircuitBreakerConfig);

        int rateLim = (historicalResponseTime1m > avgResponseTime5m) ? 200 : 400;
        RateLimiterConfig newRateLimiterConfig = RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .limitForPeriod(rateLim)
                .timeoutDuration(Duration.ofMillis(1000))
                .build();
        rateLimiterImplementation.updateConfig(newRateLimiterConfig);

        int newMaxConcurrentCalls = (historicalResponseTime1m > 1) ? 250 : 500;
        BulkheadConfig newBulkheadConfig = BulkheadConfig.custom()
                .maxConcurrentCalls(newMaxConcurrentCalls)
                .maxWaitDuration(Duration.ofSeconds(1))
                .build();
        bulkheadImplementation.updateConfig(newBulkheadConfig);

        int retries = (p90m1 > 1.2 * p50m1) ? 3 : 5;
        RetryConfig newRetryConfig = RetryConfig.custom()
                .maxAttempts(retries)
                .waitDuration(Duration.ofMillis(500))
                .build();
        retryImplementation.updateConfig(newRetryConfig);

        System.out.println("Dynamic configuration update complete.\n");
    }
}