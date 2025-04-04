package com.example.diplom;

import org.springframework.stereotype.Service;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RequestService {

    private final AtomicInteger requestCount = new AtomicInteger(0); // Лічильник запитів
    private volatile boolean failMode = false; // Стан сервісу
    private static final int THRESHOLD = 2; // Поріг успішних запитів
    private static final long FAIL_DURATION_MS = 5000; // Тривалість відновлення (5 секунд)
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public synchronized String sendRequest(String request, int requestNumber) throws Exception {
        if (failMode) {
            throw new Exception("Сервіс тимчасово недоступний для запиту #" + requestNumber);
        }

        int count = requestCount.incrementAndGet();
        if (count > THRESHOLD) {
            failMode = true;
            System.out.println("Запит #" + requestNumber + " перевищив поріг. Сервіс переходить у стан відмови...");
            scheduler.schedule(() -> {
                failMode = false;
                System.out.println("Сервіс відновлено після запиту #" + requestNumber);
            }, FAIL_DURATION_MS, TimeUnit.MILLISECONDS);

            throw new Exception("Сервіс перевантажено для запиту #" + requestNumber);
        }

        // Емуляція успішного запиту із затримкою
        Thread.sleep(100);
        return "Response #" + requestNumber + " from https://example.com";
    }

    public void resetCounter() {
        requestCount.set(0); // Скидаємо лічильник запитів
    }

    public boolean isFailMode() {
        return failMode;
    }
}