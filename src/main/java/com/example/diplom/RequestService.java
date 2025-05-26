package com.example.diplom;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class RequestService {

    public String sendRequest(String patternName, int requestNumber) {
        long delay = 300;
        // Генеруємо випадкову додаткову затримку з ймовірністю 50%
        if (ThreadLocalRandom.current().nextDouble() < 0.5) {
            long extraDelay = ThreadLocalRandom.current().nextLong(500, 5001);
            delay += extraDelay;
        }

        try {
            // Симулюємо обробку із затримкою
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "[" + patternName + "] Помилка: " + e.getMessage();
        }
        return "[" + patternName + "] Response #" + requestNumber + " after " + delay + "ms";
    }
}