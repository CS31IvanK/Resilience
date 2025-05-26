package com.example.diplom;

import io.github.resilience4j.ratelimiter.RateLimiter;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
@Getter
@Service
public class RateLimiterService {
    private final RateLimiter rateLimiter;
    private final RequestService requestService;

    @Autowired
    public RateLimiterService(RateLimiterImplementation rateLimiterImplementation) {
        this.rateLimiter = rateLimiterImplementation.getRateLimiter();
        this.requestService = new RequestService(WebClient.builder()); // ⬅ Створення окремого інстансу
    }

    public String sendRequest(int requestNumber) throws Throwable {
        return rateLimiter.executeCheckedSupplier(() -> requestService.sendRequest("RateLimiter", requestNumber));
    }
}