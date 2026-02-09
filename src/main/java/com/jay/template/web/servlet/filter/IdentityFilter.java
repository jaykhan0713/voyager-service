package com.jay.template.web.servlet.filter;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

import com.jay.template.core.context.identity.Identity;
import com.jay.template.core.context.identity.IdentityContextHolder;
import com.jay.template.core.context.identity.IdentityContextSnapshot;
import com.jay.template.core.transport.http.IdentityHeaders;

/**
 * Binds identity to the current thread for the duration of a single HTTP identity.
 *
 * <p>
 * {@code IdentityFilter} extracts identity metadata from configured inbound headers
 * and stores it in {@link IdentityContextHolder} so downstream code can access a stable,
 * immutable {@link Identity} during identity processing.
 * </p>
 *
 * <p>
 * The identity propagation is cleared in a {@code finally} block to prevent leaking identity
 * state across thread reuse.
 * </p>
 */
public class IdentityFilter extends OncePerRequestFilter {

    private final IdentityHeaders headers;

    public IdentityFilter(IdentityHeaders headers) {
        this.headers = headers;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String userId = request.getHeader(headers.userId());
        String requestId = request.getHeader(headers.requestId());

        Identity identity = new Identity(userId, requestId);

        try {
            IdentityContextHolder.context(IdentityContextSnapshot.of(identity));
            filterChain.doFilter(request, response);
        } finally {
            IdentityContextHolder.clear();
        }
    }
}
