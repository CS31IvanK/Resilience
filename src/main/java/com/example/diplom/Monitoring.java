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

    // Виміряємо час відповіді і виводимо результат в консоль
    public void recordResponseTime(String serviceName, long startTime) {
        long elapsed = System.nanoTime() - startTime;
        Timer timer = getResponseTimeTimer(serviceName);
        timer.record(elapsed, TimeUnit.NANOSECONDS);
        System.out.println("[" + serviceName + "] Час відповіді: " + TimeUnit.NANOSECONDS.toMillis(elapsed) + " ms");
    }
}