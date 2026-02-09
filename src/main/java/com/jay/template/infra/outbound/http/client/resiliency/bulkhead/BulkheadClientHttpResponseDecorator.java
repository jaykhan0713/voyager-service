package com.jay.template.infra.outbound.http.client.resiliency.bulkhead;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;

final class BulkheadClientHttpResponseDecorator implements ClientHttpResponse {

        private final ClientHttpResponse delegate;
        private final Runnable releaseOnceGate;

        /*
         * Cached body avoids multiple delegate.getBody() calls.
         * Response bodies are consumed single-threaded.
         */
        private InputStream cachedBody;

        BulkheadClientHttpResponseDecorator(
                ClientHttpResponse delegate,
                Runnable releaseOnceGate
        ) {
            this.delegate = delegate;
            this.releaseOnceGate = releaseOnceGate;
        }

        /*
         * Spring consumption: Spring reads the response body fully (via converters),
         * maps to a DTO, and then closes the response. Releasing on close() holds the
         * permit for the full in-flight lifetime.
         *
         * Note: Some implementations close the body stream as part of response.close(),
         * and some close the body stream explicitly after consumption. We release on both
         * response close and body close (guarded) so either lifecycle signal can end the permit.
         *
         * Ultimately, both signals represent the end of HTTP IO consumption, which is where
         * this bulkhead permit should be released.
         */
        @Override
        public void close() {
            try {
                delegate.close();
            } finally {
                releaseOnceGate.run();
            }
        }

        /*
         * Streamed consumption: higher layers read the InputStream directly.
         * The permit must be held until the caller closes the stream.
         *
         * socket -> InputStream -> consumer code -> (read until EOF) -> inputStream.close()
         *
         */
        @Override public InputStream getBody() throws IOException {
            if (cachedBody == null) {
                cachedBody = new FilterInputStream(delegate.getBody()) {
                    @Override
                    public void close() throws IOException {
                        try {
                            super.close();
                        } finally {
                            releaseOnceGate.run();
                        }
                    }
                };
            }

            return cachedBody;
        }

        @Override public HttpStatusCode getStatusCode() throws IOException { return delegate.getStatusCode(); }

        @Override public String getStatusText() throws IOException { return delegate.getStatusText(); }

        @Override public HttpHeaders getHeaders() { return delegate.getHeaders(); }
    }