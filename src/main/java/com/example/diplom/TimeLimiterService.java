package com.example.diplom;

import io.github.resilience4j.timelimiter.TimeLimiter;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.CompletableFuture;
@Getter
@Service
public class TimeLimiterService {
    private final TimeLimiter timeLimiter;
    private final RequestService requestService;

    @Autowired
    public TimeLimiterService(TimeLimiterImplementation timeLimiterImplementation) {
        this.timeLimiter = timeLimiterImplementation.getTimeLimiter();
        this.requestService = new RequestService(WebClient.builder()); // ⬅ Окремий інстанс
    }

    public String sendRequest(int requestNumber) throws Exception {
        return timeLimiter.executeFutureSupplier(() -> CompletableFuture.supplyAsync(() ->
        {
            try {
                return requestService.sendRequest("TimeLimiter", requestNumber);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }));
    }
}