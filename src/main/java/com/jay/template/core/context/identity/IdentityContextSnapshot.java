package com.jay.template.core.context.identity;

/**
 * Snapshot of identity-scoped identity propagation.
 *
 * <p>
 * {@code IdentityContextSnapshot} represents a point-in-time view of identity
 * that can be safely passed across layers and thread boundaries.
 * </p>
 *
 * <p>
 * Snapshots are treated as immutable value objects. Snapshot instances may be copied on
 * retrieval and binding to prevent accidental coupling between error paths. The
 * contained {@link Identity} is reused directly since it is currently immutable. If
 * {@link Identity} later contains mutable state, this implementation may defensively
 * copy identity data at this boundary to preserve snapshot isolation.
 * </p>
 *
 * <p>
 * An empty identity propagation is represented exclusively by the {@link #EMPTY} sentinel
 * instance. Callers must not assume value equality implies emptiness; reference
 * equality against {@link #EMPTY} defines the absence of identity.
 * </p>
 *
 * <p>
 * To modify identity propagation, a new snapshot must be created and bound via
 * {@link IdentityContextHolder#context(IdentityContextSnapshot)}.
 * </p>
 *
 * @param identity immutable identity associated with the identity
 */
public record IdentityContextSnapshot(Identity identity) {

    /**
     * Sentinel instance representing the absence of identity.
     *
     * <p>
     * This instance is used to model an unbound or empty identity propagation and must be
     * treated as a singleton. Reference equality checks against this instance are
     * intentional and relied upon by infrastructure code.
     * </p>
     */
    public static final IdentityContextSnapshot EMPTY =
            new IdentityContextSnapshot(new Identity("", ""));

    /**
     * Creates a normalized {@link IdentityContextSnapshot} from the provided identity.
     *
     * <p>
     * This factory enforces the invariant that a missing or empty identity is always
     * represented by the {@link #EMPTY} sentinel instance.
     * </p>
     *
     * <p>
     * Normalization rules:
     * <ul>
     *   <li>If {@code identity} is {@code null}, {@link #EMPTY} is returned.</li>
     *   <li>If {@code identity} is empty according to {@link Identity#isEmpty()},
     *       {@link #EMPTY} is returned.</li>
     *   <li>Otherwise, a new snapshot wrapping the provided identity is created.</li>
     * </ul>
     * </p>
     *
     * <p>
     * Callers should prefer this factory over direct construction to ensure sentinel
     * semantics are preserved and reference equality checks against {@link #EMPTY}
     * remain reliable.
     * </p>
     *
     * @param identity the identity to wrap, may be {@code null}
     * @return a normalized snapshot or {@link #EMPTY} if no identity is present
     */
    public static IdentityContextSnapshot of(Identity identity) {
        if (identity == null || identity.isEmpty()) {
            return EMPTY;
        }
        return new IdentityContextSnapshot(identity);
    }
}
