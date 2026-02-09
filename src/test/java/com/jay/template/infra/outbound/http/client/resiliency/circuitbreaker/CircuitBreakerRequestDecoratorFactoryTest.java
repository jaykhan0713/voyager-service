package com.jay.template.infra.outbound.http.client.resiliency.circuitbreaker;

import java.io.IOException;
import java.net.URI;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CircuitBreakerRequestDecoratorFactoryTest {

    @Test
    void createRequestDelegatesAndWrapsWithCircuitBreakerDecorator() throws IOException {
        var delegate = mock(ClientHttpRequestFactory.class);
        var circuitBreaker = mock(CircuitBreaker.class);
        var originalRequest = mock(ClientHttpRequest.class);

        URI uri = URI.create("https://example.com");
        HttpMethod method = HttpMethod.GET;

        when(delegate.createRequest(uri, method)).thenReturn(originalRequest);

        var factory =
                new CircuitBreakerRequestDecoratorFactory(delegate, circuitBreaker);

        var result = factory.createRequest(uri, method);

        verify(delegate).createRequest(uri, method);
        assertInstanceOf(CircuitBreakerClientHttpRequestDecorator.class, result);
    }
}
