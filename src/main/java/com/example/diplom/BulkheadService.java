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
    public BulkheadService(BulkheadImplementation bulkheadImplementation) {
        this.bulkhead = bulkheadImplementation.getBulkhead();
        this.requestService = new RequestService(WebClient.builder()); // ⬅ Окремий інстанс
    }

    public String sendRequest(int requestNumber) throws Throwable {
        return bulkhead.executeCheckedSupplier(() -> requestService.sendRequest("Bulkhead", requestNumber));
    }
}