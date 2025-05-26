package com.example.diplom;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
@Getter
@Service
public class CircuitBreakerService {
    private final CircuitBreaker circuitBreaker;
    private final RequestService requestService;

    @Autowired
    public CircuitBreakerService(CircuitBreakerImplementation circuitBreakerImplementation, RequestService requestService) {
        this.circuitBreaker = circuitBreakerImplementation.getCircuitBreaker();
        this.requestService = new RequestService(WebClient.builder()); // ⬅ Створення окремого інстансу
    }

    public String sendRequest(int requestNumber) throws Throwable {
        return circuitBreaker.executeCheckedSupplier(() -> requestService.sendRequest("CircuitBreaker", requestNumber));
    }
}