package com.jay.template.infra.concurrent.propagation.mdc;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import com.jay.template.infra.concurrent.propagation.ContextPropagator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MdcContextPropagatorTest {
    private static final String MDC_KEY = "mdc-key";
    private static final String PARENT = "parent";
    private static final String CHILD = "child";
    private static final String PREVIOUS = "previous";
    private static final String RESULT = "result";

    private final ContextPropagator propagator = new MdcContextPropagator();

    @BeforeEach
    void setUp() {
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void runnablePropagateAppliesCurrentToCaptured() {

        MDC.put(MDC_KEY, PARENT);
        AtomicReference<String> inside = new AtomicReference<>();
        Runnable propagated = propagator.propagate(() -> {
            inside.set(MDC.get(MDC_KEY));
            MDC.put(MDC_KEY, CHILD);
        });

        propagated.run();

        assertEquals(PARENT, inside.get());
        assertEquals(PARENT, MDC.get(MDC_KEY));
    }

    @Test
    void runnablePropagateCleansUp() {

        Runnable propagated = propagator.propagate(() -> MDC.put(MDC_KEY, CHILD));

        propagated.run();

        assertNull(MDC.getCopyOfContextMap());
    }

    @Test
    void runnablePropagateRestoresPreviousWhenCapturedIsNull() {

        Runnable propagated = propagator.propagate(() -> {
            assertNull(MDC.getCopyOfContextMap());
        });

        // Between propagate() and run(), something sets MDC:
        MDC.put(MDC_KEY, PREVIOUS);

        propagated.run();

        assertEquals(PREVIOUS, MDC.get(MDC_KEY));
    }

    @Test
    void callablePropagateAppliesCurrentToCaptured() throws Exception {

        MDC.put(MDC_KEY, PARENT);
        AtomicReference<String> inside = new AtomicReference<>();

        Callable<String> propagated = propagator.propagate(() -> {
            inside.set(MDC.get(MDC_KEY));
            MDC.put(MDC_KEY, CHILD);
            return RESULT;
        });

        String result = propagated.call();

        assertEquals(RESULT, result);
        assertEquals(PARENT, inside.get());
        assertEquals(PARENT, MDC.get(MDC_KEY));
    }



    @Test
    void callablePropagateCleansUp() throws Exception {
        Callable<String> propagated = propagator.propagate(() -> {
            MDC.put(MDC_KEY, CHILD);
            return RESULT;
        });

        String result = propagated.call();

        assertEquals(RESULT, result);
        assertNull(MDC.getCopyOfContextMap());
    }

    @Test
    void callablePropagateRestoresPreviousEvenIfCapturedIsNull() throws Exception {

        Callable<String> propagated = propagator.propagate(() -> {
            assertNull(MDC.getCopyOfContextMap());
            return RESULT;
        });

        // Between propagate() and run(), something sets MDC:
        MDC.put(MDC_KEY, PREVIOUS);

        String result = propagated.call();

        assertEquals(RESULT, result);
        assertEquals(PREVIOUS, MDC.get(MDC_KEY));
    }
}