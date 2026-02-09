package com.jay.template.bootstrap.outbound.http.client.binding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import com.jay.template.bootstrap.outbound.http.properties.OutboundHttpProperties;
import com.jay.template.core.port.outbound.http.client.HttpClientSettingsProvider;
import com.jay.template.core.outbound.http.client.settings.HttpClientSettings;
import com.jay.template.core.outbound.resiliency.policy.ResiliencyPolicy;
import com.jay.template.bootstrap.outbound.resiliency.properties.ResiliencyProperties;

@Component
public class PropertiesHttpClientSettingsResolver implements HttpClientSettingsProvider {

    private final List<HttpClientSettings> resolvedSettings;

    public PropertiesHttpClientSettingsResolver(OutboundHttpProperties props) {
        this.resolvedSettings = resolve(props);
    }

    @Override
    public List<HttpClientSettings> provide() {
        return resolvedSettings;
    }

    /*
     * Note that orDefaults usage at every level enables the settings to act as partial overrides as intended.
     */
    private static List<HttpClientSettings> resolve(OutboundHttpProperties props) {

        if (props.clients() == null || props.clients().isEmpty()) {
            return Collections.emptyList();
        }

        var propsClientMap = props.clients();

        var propsClientDefaults = props.clientDefaults();

        List<HttpClientSettings> settingsList = new ArrayList<>(propsClientMap.size());

        propsClientMap.forEach((propsClientName, propsClient) -> {

            //resiliency settings related mappings
            var propsResiliencyDefaults = propsClientDefaults.resiliency();
            var propsResiliency = propsClient.resiliencyOrDefault(propsClientDefaults);

            var bulkheadPolicy = mapBulkheadPolicy(propsResiliency, propsResiliencyDefaults);

            var circuitBreakerPolicy = mapCircuitBreakerPolicy(propsResiliency, propsResiliencyDefaults);

            var resiliencyPolicy =
                    new ResiliencyPolicy(bulkheadPolicy, circuitBreakerPolicy);

            HttpClientSettings resolvedClientSettings =
                    new HttpClientSettings(
                            propsClientName,
                            propsClient.baseUrl(),
                            propsClient.connectTimeoutOrDefault(propsClientDefaults),
                            propsClient.readTimeoutOrDefault(propsClientDefaults),
                            resiliencyPolicy
                    );

            settingsList.add(resolvedClientSettings);
        });

        return List.copyOf(settingsList);
    }

    private static ResiliencyPolicy.BulkheadPolicy mapBulkheadPolicy(
            ResiliencyProperties propsResiliency,
            ResiliencyProperties propsResiliencyDefaults
    ) {
        var propsBulkhead = propsResiliency.bulkheadOrDefault(propsResiliencyDefaults);
        var propsBulkheadDefaults = propsResiliencyDefaults.bulkhead();

        return new ResiliencyPolicy.BulkheadPolicy(
                propsBulkhead.enabledOrDefault(propsBulkheadDefaults),
                propsBulkhead.maxConcurrentCallsOrDefault(propsBulkheadDefaults),
                propsBulkhead.maxWaitDurationOrDefault(propsBulkheadDefaults)
        );
    }

    private static ResiliencyPolicy.CircuitBreakerPolicy mapCircuitBreakerPolicy(
            ResiliencyProperties propsResiliency,
            ResiliencyProperties propsResiliencyDefaults
    ) {
        var propsCircuitBreaker = propsResiliency.circuitBreakerOrDefault(propsResiliencyDefaults);
        var propsCircuitBreakerDefaults = propsResiliencyDefaults.circuitBreaker();

        return new ResiliencyPolicy.CircuitBreakerPolicy(
                propsCircuitBreaker.enabledOrDefault(propsCircuitBreakerDefaults),

                propsCircuitBreaker.failureRateThresholdOrDefault(propsCircuitBreakerDefaults),

                propsCircuitBreaker.slowCallDurationThresholdOrDefault(propsCircuitBreakerDefaults),
                propsCircuitBreaker.slowCallRateThresholdOrDefault(propsCircuitBreakerDefaults),

                mapSlidingWindowType(propsCircuitBreaker.slidingWindowTypeOrDefault(propsCircuitBreakerDefaults)),
                propsCircuitBreaker.slidingWindowSizeOrDefault(propsCircuitBreakerDefaults),
                propsCircuitBreaker.minimumNumberOfCallsOrDefault(propsCircuitBreakerDefaults),

                propsCircuitBreaker.permittedNumberOfCallsInHalfOpenStateOrDefault(propsCircuitBreakerDefaults),
                propsCircuitBreaker.waitDurationInOpenStateOrDefault(propsCircuitBreakerDefaults)
        );
    }

    private static ResiliencyPolicy.CircuitBreakerPolicy.SlidingWindowType mapSlidingWindowType(
            ResiliencyProperties.CircuitBreaker.SlidingWindowType propsType
    ) {
        return switch (propsType) {
            case COUNT_BASED -> ResiliencyPolicy.CircuitBreakerPolicy.SlidingWindowType.COUNT_BASED;
            case TIME_BASED -> ResiliencyPolicy.CircuitBreakerPolicy.SlidingWindowType.TIME_BASED;
        };
    }

}
