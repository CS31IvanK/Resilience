package com.example.diplom;

import io.github.resilience4j.bulkhead.Bulkhead;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Getter
@Service
public class BulkheadService extends RequestService {
    private final Bulkhead bulkhead;

    @Autowired
    public BulkheadService(BulkheadImplementation bulkheadImplementation) {
        this.bulkhead = bulkheadImplementation.getBulkhead();
    }

    public String sendRequest() throws Throwable {
        return bulkhead.executeCheckedSupplier(() ->
                simulateDelay("Bulkhead")
        );
    }
}