package com.jay.template.infra.outbound.http.client.registry;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.jay.template.core.outbound.http.client.settings.HttpClientSettings;
import com.jay.template.core.port.outbound.http.client.HttpClientSettingsProvider;

//takes resolved core HttpClientSettings model and creates a stateful registry to be accessed by client.
public class HttpClientSettingsRegistry {
    private final Map<String, HttpClientSettings> httpClientSettingsMap;

    public HttpClientSettingsRegistry(HttpClientSettingsProvider provider) {
        httpClientSettingsMap = buildRegistryMap(provider);
    }

    public HttpClientSettings httpClientSettings(String clientName) {
        HttpClientSettings settings = httpClientSettingsMap.get(clientName);

        if (settings == null) {
            throw new NoSuchElementException("No such http client: '" + clientName + "'");
        }

        return settings;
    }

    public Set<String> clientNames() {
        return httpClientSettingsMap.keySet();
    }

    private static Map<String, HttpClientSettings> buildRegistryMap(
            HttpClientSettingsProvider provider
    ) {
        return provider.provide()
                .stream()
                .collect(
                        Collectors
                                .toUnmodifiableMap(
                                        HttpClientSettings::clientName,
                                        Function.identity(), // same as (a) -> a
                                        (a, b) -> {
                                            throw new IllegalStateException(
                                                    "Duplicate HttpClientSettings for clientName: " + a.clientName()
                                            );
                                        }
                                )
                );
    }
}
