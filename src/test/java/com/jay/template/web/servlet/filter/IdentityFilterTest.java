package com.jay.template.web.servlet.filter;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.jay.template.core.context.identity.IdentityContextHolder;
import com.jay.template.core.context.identity.IdentityContextSnapshot;
import com.jay.template.core.transport.http.IdentityHeaders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IdentityFilterTest {

    static IdentityHeaders identityHeaders;

    @BeforeAll
    static void initClass() {
        identityHeaders = new IdentityHeaders("x-user-id", "x-request-id");
    }

    @BeforeEach
    void setUp() {
        IdentityContextHolder.clear();
    }

    @AfterEach
    void tearDown() {
        IdentityContextHolder.clear();
    }

    @Test
    void filterChainIsCalled() throws ServletException, IOException {
        IdentityFilter filter = new IdentityFilter(identityHeaders);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        assertSame(request, filterChain.getRequest());
        assertSame(response, filterChain.getResponse());
    }

    @Test
    void identityContextSnapshotIsSetAndClears() throws ServletException, IOException {
        IdentityFilter filter = new IdentityFilter(identityHeaders);

        String userId = "user-001";
        String requestId = "req-001";

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(identityHeaders.userId(), userId);
        request.addHeader(identityHeaders.requestId(), requestId);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain assertingChain = (req, res) -> {
            IdentityContextSnapshot ctx = IdentityContextHolder.context();
            assertNotNull(ctx);
            assertNotSame(IdentityContextSnapshot.EMPTY, ctx);
            assertEquals(userId, ctx.identity().userId());
            assertEquals(requestId, ctx.identity().requestId());
        };

        filter.doFilter(request, response, assertingChain);

        IdentityContextSnapshot context = IdentityContextHolder.context();
        assertSame(IdentityContextSnapshot.EMPTY, context);
    }

    @Test
    void clearsContextWhenChainThrows() {
        IdentityFilter filter = new IdentityFilter(identityHeaders);

        MockHttpServletRequest request = new MockHttpServletRequest();

        request.addHeader(identityHeaders.userId(), "user-001");
        request.addHeader(identityHeaders.requestId(), "req-001");
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain throwingChain = (req, res) -> { throw new ServletException("error"); };

        assertThrows(ServletException.class, () -> filter.doFilter(request, response, throwingChain));
        assertSame(IdentityContextSnapshot.EMPTY, IdentityContextHolder.context());
    }

}