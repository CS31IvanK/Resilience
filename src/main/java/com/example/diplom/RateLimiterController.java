package com.example.diplom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ratelimiter")
public class RateLimiterController {

    private final RateLimiterService rateLimiterService;

    @Autowired
    public RateLimiterController(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @GetMapping
    public ResponseEntity<String> processRequest() {
        try {
            String response = rateLimiterService.sendRequest();
            return ResponseEntity.ok(response);
        } catch (Throwable t) {
            System.out.println("RLE");

            return ResponseEntity.status(500)
                    .body("RLE: " + t.getMessage());
        }
    }
}