package com.example.diplom;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class RequestService {
    private final WebClient webClient;

    public RequestService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8081").build();
    }

    public CompletableFuture<String> sendRequestAsync(String patternName, int requestNumber) {
        long delay = 300;

        if (ThreadLocalRandom.current().nextDouble() < 0.3) {
            long extraDelay = ThreadLocalRandom.current().nextLong(500, 5001);
            delay += extraDelay;
        }

        return webClient.get()
                .uri("/process?pattern=" + patternName + "&request=" + requestNumber + "&delay=" + delay)
                .retrieve()
                .bodyToMono(String.class)
                .toFuture();
    }
}