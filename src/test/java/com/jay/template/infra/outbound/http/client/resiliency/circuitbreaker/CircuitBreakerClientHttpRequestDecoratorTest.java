package com.jay.template.infra.outbound.http.client.resiliency.circuitbreaker;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.Callable;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CircuitBreakerClientHttpRequestDecoratorTest {

    @Test
    void executeDelegatesThroughCircuitBreakerAndReturnsResponse() throws Exception {
        var delegate = mock(ClientHttpRequest.class);
        var circuitBreaker = mock(CircuitBreaker.class);
        var response = mock(ClientHttpResponse.class);

        when(circuitBreaker.executeCallable(any(Callable.class))).thenReturn(response);

        var req = new CircuitBreakerClientHttpRequestDecorator(delegate, circuitBreaker);

        var out = req.execute();

        assertSame(response, out);
        verify(circuitBreaker).executeCallable(any(Callable.class));
    }

    @Test
    void executeWhenCircuitBreakerThrowsIOExceptionRethrowsSameIOException() throws Exception {
        var delegate = mock(ClientHttpRequest.class);
        var circuitBreaker = mock(CircuitBreaker.class);

        IOException ioEx = new IOException();
        when(circuitBreaker.executeCallable(any(Callable.class))).thenThrow(ioEx);

        var req = new CircuitBreakerClientHttpRequestDecorator(delegate, circuitBreaker);

        IOException thrown = assertThrows(IOException.class, req::execute);
        assertSame(ioEx, thrown);
    }

    @Test
    void executeWhenCircuitBreakerThrowsRuntimeExceptionRethrowsSameRuntimeException() throws Exception {
        var delegate = mock(ClientHttpRequest.class);
        var circuitBreaker = mock(CircuitBreaker.class);

        RuntimeException rtEx = new IllegalStateException();
        when(circuitBreaker.executeCallable(any(Callable.class))).thenThrow(rtEx);

        var req = new CircuitBreakerClientHttpRequestDecorator(delegate, circuitBreaker);

        RuntimeException thrown = assertThrows(RuntimeException.class, req::execute);
        assertSame(rtEx, thrown);
    }

    @Test
    void executeWhenCircuitBreakerThrowsCheckedExceptionWrapsAsIOException() throws Exception {
        var delegate = mock(ClientHttpRequest.class);
        var circuitBreaker = mock(CircuitBreaker.class);

        Exception checked = new Exception("checked");
        when(circuitBreaker.executeCallable(any(Callable.class))).thenThrow(checked);

        var req = new CircuitBreakerClientHttpRequestDecorator(delegate, circuitBreaker);

        IOException thrown = assertThrows(IOException.class, req::execute);
        assertSame(checked, thrown.getCause());
    }

    @Test
    void delegatesPassthroughMethods() {
        var delegate = mock(ClientHttpRequest.class);
        var circuitBreaker = mock(CircuitBreaker.class);

        var headers = new HttpHeaders();
        when(delegate.getHeaders()).thenReturn(headers);

        URI uri = URI.create("https://example.com");
        when(delegate.getURI()).thenReturn(uri);

        when(delegate.getMethod()).thenReturn(HttpMethod.GET);

        @SuppressWarnings("unchecked")
        Map<String, Object> attrs = mock(Map.class);
        when(delegate.getAttributes()).thenReturn(attrs);

        var req = new CircuitBreakerClientHttpRequestDecorator(delegate, circuitBreaker);

        assertSame(headers, req.getHeaders());
        assertSame(uri, req.getURI());
        assertSame(HttpMethod.GET, req.getMethod());
        assertSame(attrs, req.getAttributes());
    }
}
