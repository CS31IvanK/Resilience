package com.example.diplom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bulkhead")
public class BulkheadController {

    private final BulkheadService bulkheadService;

    @Autowired
    public BulkheadController(BulkheadService bulkheadService) {
        this.bulkheadService = bulkheadService;
    }

    @GetMapping
    public ResponseEntity<String> processRequest() {
        try {
            String response = bulkheadService.sendRequest();
            return ResponseEntity.ok(response);
        } catch (Throwable t) {
            System.out.println("BE");

            return ResponseEntity.status(500)
                    .body("BE: " + t.getMessage());
        }
    }
}