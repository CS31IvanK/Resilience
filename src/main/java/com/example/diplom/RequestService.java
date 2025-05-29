package com.example.diplom;

import java.util.concurrent.ThreadLocalRandom;

public abstract class RequestService {
    protected String simulateDelay(String patternName) {
        long delay = 300;


        if (ThreadLocalRandom.current().nextDouble() < 0.5) {
            long extraDelay = ThreadLocalRandom.current().nextLong(100, 701);
            delay += extraDelay;
        }

        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "[" + patternName + "] Помилка: " + e.getMessage();
        }
        return "[" + patternName + "] Response after " + delay + "ms";
    }
}