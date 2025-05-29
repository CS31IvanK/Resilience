package com.example.diplom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/circuitbreaker")
public class CircuitBreakerController {

    private final CircuitBreakerService circuitBreakerService;

    @Autowired
    public CircuitBreakerController(CircuitBreakerService circuitBreakerService) {
        this.circuitBreakerService = circuitBreakerService;
    }

    @GetMapping
    public ResponseEntity<String> processRequest() {
        try {
            String response = circuitBreakerService.sendRequest();
            return ResponseEntity.ok(response);
        } catch (Throwable t) {
            System.out.println("CircuitbreakerError");
            return ResponseEntity.status(500)
                    .body("CBE: " + t.getMessage());
        }
    }
}