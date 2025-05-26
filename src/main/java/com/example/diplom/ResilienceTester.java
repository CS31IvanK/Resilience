package com.example.diplom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
@Component
public class ResilienceTester {

    private final RestTemplate restTemplate;
    // Припустимо, що ваш RequestController слухає на порті 8081
    private final String baseUrl = "http://localhost:8081/process";

    @Autowired
    public ResilienceTester(RestTemplateBuilder restTemplateBuilder) {
        // Створюємо RestTemplate через RestTemplateBuilder для зручності налаштування
        this.restTemplate = restTemplateBuilder.build();
    }

    @Scheduled(fixedRate = 5000)
    public void runTests() {
        // Генеруємо 10 HTTP-запитів до ендпоінта /process кожні 5 секунд
        for (int i = 1; i <= 10; i++) {
            int requestNumber = i;
            try {
                // Формуємо URL з параметром запиту
                String url = baseUrl + "?requestNumber=" + requestNumber;
                // Надсилаємо HTTP GET-запит; очікуємо, що контролер поверне JSON-масив або список рядків
                ResponseEntity<List> response = restTemplate.getForEntity(url, List.class);
                System.out.println("Test " + requestNumber + " responses: " + response.getBody());
            } catch (Exception e) {
                System.err.println("Test " + requestNumber + " failed: " + e.getMessage());
            }
        }
    }
}