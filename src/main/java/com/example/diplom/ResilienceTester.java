package com.example.diplom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.*;

@Component
public class ResilienceTester {

    private final RestTemplate restTemplate;
    private final String baseUrl = "http://localhost:8081/process";
    private final ExecutorService executorService =
            new ThreadPoolExecutor(50, 500, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    @Autowired
    public ResilienceTester(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    @Scheduled(fixedRate = 5000)
    public void runTests() {
        int baseRequests = 500;
        int peakRequests = ThreadLocalRandom.current().nextInt(50, 1001);
        for (int i = 1; i <= baseRequests + peakRequests; i++) {
            final int requestNumber = i;
            executorService.submit(() -> sendRequest(requestNumber));
        }
    }

    private void sendRequest(int requestNumber) {
        try {
            String url = baseUrl + "?requestNumber=" + requestNumber;
            ResponseEntity<List> response = restTemplate.getForEntity(url, List.class);
            System.out.println("Test " + requestNumber + " responses: " + response.getBody());
        } catch (Exception e) {
            System.err.println("Test " + requestNumber + " failed: " + e.getMessage());
        }
    }
}