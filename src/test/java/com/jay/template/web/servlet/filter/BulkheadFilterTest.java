package com.jay.template.web.servlet.filter;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

import io.github.resilience4j.bulkhead.Bulkhead;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.jay.template.web.servlet.error.ErrorResponseWriter;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import static com.jay.template.core.error.api.ErrorType.TOO_MANY_REQUESTS;

class BulkheadFilterTest {

    @Test
    void acquiresPermitThenReleasesPermit() throws ServletException, IOException {
        Bulkhead bulkhead = mock(Bulkhead.class);
        when(bulkhead.tryAcquirePermission()).thenReturn(true);

        ErrorResponseWriter errorResponseWriter = mock(ErrorResponseWriter.class);

        BulkheadFilter bulkheadFilter = new BulkheadFilter(bulkhead, errorResponseWriter);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        bulkheadFilter.doFilter(request, response, filterChain);

        verify(bulkhead).tryAcquirePermission();
        verify(filterChain).doFilter(request, response);
        verify(bulkhead).releasePermission();

        //after releasing permissions, bulkhead will not be used anymore.
        verifyNoMoreInteractions(bulkhead);

        verifyNoInteractions(errorResponseWriter); //never used in happy path.
    }

    @Test
    void whenFailsToAcquirePermitThenWritesErrorResponse() throws ServletException, IOException {
        Bulkhead bulkhead = mock(Bulkhead.class);
        when(bulkhead.tryAcquirePermission()).thenReturn(false);

        ErrorResponseWriter errorResponseWriter = mock(ErrorResponseWriter.class);

        BulkheadFilter bulkheadFilter = new BulkheadFilter(bulkhead, errorResponseWriter);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        bulkheadFilter.doFilter(request, response, filterChain);

        verify(bulkhead).tryAcquirePermission();
        //due to fail-fast, bulkhead should no longer be used.
        verifyNoMoreInteractions(bulkhead);

        // filterChain will never be called when permit is not granted. Write error response
        verifyNoInteractions(filterChain);

        verify(errorResponseWriter).writeJsonErrorResponse(response, TOO_MANY_REQUESTS);
    }

    @Test
    void acquiresPermitThenThrowsReleasesPermit() throws ServletException, IOException {
        Bulkhead bulkhead = mock(Bulkhead.class);
        when(bulkhead.tryAcquirePermission()).thenReturn(true);

        ErrorResponseWriter errorResponseWriter = mock(ErrorResponseWriter.class);

        BulkheadFilter bulkheadFilter = new BulkheadFilter(bulkhead, errorResponseWriter);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain filterChain = mock(FilterChain.class);
        doThrow(new IOException("filterChain throws")).when(filterChain).doFilter(request, response);

        assertThrows(IOException.class, () ->
                bulkheadFilter.doFilter(request, response, filterChain)
        );

        verify(bulkhead).tryAcquirePermission();
        verify(filterChain).doFilter(request, response);
        verify(bulkhead).releasePermission();

        //after releasing permissions, bulkhead will not be used anymore.
        verifyNoMoreInteractions(bulkhead);

        //never used since filterChain threw, not this filter's error.
        verifyNoInteractions(errorResponseWriter);
    }

}