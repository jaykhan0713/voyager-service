package com.jay.template.infra.outbound.http.client.registry;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

import com.jay.template.core.outbound.http.client.settings.HttpClientSettings;
import com.jay.template.core.port.outbound.http.client.HttpClientSettingsProvider;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HttpClientSettingsRegistryTest {

    @Test
    void buildsRegistryMapAndFreezes() {
        var provider = mock(HttpClientSettingsProvider.class);
        String clientName = "someClient";
        HttpClientSettings clientSettings = mock(HttpClientSettings.class);
        when(clientSettings.clientName()).thenReturn(clientName);
        when(provider.provide()).thenReturn(List.of(clientSettings));

        var registry = new HttpClientSettingsRegistry(provider);
        assertEquals(1, registry.clientNames().size());

        assertSame(clientSettings, registry.httpClientSettings(clientName));

        var clientNames = registry.clientNames();
        assertThrows(UnsupportedOperationException.class, () ->
                clientNames.add("newClient")
        );
    }

    @Test
    void whenClientDoesNotExistThrowsException() {
        var provider = mock(HttpClientSettingsProvider.class);
        when(provider.provide()).thenReturn(Collections.emptyList());
        var registry = new HttpClientSettingsRegistry(provider);

        var ex = assertThrows(NoSuchElementException.class, () ->
                registry.httpClientSettings("someClientThatDoesNotExist")
        );

        assertTrue(ex.getMessage().contains("someClientThatDoesNotExist"));
    }

    @Test
    void whenProvidedDuplicateClientThrowsException() {
        var provider = mock(HttpClientSettingsProvider.class);
        HttpClientSettings clientSettingsA = mock(HttpClientSettings.class);
        HttpClientSettings clientSettingsB = mock(HttpClientSettings.class);
        when(clientSettingsA.clientName()).thenReturn("clientA");
        when(clientSettingsB.clientName()).thenReturn("clientA");
        when(provider.provide()).thenReturn(List.of(clientSettingsA, clientSettingsB));

        var ex = assertThrows(IllegalStateException.class, () ->
                new HttpClientSettingsRegistry(provider)
        );

        assertTrue(ex.getMessage().contains("Duplicate HttpClientSettings"));
        assertTrue(ex.getMessage().contains("clientA"));
    }
}