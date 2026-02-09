package com.jay.template.infra.concurrent.propagation.identity;

import java.util.concurrent.Callable;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.jay.template.core.context.identity.Identity;
import com.jay.template.core.context.identity.IdentityContextHolder;
import com.jay.template.core.context.identity.IdentityContextSnapshot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class IdentityContextPropagatorTest {

    private static final String PARENT_USER = "parent-user";
    private static final String PARENT_REQUEST = "parent-identity";
    private static final String CHILD_USER = "child-user";
    private static final String CHILD_REQUEST = "child-identity";
    private static final String PREVIOUS_USER = "previous-user";
    private static final String PREVIOUS_REQUEST = "previous-identity";

    private final IdentityContextPropagator propagator = new IdentityContextPropagator();

    @BeforeEach
    void setUp() {
        IdentityContextHolder.clear();
    }

    @AfterEach
    void tearDown() {
        IdentityContextHolder.clear();
    }

    @Test
    void runnablePropagateAppliesCurrentToCaptured() {
        IdentityContextHolder.context(
                IdentityContextSnapshot.of(new Identity(PARENT_USER, PARENT_REQUEST)));
        IdentityContextSnapshot captured = IdentityContextHolder.context();

        Runnable propagated = propagator.propagate(() -> {
            IdentityContextSnapshot inside = IdentityContextHolder.context();
            assertEquals(captured, inside);
            IdentityContextHolder.context(
                    IdentityContextSnapshot.of(new Identity(CHILD_USER, CHILD_REQUEST)));
        });

        propagated.run();

        assertEquals(captured, IdentityContextHolder.context());
    }

    @Test
    void runnablePropagateCleansUp() {
        Runnable propagated = propagator.propagate(() ->
            IdentityContextHolder.context(
                    IdentityContextSnapshot.of(new Identity(CHILD_USER, CHILD_REQUEST)))
        );

        propagated.run();

        assertSame(IdentityContextSnapshot.EMPTY, IdentityContextHolder.context());
    }

    @Test
    void runnablePropagateRestoresPreviousWhenCapturedIsEmpty() {

        Runnable propagated = propagator.propagate(() -> {
            assertSame(IdentityContextSnapshot.EMPTY, IdentityContextHolder.context()); // Captured propagation is empty
        });

        // Between propagate() and run(), something sets new snapshot:
        IdentityContextSnapshot previous = IdentityContextSnapshot.of(new Identity(PREVIOUS_USER, PREVIOUS_REQUEST));
        IdentityContextHolder.context(previous);

        propagated.run();

        assertEquals(previous, IdentityContextHolder.context());
    }

    @Test
    void callablePropagateAppliesCurrentToCaptured() throws Exception {
        IdentityContextHolder.context(
                IdentityContextSnapshot.of(new Identity(PARENT_USER, PARENT_REQUEST)));
        IdentityContextSnapshot captured = IdentityContextHolder.context();

        Callable<Identity> propagated = propagator.propagate(() -> {
            IdentityContextSnapshot inside = IdentityContextHolder.context();
            Identity id = inside.identity();
            assertEquals(captured, inside);
            IdentityContextHolder.context(
                    IdentityContextSnapshot.of(new Identity(CHILD_USER, CHILD_REQUEST)));
            return id;
        });

        Identity result = propagated.call();

        assertEquals(captured.identity(), result);
        assertEquals(captured, IdentityContextHolder.context());
    }

    @Test
    void callablePropagateCleansUp() throws Exception {
        Callable<Void> propagated = propagator.propagate(() -> {
                IdentityContextHolder.context(
                        IdentityContextSnapshot.of(new Identity(CHILD_USER, CHILD_REQUEST)));
                return null;
            }
        );

        propagated.call();

        assertSame(IdentityContextSnapshot.EMPTY, IdentityContextHolder.context());
    }

    @Test
    void callablePropagateRestoresPreviousWhenCapturedIsEmpty() throws Exception {

        Callable<Void> propagated = propagator.propagate(() -> {
            assertSame(IdentityContextSnapshot.EMPTY, IdentityContextHolder.context()); // Captured propagation is empty
            return null;
        });

        // Between propagate() and run(), something sets new snapshot:
        IdentityContextSnapshot previous = IdentityContextSnapshot.of(new Identity(PREVIOUS_USER, PREVIOUS_REQUEST));
        IdentityContextHolder.context(previous);

        propagated.call();

        assertEquals(previous, IdentityContextHolder.context());
    }
}