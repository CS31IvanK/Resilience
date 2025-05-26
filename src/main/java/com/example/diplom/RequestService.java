package com.example.diplom;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class RequestService {

    private final WebClient webClient;

    public RequestService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8081").build();
    }

    public String sendRequest(String patternName, int requestNumber) throws Exception {
        long delay = 100;

        // Додаємо випадкову додаткову затримку
        if (ThreadLocalRandom.current().nextDouble() < 0.3) {
            long extraDelay = ThreadLocalRandom.current().nextLong(200, 401);
            delay += extraDelay;
        }

        // Виконуємо HTTP-запит до іншого сервісу
        return webClient.get()
                .uri("/process?pattern=" + patternName + "&request=" + requestNumber + "&delay=" + delay)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}