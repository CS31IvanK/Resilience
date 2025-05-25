package com.example.diplom;

import org.springframework.stereotype.Service;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RequestService {

    private final AtomicInteger requestCount = new AtomicInteger(0); // Лічильник запитів
    private volatile boolean failMode = false;
    private static final int THRESHOLD = 10;
    private static final long FAIL_DURATION_MS = 5000;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public String sendRequest(String patternName, int requestNumber) throws Exception {
        if (failMode) {
            throw new Exception("[" + patternName + "] Сервіс тимчасово недоступний для запиту #" + requestNumber);
        }

        int count = requestCount.incrementAndGet();
        if (count > THRESHOLD) {
            failMode = true;
            //System.out.println("[" + patternName + "] Запит #" + requestNumber + " перевищив поріг. Сервіс переходить у стан відмови...");
            scheduler.schedule(() -> {
                failMode = false;
                //System.out.println("[" + patternName + "] Сервіс відновлено після запиту #" + requestNumber);
            }, FAIL_DURATION_MS, TimeUnit.MILLISECONDS);

            throw new Exception("[" + patternName + "] Сервіс перевантажено для запиту #" + requestNumber);
        }

        Thread.sleep(100);
        return "[" + patternName + "] Response #" + requestNumber + " from https://example.com";
    }

    public void resetCounter() {
        requestCount.set(0);
    }

    public boolean isFailMode() {
        return failMode;
    }
}