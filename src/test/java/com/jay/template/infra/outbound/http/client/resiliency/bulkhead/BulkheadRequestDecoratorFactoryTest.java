package com.jay.template.infra.outbound.http.client.resiliency.bulkhead;

import java.io.IOException;
import java.net.URI;

import io.github.resilience4j.bulkhead.Bulkhead;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BulkheadRequestDecoratorFactoryTest {

    @Test
    void createRequestDelegatesAndWrapsWithBulkheadDecorator() throws IOException {
        var delegate = mock(ClientHttpRequestFactory.class);
        var bulkhead = mock(Bulkhead.class);
        var originalRequest = mock(ClientHttpRequest.class);

        URI uri = URI.create("https://example.com");
        HttpMethod method = HttpMethod.GET;

        when(delegate.createRequest(uri, method)).thenReturn(originalRequest);

        var factory =
                new BulkheadRequestDecoratorFactory(delegate, bulkhead);

        var result = factory.createRequest(uri, method);

        verify(delegate).createRequest(uri, method);
        assertInstanceOf(BulkheadClientHttpRequestDecorator.class, result);
    }
}
