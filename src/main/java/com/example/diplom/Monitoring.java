package com.example.diplom;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class Monitoring {

    private final MeterRegistry meterRegistry;

    @Autowired
    public Monitoring(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public Timer getResponseTimeTimer(String serviceName) {
        return meterRegistry.timer("service.response.time", "service", serviceName);
    }

    public void recordResponseTime(String serviceName, long startTime, int num) {
        long elapsed = System.nanoTime() - startTime;
        Timer timer = getResponseTimeTimer(serviceName);
        timer.record(elapsed, TimeUnit.NANOSECONDS);
        System.out.println("[" + serviceName + "] Час відповіді " +  num + " : " + TimeUnit.NANOSECONDS.toMillis(elapsed) + " ms");
    }

    public void recordWaitTime(String serviceName, long waitStartTime, int num) {
        long waitElapsed = System.nanoTime() - waitStartTime;
        System.out.println("[" + serviceName + "] Час очікування " +  num + " : " + TimeUnit.NANOSECONDS.toMillis(waitElapsed) + " ms");
    }

    public void recordSummary(String patternName, int successCount, int failureCount, long totalStartTime) {
        long totalElapsed = System.nanoTime() - totalStartTime;
        System.out.println("[" + patternName + "] Загальна кількість успішних запитів: " + successCount);
        System.out.println("[" + patternName + "] Загальна кількість невдалих запитів: " + failureCount);
        System.out.println("[" + patternName + "] Загальний час тестування: " + TimeUnit.NANOSECONDS.toMillis(totalElapsed) + " ms");
    }
}