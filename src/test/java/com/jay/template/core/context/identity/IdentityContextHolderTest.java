package com.jay.template.core.context.identity;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

class IdentityContextHolderTest {

    @BeforeEach
    void setUp() {
        IdentityContextHolder.clear();
    }

    @AfterEach
    void tearDown() {
        IdentityContextHolder.clear();
    }

    @Test
    void setContextNullClearsAndGetReturnsEmpty() {
        IdentityContextHolder.context(null);
        IdentityContextSnapshot context = IdentityContextHolder.context();
        assertSame(IdentityContextSnapshot.EMPTY, context);
    }

    @Test
    void setContextEmptyAndGetReturnsEmpty() {
        IdentityContextHolder.context(IdentityContextSnapshot.EMPTY);
        IdentityContextSnapshot context = IdentityContextHolder.context();
        assertSame(IdentityContextSnapshot.EMPTY, context);
    }

    @Test
    void setContextAndGetReturnsCopyOfContext() {
        Identity identity = new Identity("user-001", "req-001");
        IdentityContextSnapshot snapshot = IdentityContextSnapshot.of(identity);
        IdentityContextHolder.context(snapshot);

        IdentityContextSnapshot context = IdentityContextHolder.context();
        assertNotSame(IdentityContextSnapshot.EMPTY, context);
        assertNotSame(snapshot, context); //getContext returns a copy of snapshot
        assertEquals(identity, context.identity());
    }
}