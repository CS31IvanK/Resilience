package com.example.diplom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ResilienceTester {

    private final RequestService requestService;
    private final Monitoring monitoring;

    @Autowired
    public ResilienceTester(RequestService requestService, Monitoring monitoring) {
        this.requestService = requestService;
        this.monitoring = monitoring;
    }

    @Scheduled(fixedRate = 20000) // Запуск тестів кожні 20 секунд
    public void runTests() {
        System.out.println("\n--- Розпочато тестування ---");

        executeTest("CircuitBreaker", requestService::sendRequest);
        executeTest("RateLimiter", requestService::sendRequest);
        executeTest("Bulkhead", requestService::sendRequest);

        requestService.resetCounter(); // Скидаємо стан після завершення тесту
    }

    private void executeTest(String patternName, PatternProcessor processor) {
        System.out.println("Тестуємо паттерн: " + patternName);

        for (int i = 1; i <= 10; i++) {
            long startTime = System.nanoTime();
            try {
                // Виконання запиту
                String response = processor.process(patternName + " запит", i);
                monitoring.recordResponseTime(patternName, startTime);
                System.out.println("[" + patternName + "] Запит #" + i + ": Відповідь: " + response);
            } catch (Exception e) {
                monitoring.recordResponseTime(patternName, startTime);
                System.out.println("[" + patternName + "] Запит #" + i + ": Відмова: " + e.getMessage());

                // Очікуємо відновлення перед наступним запитом
                while (requestService.isFailMode()) {
                    try {
                        Thread.sleep(500); // Чекаємо, поки сервіс відновиться
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        }

        System.out.println("--------------------------------------");
    }

    @FunctionalInterface
    interface PatternProcessor {
        String process(String request, int requestNumber) throws Exception;
    }
}