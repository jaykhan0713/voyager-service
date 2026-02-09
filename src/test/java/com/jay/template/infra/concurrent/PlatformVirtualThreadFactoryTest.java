package com.jay.template.infra.concurrent;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import com.jay.template.infra.concurrent.propagation.mdc.MdcContextPropagator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlatformVirtualThreadFactoryTest {

    @BeforeEach
    void setUp() {
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void newThreadIsVirtual() {
        final ThreadFactory factory = new PlatformVirtualThreadFactory(Collections.emptyList());
        Thread thread = factory.newThread(() -> {});
        assertTrue(thread.isVirtual());
    }

    @Test
    void newThreadSeesParentMdc() throws InterruptedException {

        final ThreadFactory factory = new PlatformVirtualThreadFactory(List.of(new MdcContextPropagator()));

        MDC.put("mdc-key", "parent");
        AtomicReference<String> inside = new AtomicReference<>();

        Thread thread = factory.newThread(() -> inside.set(MDC.get("mdc-key")));
        thread.start();
        thread.join();

        assertEquals("parent", inside.get());
    }

    @Test
    void newThreadDoesNotPolluteParentMdc() throws InterruptedException {
        final ThreadFactory factory = new PlatformVirtualThreadFactory(List.of(new MdcContextPropagator()));

        MDC.put("mdc-key", "parent");

        Thread thread = factory.newThread(() -> MDC.put("mdc-key", "child"));
        thread.start();
        thread.join();

        assertEquals("parent", MDC.get("mdc-key"));
    }
}