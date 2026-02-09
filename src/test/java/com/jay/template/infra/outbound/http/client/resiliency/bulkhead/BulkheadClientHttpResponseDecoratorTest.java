package com.jay.template.infra.outbound.http.client.resiliency.bulkhead;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import io.github.resilience4j.bulkhead.Bulkhead;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BulkheadClientHttpResponseDecoratorTest {

    @Test
    void closeDelegatesAndReleasesPermitOnce() {
        var delegate = mock(ClientHttpResponse.class);

        var permitHeld = new AtomicBoolean(true);
        var bulkhead = mock(Bulkhead.class);

        Runnable releaseOnceGate = () -> {
            if (permitHeld.compareAndSet(true, false)) {
                bulkhead.releasePermission();
            }
        };

        var resp = new BulkheadClientHttpResponseDecorator(delegate, releaseOnceGate);

        resp.close();
        resp.close();

        verify(delegate, times(2)).close();
        verify(bulkhead, times(1)).releasePermission();
    }

    @Test
    void getBodyCachesStreamAndClosingBodyReleasesPermitOnce() throws IOException {
        var delegate = mock(ClientHttpResponse.class);

        InputStream delegateBody = new ByteArrayInputStream(new byte[] { 1, 2, 3 });
        when(delegate.getBody()).thenReturn(delegateBody);

        var permitHeld = new AtomicBoolean(true);
        var bulkhead = mock(io.github.resilience4j.bulkhead.Bulkhead.class);

        Runnable releaseOnceGate = () -> {
            if (permitHeld.compareAndSet(true, false)) {
                bulkhead.releasePermission();
            }
        };

        var resp = new BulkheadClientHttpResponseDecorator(delegate, releaseOnceGate);

        InputStream body1 = resp.getBody();
        InputStream body2 = resp.getBody();

        assertSame(body1, body2);
        verify(delegate, times(1)).getBody();

        body1.close();
        body1.close();

        verify(bulkhead, times(1)).releasePermission();
    }

    @Test
    void closingBodyThenClosingResponseReleasesPermitOnceTotal() throws IOException {
        var delegate = mock(ClientHttpResponse.class);

        InputStream delegateBody = new ByteArrayInputStream(new byte[] { 1 });
        when(delegate.getBody()).thenReturn(delegateBody);

        var permitHeld = new AtomicBoolean(true);
        var bulkhead = mock(Bulkhead.class);

        Runnable releaseOnceGate = () -> {
            if (permitHeld.compareAndSet(true, false)) {
                bulkhead.releasePermission();
            }
        };

        var resp = new BulkheadClientHttpResponseDecorator(delegate, releaseOnceGate);

        InputStream body = resp.getBody();
        body.close();
        resp.close();

        verify(delegate).close();
        verify(bulkhead, times(1)).releasePermission();
    }

    @Test
    void delegatesStatusAndHeaders() throws IOException {
        var delegate = mock(ClientHttpResponse.class);

        when(delegate.getStatusCode()).thenReturn(HttpStatus.OK);
        when(delegate.getStatusText()).thenReturn("OK");

        HttpHeaders headers = new HttpHeaders();
        headers.add("x-test", "1");
        when(delegate.getHeaders()).thenReturn(headers);

        var permitHeld = new AtomicBoolean(false);
        var bulkhead = mock(Bulkhead.class);

        Runnable releaseOnceGate = () -> {
            if (permitHeld.compareAndSet(true, false)) {
                bulkhead.releasePermission();
            }
        };

        var resp = new BulkheadClientHttpResponseDecorator(delegate, releaseOnceGate);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("OK", resp.getStatusText());
        assertSame(headers, resp.getHeaders());
    }
}
