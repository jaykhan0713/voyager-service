package com.jay.template.infra.outbound.http.client.resiliency.bulkhead;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;

final class BulkheadClientHttpRequestDecorator implements ClientHttpRequest {
    private final ClientHttpRequest delegate;
    private final Bulkhead bulkhead;

    BulkheadClientHttpRequestDecorator(ClientHttpRequest delegate, Bulkhead bulkhead) {
        this.delegate = delegate;
        this.bulkhead = bulkhead;
    }

    @Override
    public ClientHttpResponse execute() throws IOException {

        if (bulkhead.tryAcquirePermission()) {
            AtomicBoolean permitHeld = new AtomicBoolean(true);

            // 1 permit held needs exactly 1 safe release. A release-once gate.
            Runnable releaseOnceGate = () -> {
                if (permitHeld.compareAndSet(true, false)) {
                    bulkhead.releasePermission();
                }
            };

            try {
                ClientHttpResponse response = delegate.execute();
                return new BulkheadClientHttpResponseDecorator(response, releaseOnceGate);
            } catch (RuntimeException | IOException ex) {
                releaseOnceGate.run();
                throw ex;
            }
            /* NOTE: Do not want to finally release, as we want the permit only to be released
             * when response body is fully consumed by higher layers and close() is called.
             * ClientHttpResponse.close() for buffered responses and InputStream close()
             * for streamed responses.
             */

        } else {
            throw BulkheadFullException.createBulkheadFullException(bulkhead);
        }
    }

    @Override public OutputStream getBody() throws IOException { return delegate.getBody(); }

    @Override public HttpMethod getMethod() { return delegate.getMethod(); }

    @Override public URI getURI() { return delegate.getURI(); }

    @Override public Map<String, Object> getAttributes() { return delegate.getAttributes(); }

    @Override public HttpHeaders getHeaders() { return delegate.getHeaders(); }
}
