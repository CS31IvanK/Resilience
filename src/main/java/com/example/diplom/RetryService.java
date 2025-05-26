package com.example.diplom;

import io.github.resilience4j.retry.Retry;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
@Getter
@Service
public class RetryService {
    private final Retry retry;
    private final RequestService requestService;

    @Autowired
    public RetryService(RetryImplementation retryImplementation) {
        this.retry = retryImplementation.getRetry();
        this.requestService = new RequestService(WebClient.builder()); // ⬅ Окремий інстанс
    }

    public String sendRequest(int requestNumber) throws Throwable {
        return retry.executeCheckedSupplier(() -> requestService.sendRequest("Retry", requestNumber));
    }
}