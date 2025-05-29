package com.example.diplom;

import io.github.resilience4j.timelimiter.TimeLimiter;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Getter
@Service
public class TimeLimiterService extends RequestService {
    private final TimeLimiter timeLimiter;

    @Autowired
    public TimeLimiterService(TimeLimiterImplementation timeLimiterImplementation) {
        this.timeLimiter = timeLimiterImplementation.getTimeLimiter();
    }

    public String sendRequest() throws Throwable {
        return timeLimiter.executeFutureSupplier(() -> CompletableFuture.supplyAsync(() ->
                simulateDelay("TimeLimiter")
        ));
    }
}