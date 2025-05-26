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
@Component
public class ResilienceTester {

    private final RestTemplate restTemplate;
    private final String baseUrl = "http://localhost:8081/process";

    @Autowired
    public ResilienceTester(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    @Scheduled(fixedRate = 5000)
    public void runTests() {
        for (int i = 1; i <= 10; i++) {
            int requestNumber = i;
            try {
                String url = baseUrl + "?requestNumber=" + requestNumber;
                ResponseEntity<List> response = restTemplate.getForEntity(url, List.class);
                System.out.println("Test " + requestNumber + " responses: " + response.getBody());
            } catch (Exception e) {
                System.err.println("Test " + requestNumber + " failed: " + e.getMessage());
            }
        }
    }
}