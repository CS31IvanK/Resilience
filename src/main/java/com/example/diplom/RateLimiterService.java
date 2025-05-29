package com.example.diplom;

import io.github.resilience4j.ratelimiter.RateLimiter;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Getter
@Service
public class RateLimiterService extends RequestService {
    private final RateLimiter rateLimiter;

    @Autowired
    public RateLimiterService(RateLimiterImplementation rateLimiterImplementation) {
        this.rateLimiter = rateLimiterImplementation.getRateLimiter();
    }

    public String sendRequest() throws Throwable {
        return rateLimiter.executeCheckedSupplier(() ->
                simulateDelay("RateLimiter")
        );
    }
}