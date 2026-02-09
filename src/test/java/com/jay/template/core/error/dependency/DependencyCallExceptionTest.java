package com.jay.template.core.error.dependency;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DependencyCallExceptionTest {

    private static final String CLIENT_NAME = "someClient";

    @Test
    void ctorRequiresClientNameAndReason() {
        assertThrows(NullPointerException.class, () -> new DependencyCallException(CLIENT_NAME, null));
        assertThrows(NullPointerException.class, () -> new DependencyCallException(null, Reason.IO_ERROR));
    }

    @Test
    void ctorWithClientNameAndReason() {
        DependencyCallException ex = new DependencyCallException(CLIENT_NAME, Reason.IO_ERROR);
        assertEquals(CLIENT_NAME, ex.clientName());
        assertSame(Reason.IO_ERROR, ex.reason());
        assertNull(ex.getCause());
    }

    @Test
    void ctorWithClientNameAndReasonAndCause() {
        RuntimeException root = new RuntimeException();
        DependencyCallException ex = new DependencyCallException(CLIENT_NAME, Reason.IO_ERROR, root);
        assertEquals(CLIENT_NAME, ex.clientName());
        assertSame(Reason.IO_ERROR, ex.reason());
        assertSame(root, ex.getCause());
    }
}