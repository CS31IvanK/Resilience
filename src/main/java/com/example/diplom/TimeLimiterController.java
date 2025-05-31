package com.example.diplom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/timelimiter")
public class TimeLimiterController {

    private final TimeLimiterService timeLimiterService;

    @Autowired
    public TimeLimiterController(TimeLimiterService timeLimiterService) {
        this.timeLimiterService = timeLimiterService;
    }

    @GetMapping
    public ResponseEntity<String> processRequest() {
        try {
            String response = timeLimiterService.sendRequest();
            return ResponseEntity.ok(response);
        } catch (Throwable t) {
            System.out.println("TLE");
            return ResponseEntity.status(500)
                    .body("TLE: " + t.getMessage());
        }
    }
}