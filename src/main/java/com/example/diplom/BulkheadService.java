package com.example.diplom;

import io.github.resilience4j.bulkhead.Bulkhead;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
@Getter
@Service
public class BulkheadService {
    private final Bulkhead bulkhead;
    private final RequestService requestService;

    @Autowired
    public BulkheadService(BulkheadImplementation bulkheadImplementation, RequestService requestService) {
        this.bulkhead = bulkheadImplementation.getBulkhead();
        this.requestService = requestService;
    }

    public String sendRequest(int requestNumber) throws Throwable {
        // Використовуємо механізм Bulkhead для обмеження одночасних запитів
        return bulkhead.executeCheckedSupplier(() ->
                requestService.sendRequest("Bulkhead", requestNumber)
        );
    }
}