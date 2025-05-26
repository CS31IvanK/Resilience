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


    @Scheduled(fixedRate = 10000)
    public void updateDynamicConfigurations() {
        double avgResponseTime1m = prometheusClientService.getAverageResponseTime("1m");
        double avgResponseTime5m = prometheusClientService.getAverageResponseTime("5m");
        //double errorRate1m = prometheusClientService.getErrorRate("1m");
        //double errorRate5m = prometheusClientService.getErrorRate("5m");
        double cpuUsage = prometheusClientService.getCpuUsage();
        double memoryUsage = prometheusClientService.getMemoryUsage();

        System.out.println("=== Prometheus Metrics ===");
        System.out.println("Avg Response Time (1m): " + avgResponseTime1m + " sec");
        System.out.println("Avg Response Time (5m): " + avgResponseTime5m + " sec");
        //System.out.println("Error Rate (1m): " + errorRate1m + " %");
        //System.out.println("Error Rate (5m): " + errorRate5m + " %");
        System.out.println("CPU Usage: " + cpuUsage);
        System.out.println("Memory Usage: " + memoryUsage + " bytes");
        System.out.println("==========================");

        long newTimeoutMillis = (long) (avgResponseTime1m * 1500);
        if (newTimeoutMillis < 500) {
            newTimeoutMillis = 500;
        }
        Duration newTimeLimiterTimeout = Duration.ofMillis(newTimeoutMillis);
        System.out.println("Updating TimeLimiter timeout from current value "+ timeLimiterImplementation.getConfig() +" to: " + newTimeLimiterTimeout.toMillis() + " ms");
        TimeLimiterConfig newTimeLimiterConfig = TimeLimiterConfig.custom()
                .timeoutDuration(newTimeLimiterTimeout)
                .build();
        timeLimiterImplementation.updateConfig(newTimeLimiterConfig);

        //alternative
        int newDuration = (int) ((avgResponseTime1m > avgResponseTime5m) ? avgResponseTime1m*100 : avgResponseTime1m);
        CircuitBreakerConfig newCircuitBreakerConfig = CircuitBreakerConfig.custom()
                .waitDurationInOpenState(Duration.ofSeconds(newDuration))
                .build();
        circuitBreakerImplementation.updateConfig(newCircuitBreakerConfig);

        int rateLim =  (int) ((avgResponseTime1m > avgResponseTime5m) ? 300 : 500);
        RateLimiterConfig newRateLimiterConfig = RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .limitForPeriod(rateLim)
                .timeoutDuration(Duration.ofMillis(1000))
                .build();
        rateLimiterImplementation.updateConfig(newRateLimiterConfig);

        /*int newFailureThreshold = (errorRate1m > 5.0) ? 10 : 1;
        System.out.println("Updating CircuitBreaker failure threshold from "+ circuitBreakerImplementation.getConfig() +" to: " + newFailureThreshold + " %"); //check this output
        CircuitBreakerConfig newCircuitBreakerConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(newFailureThreshold)
                .waitDurationInOpenState(Duration.ofSeconds(1))
                .build();
        circuitBreakerImplementation.updateConfig(newCircuitBreakerConfig);


        int newRateLimit = (errorRate1m > 5.0) ? 300 : 500;
        System.out.println("Updating RateLimiter limit for period from "+ rateLimiterImplementation.getConfig() + " to: " + newRateLimit);
        RateLimiterConfig newRateLimiterConfig = RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .limitForPeriod(newRateLimit)
                .timeoutDuration(Duration.ofMillis(1000))
                .build();
        rateLimiterImplementation.updateConfig(newRateLimiterConfig);*/


        int newMaxConcurrentCalls = (cpuUsage > 0.5) ? 500 : 1000;
        System.out.println("Updating Bulkhead max concurrent calls from " + bulkheadImplementation.getConfig() + " to: " + newMaxConcurrentCalls);
        BulkheadConfig newBulkheadConfig = BulkheadConfig.custom()
                .maxConcurrentCalls(newMaxConcurrentCalls)
                .maxWaitDuration(Duration.ofSeconds(1))
                .build();
        bulkheadImplementation.updateConfig(newBulkheadConfig);

        //alternative
        int retries =  (int) ((avgResponseTime1m > avgResponseTime5m) ? 4 : 8);
        RetryConfig newRetryConfig = RetryConfig.custom()
                .maxAttempts(retries)
                .waitDuration(Duration.ofMillis(500))
                .build();
        retryImplementation.updateConfig(newRetryConfig);

        /*int newMaxAttempts = (errorRate1m > 5.0) ? 3 : 5;
        System.out.println("Updating Retry max attempts from "+ retryImplementation.getConfig() +" to: " + newMaxAttempts);
        RetryConfig newRetryConfig = RetryConfig.custom()
                .maxAttempts(newMaxAttempts)
                .waitDuration(Duration.ofMillis(500))
                .build();
        retryImplementation.updateConfig(newRetryConfig);*/

        System.out.println("Dynamic configuration update complete.\n");
    }
}