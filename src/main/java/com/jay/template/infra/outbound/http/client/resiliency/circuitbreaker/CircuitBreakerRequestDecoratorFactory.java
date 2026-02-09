package com.jay.template.infra.outbound.http.client.resiliency.circuitbreaker;

import java.io.IOException;
import java.net.URI;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;

public final class CircuitBreakerRequestDecoratorFactory implements ClientHttpRequestFactory {

    private final ClientHttpRequestFactory delegate;
    private final CircuitBreaker circuitBreaker;

    public CircuitBreakerRequestDecoratorFactory(
            ClientHttpRequestFactory delegate,
            CircuitBreaker circuitBreaker
    ) {
        this.delegate = delegate;
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    public ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod) throws IOException {
        ClientHttpRequest req = delegate.createRequest(uri, httpMethod);
        return new CircuitBreakerClientHttpRequestDecorator(req, circuitBreaker);
    }
}
