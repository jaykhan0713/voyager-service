package com.jay.template.infra.outbound.http.client.rest.error;

import java.util.function.Predicate;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import com.jay.template.core.error.dependency.DependencyCallException;
import com.jay.template.core.error.dependency.Reason;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RestClientExceptionTranslatorTest {

    @Test
    void supplierGetsObject() {
        Object o = new Object();
        Object result =
                RestClientExceptionTranslator.execute(() -> o, "someClient");
        assertSame(o, result);
    }

    @Test
    void supplierCatchesResourceAccessExceptionAndThrowsDependencyCallException() {
        String clientName = "someClient";
        ResourceAccessException resourceAccessException = new ResourceAccessException("");
        var depEx =
                assertThrows(DependencyCallException.class, () ->
                        RestClientExceptionTranslator.execute(() -> { throw resourceAccessException; }, clientName)
                );

        assertEquals(clientName, depEx.clientName());
        assertEquals(Reason.IO_ERROR, depEx.reason());
        assertSame(resourceAccessException, depEx.getCause());
    }

    @Test
    void supplierCatchesBulkheadExceptionAndThrowsDependencyCallException() {
        String clientName = "someClient";

        BulkheadFullException bulkheadEx =
                BulkheadFullException.createBulkheadFullException(
                        Bulkhead.of(clientName, BulkheadConfig.ofDefaults())
                );

        var depEx =
                assertThrows(DependencyCallException.class, () ->
                        RestClientExceptionTranslator.execute(() -> { throw bulkheadEx; }, clientName)
                );

        assertEquals(clientName, depEx.clientName());
        assertEquals(Reason.CAPACITY_REJECTED, depEx.reason());
        assertSame(bulkheadEx, depEx.getCause());
    }

    @Test
    void supplierCatchesCallNotPermittedExceptionAndThrowsDependencyCallException() {
        String clientName = "someClient";

        CallNotPermittedException cnpEx =
                CallNotPermittedException.createCallNotPermittedException(CircuitBreaker.ofDefaults(clientName));

        var depEx =
                assertThrows(DependencyCallException.class, () ->
                        RestClientExceptionTranslator.execute(() -> { throw cnpEx; }, clientName)
                );

        assertEquals(clientName, depEx.clientName());
        assertEquals(Reason.SHORT_CIRCUITED, depEx.reason());
        assertSame(cnpEx, depEx.getCause());
    }

    @Test
    void supplierCatchesRuntimeExceptionAndThrowsDependencyCallException() {
        String clientName = "someClient";

        RuntimeException runtimeException = new RuntimeException("");

        var depEx =
                assertThrows(DependencyCallException.class, () ->
                        RestClientExceptionTranslator.execute(() -> { throw runtimeException; }, clientName)
                );

        assertEquals(clientName, depEx.clientName());
        assertEquals(Reason.UNKNOWN, depEx.reason());
        assertSame(runtimeException, depEx.getCause());
    }

    @Test
    void applyDefaultOnStatusHandlersRegisters4xxAnd5xxHandlersThatThrowDependencyCallException() {
        String clientName = "someClient";

        RestClient.ResponseSpec spec = mock(RestClient.ResponseSpec.class);

        // Captors for the two calls
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Predicate<HttpStatusCode>> predicateCaptor = ArgumentCaptor.forClass(Predicate.class);

        ArgumentCaptor<RestClient.ResponseSpec.ErrorHandler> handlerCaptor =
                ArgumentCaptor.forClass(RestClient.ResponseSpec.ErrorHandler.class);

        when(spec.onStatus(predicateCaptor.capture(), handlerCaptor.capture()))
                .thenReturn(spec);

        RestClientExceptionTranslator.applyDefaultOnStatusHandlers(spec, clientName);

        // Two handlers registered
        assertEquals(2, predicateCaptor.getAllValues().size());
        assertEquals(2, handlerCaptor.getAllValues().size());

        Predicate<HttpStatusCode> p4xx = predicateCaptor.getAllValues().get(0);
        Predicate<HttpStatusCode> p5xx = predicateCaptor.getAllValues().get(1);

        assertTrue(p4xx.test(HttpStatusCode.valueOf(404)));
        assertFalse(p4xx.test(HttpStatusCode.valueOf(500)));

        assertTrue(p5xx.test(HttpStatusCode.valueOf(500)));
        assertFalse(p5xx.test(HttpStatusCode.valueOf(404)));

        // Invoke handlers directly
        HttpRequest req = mock(HttpRequest.class);
        ClientHttpResponse res = mock(ClientHttpResponse.class);

        var handler4xx = handlerCaptor.getAllValues().get(0);
        DependencyCallException depException4xx =
                assertThrows(DependencyCallException.class, () -> handler4xx.handle(req, res));
        assertEquals(clientName, depException4xx.clientName());
        assertEquals(Reason.RESPONSE_CLIENT_ERROR, depException4xx.reason());

        var handler5xx = handlerCaptor.getAllValues().get(1);
        DependencyCallException depException5xx =
                assertThrows(DependencyCallException.class, () -> handler5xx.handle(req, res));
        assertEquals(clientName, depException5xx.clientName());
        assertEquals(Reason.RESPONSE_SERVER_ERROR, depException5xx.reason());
    }

}