package com.jay.voyager.core.port.outbound.http.client;

import java.util.List;

import com.jay.voyager.core.outbound.http.client.settings.HttpClientSettings;

public interface HttpClientSettingsProvider {

    List<HttpClientSettings> provide();
}
