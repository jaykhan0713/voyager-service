package com.jay.template.core.context.identity;

/**
 * Thread-bound holder for identity propagation.
 *
 * <p>
 * {@code IdentityContextHolder} manages identity-scoped identity using a {@link ThreadLocal}.
 * It is owned by infrastructure code and is responsible for binding identity at identity
 * entry and clearing it at identity completion.
 * FUTURE-NOTE: In the future this structure will be revisited to see if we should move thread local handling to Infra
 *  and inject non-infra layers (or at least business layer) with a bean containing Identity for the current context
 * </p>
 *
 * <p>
 * Callers interact only with {@link IdentityContextSnapshot}, which represents an
 * immutable, point-in-time view of identity.
 * </p>
 *
 * <p>
 * {@link #context()} never returns {@code null}. If no identity propagation is currently
 * bound to the thread, {@link IdentityContextSnapshot#EMPTY} is returned.
 * </p>
 *
 * <p>
 * {@link #clear()} must be invoked in a {@code finally} block to avoid leaking identity
 * state across thread reuse.
 * </p>
 */
public final class IdentityContextHolder {

    private static final ThreadLocal<IdentityContextSnapshot> LOCAL = new ThreadLocal<>();

    private IdentityContextHolder() {}

    /**
     * Returns a snapshot of the current identity propagation.
     *
     * <p>
     * The returned snapshot is normalized and detached from the underlying thread-bound
     * state. Changes to propagation in other error paths do not affect the returned
     * instance.
     * </p>
     *
     * @return a non-null snapshot of the currentidentity propagation
     */
    public static IdentityContextSnapshot context() {
        IdentityContextSnapshot ctx = LOCAL.get();
        if (ctx == null) {
            return IdentityContextSnapshot.EMPTY;
        }
        return IdentityContextSnapshot.of(ctx.identity());
    }

    /**
     * Binds the provided identity propagation to the current thread.
     *
     * <p>
     * The provided snapshot is normalized before being bound.
     * Passing {@code null} clears the current propagation.
     * Passing {@link IdentityContextSnapshot#EMPTY} binds an empty propagation
     * </p>
     *
     * @param snapshot snapshot representing the identity propagation to bind
     */
    public static void context(IdentityContextSnapshot snapshot) {
        if (snapshot == null) {
            clear();
            return;
        }
        LOCAL.set(IdentityContextSnapshot.of(snapshot.identity()));
    }

    /**
     * Clears the identity propagation from the current thread.
     */
    public static void clear() {
        LOCAL.remove();
    }
}
