package com.jay.template.core.context.identity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IdentityTest {

    @Test
    void isEmptyWhenAllFieldsAreBlank() {
        Identity identity = new Identity("", "   ");
        assertTrue(identity.isEmpty());
    }

    @Test
    void isEmptyWhenAllFieldsAreNull() {
        Identity identity = new Identity(null, null);
        assertTrue(identity.isEmpty());
    }

    @Test
    void isNotEmptyWhenAnyFieldIsNonBlank() {
        Identity identity1 = new Identity("user-001", "");
        Identity identity2 = new Identity("", "req-002");
        Identity identity3 = new Identity("user-003", "req-003");

        assertFalse(identity1.isEmpty());
        assertFalse(identity2.isEmpty());
        assertFalse(identity3.isEmpty());
    }

    @Test
    void normalizesNullToEmptyStrings() {
        Identity identity = new Identity(null, "req");
        assertEquals("", identity.userId());
    }
}