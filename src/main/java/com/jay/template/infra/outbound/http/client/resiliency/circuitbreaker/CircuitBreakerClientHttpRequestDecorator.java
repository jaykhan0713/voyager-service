package com.jay.template.infra.outbound.http.client.resiliency.circuitbreaker;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Map;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;

final class CircuitBreakerClientHttpRequestDecorator implements ClientHttpRequest {

    private final ClientHttpRequest delegate;
    private final CircuitBreaker circuitBreaker;

    CircuitBreakerClientHttpRequestDecorator(ClientHttpRequest delegate, CircuitBreaker circuitBreaker) {
        this.delegate = delegate;
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    public ClientHttpResponse execute() throws IOException {
        /* FUTURE-NOTE: Decide whether the circuit breaker should treat downstream HTTP 5xx responses as failures.
         *  Current behavior: breaker records failures only when the request throws
         *  (default settings records all exceptions).
         *
         *  FUTURE-NOTE: When needed, translate downstream 5xx into exceptions in the outbound client adapter
         *    so the breaker can learn.
         *
         *  Note: CallNotPermittedException is thrown when the breaker is open and the call is rejected
         *  (no call executed).
         */
        try {
            return circuitBreaker.executeCallable(delegate::execute);
        } catch (IOException | RuntimeException ex) { //RuntimeException accounts for CallNotPermittedException
            throw ex;
        } catch (Exception ex) { //needs wider Exception for executeCallable
            throw new IOException(ex);
        }
    }

    @Override public OutputStream getBody() throws IOException { return delegate.getBody(); }

    @Override public HttpMethod getMethod() { return delegate.getMethod(); }

    @Override public URI getURI() { return delegate.getURI(); }

    @Override public Map<String, Object> getAttributes() { return delegate.getAttributes(); }

    @Override public HttpHeaders getHeaders() { return delegate.getHeaders(); }
}