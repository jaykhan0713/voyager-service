package com.jay.template.core.port.outbound.http.client;

import java.util.List;

import com.jay.template.core.outbound.http.client.settings.HttpClientSettings;

public interface HttpClientSettingsProvider {

    List<HttpClientSettings> provide();
}
