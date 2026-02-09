package com.jay.template.core.context.identity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

class IdentityContextSnapshotTest {

    @Test
    void empty() {
        IdentityContextSnapshot snapshot = IdentityContextSnapshot.EMPTY;
        assertSame(IdentityContextSnapshot.EMPTY, snapshot);
    }

    @Test
    void ofNullIdentityReturnsEmpty() {
        IdentityContextSnapshot snapshot = IdentityContextSnapshot.of(null);
        assertSame(IdentityContextSnapshot.EMPTY, snapshot);
    }

    @Test
    void ofEmptyIdentityReturnsEmpty() {
        Identity emptyIdentity = new Identity("", "");
        IdentityContextSnapshot snapshot = IdentityContextSnapshot.of(emptyIdentity);
        assertSame(IdentityContextSnapshot.EMPTY, snapshot);
    }

    @Test
    void ofIdentityReturnsNewSnapshotWithSameIdentityValues() {
        Identity identity = new Identity("user-001", "req-001");

        IdentityContextSnapshot s1 = IdentityContextSnapshot.of(identity);
        IdentityContextSnapshot s2 = IdentityContextSnapshot.of(identity);

        assertNotSame(s1, s2);
        assertEquals(identity, s1.identity());
    }
}