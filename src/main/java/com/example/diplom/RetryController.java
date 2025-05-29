package com.example.diplom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/retry")
public class RetryController {

    private final RetryService retryService;

    @Autowired
    public RetryController(RetryService retryService) {
        this.retryService = retryService;
    }

    @GetMapping
    public ResponseEntity<String> processRequest() {
        try {
            String response = retryService.sendRequest();
            return ResponseEntity.ok(response);
        } catch (Throwable t) {
            System.out.println("Retry");
            return ResponseEntity.status(500)
                    .body("RE: " + t.getMessage());
        }
    }
}