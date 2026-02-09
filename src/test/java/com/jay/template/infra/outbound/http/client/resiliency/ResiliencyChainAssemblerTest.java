package com.jay.template.infra.outbound.http.client.resiliency;

import java.io.IOException;
import java.time.Duration;
import java.util.function.Predicate;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;

import com.jay.template.core.outbound.resiliency.policy.ResiliencyPolicy;
import com.jay.template.infra.outbound.http.client.resiliency.bulkhead.BulkheadRequestDecoratorFactory;
import com.jay.template.infra.outbound.http.client.resiliency.circuitbreaker.CircuitBreakerRequestDecoratorFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ResiliencyChainAssemblerTest {

    @Test
    void assembleAppliesCircuitBreakerThenBulkhead() {
        var bulkheadRegistry = mock(BulkheadRegistry.class);
        var circuitBreakerRegistry = mock(CircuitBreakerRegistry.class);

        var requestFactory = mock(ClientHttpRequestFactory.class);

        var resiliencyPolicy = mock(ResiliencyPolicy.class);
        var bulkheadPolicy = mock(ResiliencyPolicy.BulkheadPolicy.class);
        var circuitBreakerPolicy = mock(ResiliencyPolicy.CircuitBreakerPolicy.class);

        when(resiliencyPolicy.bulkheadPolicy()).thenReturn(bulkheadPolicy);
        when(resiliencyPolicy.circuitBreakerPolicy()).thenReturn(circuitBreakerPolicy);

        when(bulkheadPolicy.enabled()).thenReturn(true);
        when(bulkheadPolicy.maxConcurrentCalls()).thenReturn(1);
        when(bulkheadPolicy.maxWaitDuration()).thenReturn(Duration.ZERO);

        when(circuitBreakerPolicy.enabled()).thenReturn(true);
        when(circuitBreakerPolicy.failureRateThreshold()).thenReturn(50);
        when(circuitBreakerPolicy.slowCallDurationThreshold()).thenReturn(Duration.ofSeconds(1));
        when(circuitBreakerPolicy.slowCallRateThreshold()).thenReturn(50);
        when(circuitBreakerPolicy.slidingWindowType())
                .thenReturn(ResiliencyPolicy.CircuitBreakerPolicy.SlidingWindowType.COUNT_BASED);
        when(circuitBreakerPolicy.slidingWindowSize()).thenReturn(10);
        when(circuitBreakerPolicy.minimumNumberOfCalls()).thenReturn(10);
        when(circuitBreakerPolicy.permittedNumberOfCallsInHalfOpenState()).thenReturn(5);
        when(circuitBreakerPolicy.waitDurationInOpenState()).thenReturn(Duration.ofSeconds(5));

        when(circuitBreakerRegistry.circuitBreaker(anyString(), any(CircuitBreakerConfig.class)))
                .thenReturn(mock(CircuitBreaker.class));
        when(bulkheadRegistry.bulkhead(anyString(), any(BulkheadConfig.class)))
                .thenReturn(mock(Bulkhead.class));

        var assembler = new ResiliencyChainAssembler(bulkheadRegistry, circuitBreakerRegistry);

        assembler.assemble(requestFactory, resiliencyPolicy, "clientA");

        var inOrder = inOrder(circuitBreakerRegistry, bulkheadRegistry);
        inOrder.verify(circuitBreakerRegistry)
                .circuitBreaker(eq("clientAOutboundClient"), any(CircuitBreakerConfig.class));
        inOrder.verify(bulkheadRegistry)
                .bulkhead(eq("clientAOutboundClient"), any(BulkheadConfig.class));
    }

    @Test
    void applyCircuitBreakerWhenDisabledReturnsSameDelegate() {
        var bulkheadRegistry = mock(BulkheadRegistry.class);
        var circuitBreakerRegistry = mock(CircuitBreakerRegistry.class);

        var delegate = mock(ClientHttpRequestFactory.class);

        var circuitBreakerPolicy = mock(ResiliencyPolicy.CircuitBreakerPolicy.class);
        when(circuitBreakerPolicy.enabled()).thenReturn(false);

        var assembler = new ResiliencyChainAssembler(bulkheadRegistry, circuitBreakerRegistry);

        var result =
                assembler.applyCircuitBreaker(delegate, circuitBreakerPolicy, "clientAOutboundClient");

        assertSame(delegate, result);
        verifyNoInteractions(circuitBreakerRegistry);
    }

    @Test
    void applyCircuitBreakerWhenEnabledDecoratesRequestFactory() {
        var bulkheadRegistry = mock(BulkheadRegistry.class);
        var circuitBreakerRegistry = mock(CircuitBreakerRegistry.class);

        var delegate = mock(ClientHttpRequestFactory.class);

        var circuitBreakerPolicy = mock(ResiliencyPolicy.CircuitBreakerPolicy.class);
        when(circuitBreakerPolicy.enabled()).thenReturn(true);
        when(circuitBreakerPolicy.failureRateThreshold()).thenReturn(50);
        when(circuitBreakerPolicy.slowCallDurationThreshold()).thenReturn(Duration.ofSeconds(1));
        when(circuitBreakerPolicy.slowCallRateThreshold()).thenReturn(50);
        when(circuitBreakerPolicy.slidingWindowType())
                .thenReturn(ResiliencyPolicy.CircuitBreakerPolicy.SlidingWindowType.TIME_BASED);
        when(circuitBreakerPolicy.slidingWindowSize()).thenReturn(10);
        when(circuitBreakerPolicy.minimumNumberOfCalls()).thenReturn(10);
        when(circuitBreakerPolicy.permittedNumberOfCallsInHalfOpenState()).thenReturn(5);
        when(circuitBreakerPolicy.waitDurationInOpenState()).thenReturn(Duration.ofSeconds(5));

        when(circuitBreakerRegistry.circuitBreaker(anyString(), any(CircuitBreakerConfig.class)))
                .thenReturn(mock(CircuitBreaker.class));

        var assembler = new ResiliencyChainAssembler(bulkheadRegistry, circuitBreakerRegistry);

        var result =
                assembler.applyCircuitBreaker(delegate, circuitBreakerPolicy, "clientAOutboundClient");

        assertInstanceOf(CircuitBreakerRequestDecoratorFactory.class, result);
        verify(circuitBreakerRegistry)
                .circuitBreaker(eq("clientAOutboundClient"), any(CircuitBreakerConfig.class));
    }

    @Test
    void applyBulkheadWhenDisabledReturnsSameDelegate() {
        var bulkheadRegistry = mock(BulkheadRegistry.class);
        var circuitBreakerRegistry = mock(CircuitBreakerRegistry.class);

        var delegate = mock(ClientHttpRequestFactory.class);

        var bulkheadPolicy = mock(ResiliencyPolicy.BulkheadPolicy.class);
        when(bulkheadPolicy.enabled()).thenReturn(false);

        var assembler = new ResiliencyChainAssembler(bulkheadRegistry, circuitBreakerRegistry);

        var result =
                assembler.applyBulkhead(delegate, bulkheadPolicy, "clientAOutboundClient");

        assertSame(delegate, result);
        verifyNoInteractions(bulkheadRegistry);
    }

    @Test
    void applyBulkheadWhenEnabledDecoratesRequestFactory() {
        var bulkheadRegistry = mock(BulkheadRegistry.class);
        var circuitBreakerRegistry = mock(CircuitBreakerRegistry.class);

        var delegate = mock(ClientHttpRequestFactory.class);

        var bulkheadPolicy = mock(ResiliencyPolicy.BulkheadPolicy.class);
        when(bulkheadPolicy.enabled()).thenReturn(true);
        when(bulkheadPolicy.maxConcurrentCalls()).thenReturn(3);
        when(bulkheadPolicy.maxWaitDuration()).thenReturn(Duration.ZERO);

        when(bulkheadRegistry.bulkhead(anyString(), any(BulkheadConfig.class)))
                .thenReturn(mock(Bulkhead.class));

        var assembler = new ResiliencyChainAssembler(bulkheadRegistry, circuitBreakerRegistry);

        var result =
                assembler.applyBulkhead(delegate, bulkheadPolicy, "clientAOutboundClient");

        assertInstanceOf(BulkheadRequestDecoratorFactory.class, result);
        verify(bulkheadRegistry)
                .bulkhead(eq("clientAOutboundClient"), any(BulkheadConfig.class));
    }

    @Test
    void applyBulkheadWhenEnabledMapsPolicyIntoBulkheadConfig() {
        var bulkheadRegistry = mock(BulkheadRegistry.class);
        var circuitBreakerRegistry = mock(CircuitBreakerRegistry.class);

        var delegate = mock(ClientHttpRequestFactory.class);

        var bulkheadPolicy = mock(ResiliencyPolicy.BulkheadPolicy.class);
        when(bulkheadPolicy.enabled()).thenReturn(true);
        when(bulkheadPolicy.maxConcurrentCalls()).thenReturn(12);
        when(bulkheadPolicy.maxWaitDuration()).thenReturn(Duration.ofSeconds(2));

        var configCaptor = ArgumentCaptor.forClass(BulkheadConfig.class);
        when(bulkheadRegistry.bulkhead(eq("clientAOutboundClient"), configCaptor.capture()))
                .thenReturn(mock(Bulkhead.class));

        var assembler = new ResiliencyChainAssembler(bulkheadRegistry, circuitBreakerRegistry);

        assembler.applyBulkhead(delegate, bulkheadPolicy, "clientAOutboundClient");

        BulkheadConfig cfg = configCaptor.getValue();
        assertEquals(12, cfg.getMaxConcurrentCalls());
        assertEquals(Duration.ofSeconds(2), cfg.getMaxWaitDuration());
    }

    @Test
    void applyCircuitBreakerWhenEnabledMapsPolicyAndRecords5xxAsFailure() throws IOException {
        var bulkheadRegistry = mock(BulkheadRegistry.class);
        var circuitBreakerRegistry = mock(CircuitBreakerRegistry.class);

        var delegate = mock(ClientHttpRequestFactory.class);

        var circuitBreakerPolicy = mock(ResiliencyPolicy.CircuitBreakerPolicy.class);
        when(circuitBreakerPolicy.enabled()).thenReturn(true);

        when(circuitBreakerPolicy.failureRateThreshold()).thenReturn(42);
        when(circuitBreakerPolicy.slowCallDurationThreshold()).thenReturn(Duration.ofSeconds(3));
        when(circuitBreakerPolicy.slowCallRateThreshold()).thenReturn(21);

        when(circuitBreakerPolicy.slidingWindowType())
                .thenReturn(ResiliencyPolicy.CircuitBreakerPolicy.SlidingWindowType.TIME_BASED);
        when(circuitBreakerPolicy.slidingWindowSize()).thenReturn(60);
        when(circuitBreakerPolicy.minimumNumberOfCalls()).thenReturn(10);

        when(circuitBreakerPolicy.permittedNumberOfCallsInHalfOpenState()).thenReturn(7);
        when(circuitBreakerPolicy.waitDurationInOpenState()).thenReturn(Duration.ofSeconds(9));

        var configCaptor = ArgumentCaptor.forClass(CircuitBreakerConfig.class);
        when(circuitBreakerRegistry.circuitBreaker(eq("clientAOutboundClient"), configCaptor.capture()))
                .thenReturn(mock(CircuitBreaker.class));

        var assembler = new ResiliencyChainAssembler(bulkheadRegistry, circuitBreakerRegistry);

        assembler.applyCircuitBreaker(delegate, circuitBreakerPolicy, "clientAOutboundClient");

        CircuitBreakerConfig cfg = configCaptor.getValue();

        assertEquals(42.0f, cfg.getFailureRateThreshold());
        assertEquals(Duration.ofSeconds(3), cfg.getSlowCallDurationThreshold());
        assertEquals(21.0f, cfg.getSlowCallRateThreshold());
        assertEquals(CircuitBreakerConfig.SlidingWindowType.TIME_BASED, cfg.getSlidingWindowType());
        assertEquals(60, cfg.getSlidingWindowSize());
        assertEquals(10, cfg.getMinimumNumberOfCalls());
        assertEquals(7, cfg.getPermittedNumberOfCallsInHalfOpenState());

        Predicate<Object> recordResult = cfg.getRecordResultPredicate();
        assertNotNull(recordResult);

        ClientHttpResponse resp5xx = mock(ClientHttpResponse.class);
        when(resp5xx.getStatusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);
        assertTrue(recordResult.test(resp5xx));

        ClientHttpResponse resp4xx = mock(ClientHttpResponse.class);
        when(resp4xx.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
        assertFalse(recordResult.test(resp4xx));

        ClientHttpResponse respThrows = mock(ClientHttpResponse.class);
        when(respThrows.getStatusCode()).thenThrow(new IOException("IO exception"));
        assertFalse(recordResult.test(respThrows));

        assertFalse(recordResult.test("not a response"));
    }
}
