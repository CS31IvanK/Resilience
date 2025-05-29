package com.example.diplom;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Getter
@Service
public class CircuitBreakerService extends RequestService {
    private final CircuitBreaker circuitBreaker;

    @Autowired
    public CircuitBreakerService(CircuitBreakerImplementation circuitBreakerImplementation) {
        this.circuitBreaker = circuitBreakerImplementation.getCircuitBreaker();
    }

    public String sendRequest() throws Throwable {
        return circuitBreaker.executeCheckedSupplier(() ->
                simulateDelay("CircuitBreaker")
        );
    }
}