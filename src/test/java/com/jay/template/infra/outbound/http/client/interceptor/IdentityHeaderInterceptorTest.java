package com.jay.template.infra.outbound.http.client.interceptor;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

import com.jay.template.core.context.identity.Identity;
import com.jay.template.core.context.identity.IdentityContextHolder;
import com.jay.template.core.context.identity.IdentityContextSnapshot;
import com.jay.template.core.transport.http.IdentityHeaders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IdentityHeaderInterceptorTest {

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
    void addsIdentityHeadersAndDelegates() throws IOException {

        IdentityHeaderInterceptor interceptor = new IdentityHeaderInterceptor(identityHeaders);

        Identity identity = new Identity("user-001", "request-001");
        IdentityContextHolder.context(IdentityContextSnapshot.of(identity));

        HttpRequest request = mock(HttpRequest.class);
        HttpHeaders headers = new HttpHeaders();
        when(request.getHeaders()).thenReturn(headers);

        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        ClientHttpResponse response = mock(ClientHttpResponse.class);
        when(execution.execute(any(), any())).thenReturn(response);

        byte[] body = new byte[0];

        ClientHttpResponse result = interceptor.intercept(request, body, execution);

        assertEquals(1, headers.get(identityHeaders.userId()).size());
        assertEquals(identity.userId(), headers.getFirst(identityHeaders.userId()));

        assertEquals(1, headers.get(identityHeaders.requestId()).size());
        assertEquals(identity.requestId(), headers.getFirst(identityHeaders.requestId()));

        verify(execution).execute(request, body);
        assertSame(response, result);
    }

    @Test
    void addsEmptyIdentityHeadersAndDelegates() throws IOException {

        IdentityHeaderInterceptor interceptor = new IdentityHeaderInterceptor(identityHeaders);

        HttpRequest request = mock(HttpRequest.class);
        HttpHeaders headers = new HttpHeaders();
        when(request.getHeaders()).thenReturn(headers);

        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        ClientHttpResponse response = mock(ClientHttpResponse.class);
        when(execution.execute(any(), any())).thenReturn(response);

        byte[] body = new byte[0];

        ClientHttpResponse result = interceptor.intercept(request, body, execution);

        assertEquals(1, headers.get(identityHeaders.userId()).size());
        assertEquals("", headers.getFirst(identityHeaders.userId()));

        assertEquals(1, headers.get(identityHeaders.requestId()).size());
        assertEquals("", headers.getFirst(identityHeaders.requestId()));

        verify(execution).execute(request, body);
        assertSame(response, result);
    }

}