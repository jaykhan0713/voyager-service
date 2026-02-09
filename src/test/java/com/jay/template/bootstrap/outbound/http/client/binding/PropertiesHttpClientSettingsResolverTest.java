package com.jay.template.bootstrap.outbound.http.client.binding;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.jay.template.bootstrap.outbound.http.properties.OutboundHttpProperties;
import com.jay.template.bootstrap.outbound.resiliency.properties.ResiliencyProperties;
import com.jay.template.core.outbound.http.client.settings.HttpClientSettings;
import com.jay.template.core.outbound.resiliency.policy.ResiliencyPolicy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class PropertiesHttpClientSettingsResolverTest {

    private static final OutboundHttpProperties PROPS = buildPropsWithClientAAndClientB();

    @Test
    void resolvesToEmptyListWhenNoMapExists() {
        var props =
                new OutboundHttpProperties(mock(OutboundHttpProperties.ClientDefaults.class), Collections.emptyMap());
        var resolver = new PropertiesHttpClientSettingsResolver(props);
        assertTrue(resolver.provide().isEmpty());
    }

    @Test
    void resolvesClientAUsingDefaults() {
        var resolver = new PropertiesHttpClientSettingsResolver(PROPS);
        List<HttpClientSettings> resolved = resolver.provide();

        HttpClientSettings a = getByClientName(resolved, "clientA");

        assertEquals("clientA", a.clientName());
        assertEquals("https://a.example.com", a.baseUrl());
        assertEquals(Duration.ofSeconds(2), a.connectTimeout());
        assertEquals(Duration.ofSeconds(3), a.readTimeout());

        ResiliencyPolicy aPolicy = a.resiliencyPolicy();
        assertTrue(aPolicy.bulkheadPolicy().enabled());
        assertEquals(10, aPolicy.bulkheadPolicy().maxConcurrentCalls());
        assertEquals(Duration.ZERO, aPolicy.bulkheadPolicy().maxWaitDuration());

        assertTrue(aPolicy.circuitBreakerPolicy().enabled());
        assertEquals(50, aPolicy.circuitBreakerPolicy().failureRateThreshold());
        assertEquals(Duration.ofMillis(200), aPolicy.circuitBreakerPolicy().slowCallDurationThreshold());
        assertEquals(75, aPolicy.circuitBreakerPolicy().slowCallRateThreshold());
        assertEquals(
                ResiliencyPolicy.CircuitBreakerPolicy.SlidingWindowType.COUNT_BASED,
                aPolicy.circuitBreakerPolicy().slidingWindowType()
        );
        assertEquals(100, aPolicy.circuitBreakerPolicy().slidingWindowSize());
        assertEquals(20, aPolicy.circuitBreakerPolicy().minimumNumberOfCalls());
        assertEquals(5, aPolicy.circuitBreakerPolicy().permittedNumberOfCallsInHalfOpenState());
        assertEquals(Duration.ofSeconds(10), aPolicy.circuitBreakerPolicy().waitDurationInOpenState());
    }

    @Test
    void resolvesClientBUsingOverridesAndDefaults() {
        var resolver = new PropertiesHttpClientSettingsResolver(PROPS);
        List<HttpClientSettings> resolved = resolver.provide();

        HttpClientSettings b = getByClientName(resolved, "clientB");

        assertEquals("clientB", b.clientName());
        assertEquals("https://b.example.com", b.baseUrl());
        assertEquals(Duration.ofSeconds(5), b.connectTimeout());
        assertEquals(Duration.ofSeconds(3), b.readTimeout()); // default

        ResiliencyPolicy bPolicy = b.resiliencyPolicy();

        assertTrue(bPolicy.bulkheadPolicy().enabled()); // default
        assertEquals(25, bPolicy.bulkheadPolicy().maxConcurrentCalls()); // override
        assertEquals(Duration.ZERO, bPolicy.bulkheadPolicy().maxWaitDuration()); // default

        assertTrue(bPolicy.circuitBreakerPolicy().enabled()); // default
        assertEquals(60, bPolicy.circuitBreakerPolicy().failureRateThreshold()); // override
        assertEquals(Duration.ofMillis(200), bPolicy.circuitBreakerPolicy().slowCallDurationThreshold()); // default
        assertEquals(75, bPolicy.circuitBreakerPolicy().slowCallRateThreshold()); // default
        assertEquals(
                ResiliencyPolicy.CircuitBreakerPolicy.SlidingWindowType.TIME_BASED,
                bPolicy.circuitBreakerPolicy().slidingWindowType()
        );
        assertEquals(100, bPolicy.circuitBreakerPolicy().slidingWindowSize()); // default
        assertEquals(20, bPolicy.circuitBreakerPolicy().minimumNumberOfCalls()); // default
        assertEquals(5, bPolicy.circuitBreakerPolicy().permittedNumberOfCallsInHalfOpenState()); // default
        assertEquals(Duration.ofSeconds(30), bPolicy.circuitBreakerPolicy().waitDurationInOpenState()); // override
    }

    @Test
    void returnsImmutableList() {
        var resolver = new PropertiesHttpClientSettingsResolver(PROPS);
        List<HttpClientSettings> resolved = resolver.provide();

        assertThrows(UnsupportedOperationException.class, () -> resolved.add(mock(HttpClientSettings.class)));
    }

    private static OutboundHttpProperties buildPropsWithClientAAndClientB() {

        var defaultBulkhead = new ResiliencyProperties.Bulkhead(
                true,
                10,
                Duration.ZERO
        );

        var defaultCircuitBreaker = new ResiliencyProperties.CircuitBreaker(
                true,
                50,
                Duration.ofMillis(200),
                75,
                ResiliencyProperties.CircuitBreaker.SlidingWindowType.COUNT_BASED,
                100,
                20,
                5,
                Duration.ofSeconds(10)
        );

        var resiliencyDefaults = new ResiliencyProperties(defaultBulkhead, defaultCircuitBreaker);

        var clientDefaults = new OutboundHttpProperties.ClientDefaults(
                Duration.ofSeconds(2),
                Duration.ofSeconds(3),
                resiliencyDefaults
        );

        var clientA = new OutboundHttpProperties.Client(
                "https://a.example.com",
                null,
                null,
                null
        );

        var overrideBulkhead = new ResiliencyProperties.Bulkhead(
                null,
                25,
                null
        );

        var overrideCircuitBreaker = new ResiliencyProperties.CircuitBreaker(
                null,
                60,
                null,
                null,
                ResiliencyProperties.CircuitBreaker.SlidingWindowType.TIME_BASED,
                null,
                null,
                null,
                Duration.ofSeconds(30)
        );

        var resiliencyOverride = new ResiliencyProperties(overrideBulkhead, overrideCircuitBreaker);

        var clientB = new OutboundHttpProperties.Client(
                "https://b.example.com",
                Duration.ofSeconds(5),
                null,
                resiliencyOverride
        );

        Map<String, OutboundHttpProperties.Client> clients = new LinkedHashMap<>();
        clients.put("clientA", clientA);
        clients.put("clientB", clientB);

        return new OutboundHttpProperties(clientDefaults, clients);
    }

    private static HttpClientSettings getByClientName(List<HttpClientSettings> resolved, String clientName) {

        for (HttpClientSettings settings : resolved) {
            if (clientName.equals(settings.clientName())) {
                return settings;
            }
        }
        throw new AssertionError("Expected client settings not found for clientName=" + clientName);
    }
}
