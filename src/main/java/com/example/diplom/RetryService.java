package com.example.diplom;

import io.github.resilience4j.retry.Retry;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Getter
@Service
public class RetryService extends RequestService {
    private final Retry retry;

    @Autowired
    public RetryService(RetryImplementation retryImplementation) {
        this.retry = retryImplementation.getRetry();
    }

    public String sendRequest() throws Throwable {
        return retry.executeCheckedSupplier(() ->
                simulateDelay("Retry")
        );
    }
}