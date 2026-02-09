package com.jay.template.infra.outbound.http.client.resiliency.bulkhead;

import java.io.IOException;
import java.net.URI;

import io.github.resilience4j.bulkhead.Bulkhead;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;

public final class BulkheadRequestDecoratorFactory implements ClientHttpRequestFactory {

    private final ClientHttpRequestFactory delegate;
    private final Bulkhead bulkhead;

    public BulkheadRequestDecoratorFactory(ClientHttpRequestFactory delegate, Bulkhead bulkhead) {
        this.delegate = delegate;
        this.bulkhead = bulkhead;
    }

    @Override
    public ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod) throws IOException {
        ClientHttpRequest req = delegate.createRequest(uri, httpMethod);
        return new BulkheadClientHttpRequestDecorator(req, bulkhead);
    }
}
