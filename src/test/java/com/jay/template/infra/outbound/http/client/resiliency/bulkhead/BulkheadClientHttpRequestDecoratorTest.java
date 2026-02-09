package com.jay.template.infra.outbound.http.client.resiliency.bulkhead;

import java.io.IOException;
import java.time.Duration;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class BulkheadClientHttpRequestDecoratorTest {

    @Test
    void executeWhenPermitAcquiredWrapsResponseAndDoesNotReleaseImmediately() throws IOException {
        var delegate = mock(ClientHttpRequest.class);
        var bulkhead = mock(Bulkhead.class);
        var response = mock(ClientHttpResponse.class);

        when(bulkhead.tryAcquirePermission()).thenReturn(true);
        when(delegate.execute()).thenReturn(response);

        var req = new BulkheadClientHttpRequestDecorator(delegate, bulkhead);

        var out = req.execute();

        verify(bulkhead).tryAcquirePermission();
        verify(delegate).execute();

        verify(bulkhead, never()).releasePermission();
        assertInstanceOf(BulkheadClientHttpResponseDecorator.class, out);
    }

    @Test
    void executeWhenDelegateThrowsIOExceptionReleasesPermitOnceAndRethrows() throws IOException {
        var delegate = mock(ClientHttpRequest.class);
        var bulkhead = mock(Bulkhead.class);

        when(bulkhead.tryAcquirePermission()).thenReturn(true);
        when(delegate.execute()).thenThrow(new IOException());

        var req = new BulkheadClientHttpRequestDecorator(delegate, bulkhead);

        assertThrows(IOException.class, req::execute);

        verify(bulkhead).tryAcquirePermission();
        verify(delegate).execute();
        verify(bulkhead, times(1)).releasePermission();
        verifyNoMoreInteractions(bulkhead);
    }

    @Test
    void executeWhenDelegateThrowsRuntimeExceptionReleasesPermitOnceAndRethrows() throws IOException {
        var delegate = mock(ClientHttpRequest.class);
        var bulkhead = mock(Bulkhead.class);

        when(bulkhead.tryAcquirePermission()).thenReturn(true);
        when(delegate.execute()).thenThrow(new IllegalStateException());

        var req = new BulkheadClientHttpRequestDecorator(delegate, bulkhead);

        assertThrows(IllegalStateException.class, req::execute);

        verify(bulkhead).tryAcquirePermission();
        verify(delegate).execute();
        verify(bulkhead, times(1)).releasePermission();
        verifyNoMoreInteractions(bulkhead);
    }

    @Test
    void executeWhenPermitNotAcquiredThrowsBulkheadFullExceptionAndDoesNotCallDelegate() {
        var delegate = mock(ClientHttpRequest.class);

        Bulkhead bulkhead =
                Bulkhead.of(
                        "clientAOutboundClient",
                        BulkheadConfig.custom()
                                .maxConcurrentCalls(1)
                                .maxWaitDuration(Duration.ZERO)
                                .build()
                );

        // Occupy the only permit
        boolean acquired = bulkhead.tryAcquirePermission();
        assertTrue(acquired);

        var req = new BulkheadClientHttpRequestDecorator(delegate, bulkhead);

        assertThrows(BulkheadFullException.class, req::execute);

        verifyNoInteractions(delegate);

        // cleanup so the test doesn’t leak permits
        bulkhead.releasePermission();
    }
}
