package com.jay.template.web.servlet.filter;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import io.github.resilience4j.bulkhead.Bulkhead;
import org.springframework.web.filter.OncePerRequestFilter;

import com.jay.template.web.servlet.error.ErrorResponseWriter;

import static com.jay.template.core.error.api.ErrorType.TOO_MANY_REQUESTS;

/**
 * Inbound concurrency guard implemented as a servlet filter.
 *
 * <p>This filter enforces a global, filter-level concurrency limit using a
 * Resilience4j {@link io.github.resilience4j.bulkhead.Bulkhead} (semaphore-based).
 * In traditional servlet error, the servlet container parsed requests and dispatched them
 * onto a bounded worker thread pool. With virtual threads, that implicit bound
 * no longer exists, so explicit admission control is required.</p>
 *
 * <h2>Behavior</h2>
 * <ul>
 *   <li>Runs once per HTTP request (via {@link OncePerRequestFilter}).</li>
 *   <li>Attempts to acquire a bulkhead permit before request processing.</li>
 *   <li>If a permit is available, the request proceeds and the permit is released
 *       when processing completes.</li>
 *   <li>If no permit is available, the request is rejected immediately.</li>
 * </ul>
 *
 * <h2>Fail-fast design</h2>
 * <p>This filter is intentionally fail-fast. It does <strong>not</strong> wait
 * for permits. Waiting would park virtual threads while retaining request state
 * on the heap, increasing latency and memory pressure under load. Instead, excess
 * requests are rejected immediately to preserve service stability.</p>
 *
 * <h2>Error handling</h2>
 * <p>Rejections are handled directly at the filter layer, before the request
 * reaches Spring MVC. The {@link ErrorResponseWriter} is responsible for writing
 * the standardized {@link com.jay.template.api.v1.common.error.ErrorResponse}
 * JSON payload and HTTP status to the response. This ensures consistent error
 * contracts even when the request never reaches {@code DispatcherServlet}.</p>
 *
 * <h2>Threading model</h2>
 * <p>This filter executes on a virtual thread. The semaphore-based bulkhead limits
 * the number of in-flight requests inside the service, protecting heap, CPU, and
 * downstream dependencies from unbounded concurrency.</p>
 */
public class BulkheadFilter extends OncePerRequestFilter {

    private final Bulkhead bulkhead;
    private final ErrorResponseWriter errorResponseWriter;

    public BulkheadFilter(
            Bulkhead bulkhead,
            ErrorResponseWriter errorResponseWriter
    ) {
        this.bulkhead = bulkhead;
        this.errorResponseWriter = errorResponseWriter;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (bulkhead.tryAcquirePermission()) {
            try {
                filterChain.doFilter(request, response);
            } finally {
                bulkhead.releasePermission();
            }
        } else {
            errorResponseWriter.writeJsonErrorResponse(response, TOO_MANY_REQUESTS);
        }
    }
}
