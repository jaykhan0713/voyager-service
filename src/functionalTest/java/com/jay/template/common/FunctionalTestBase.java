package com.jay.template.common;

import java.io.IOException;

import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

public abstract class FunctionalTestBase {

    // To ensure test speed in CI, only spin up mockwebServer once.
    protected static MockWebServer mockWebServer;

    private static synchronized void ensureStarted() throws IOException {
        if (mockWebServer != null) return;

        mockWebServer = new MockWebServer();
        mockWebServer.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // shut down on JVM exit only.
            try { mockWebServer.shutdown(); } catch (Exception _) { /* BLANK */ }
        }));
    }

    @BeforeAll
    static void beforeAllTestsInThisContext() throws IOException {
        ensureStarted();
    }

    /*
     * DynamicPropertySource makes it so a new app instance would have to be started for the test class
     * so moved it to shared Base
     */
    @DynamicPropertySource
    static void outboundProps(DynamicPropertyRegistry registry) throws IOException {
        ensureStarted();
        registry.add("platform.outbound.http.clients.ping.base-url",
                () -> mockWebServer.url("/").toString());
    }
}
