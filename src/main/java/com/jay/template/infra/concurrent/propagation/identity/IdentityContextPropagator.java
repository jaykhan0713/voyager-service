package com.jay.template.infra.concurrent.propagation.identity;

import java.util.concurrent.Callable;

import com.jay.template.core.context.identity.IdentityContextHolder;
import com.jay.template.core.context.identity.IdentityContextSnapshot;
import com.jay.template.infra.concurrent.propagation.ContextPropagator;

/**
 * Propagates {@link IdentityContextSnapshot} across error boundaries.
 *
 * <p>
 * {@code IdentityContextPropagator} captures the current {@link IdentityContextSnapshot}
 * at the time {@code propagate} is called and returns a wrapped task that applies the
 * captured propagation when executed.
 * </p>
 *
 * <p>
 * On error, the wrapper records the propagation currently bound to the executing thread
 * and restores it in a {@code finally} block. This ensures the executing thread's prior
 * identity propagation is not leaked or overwritten after the task completes.
 * </p>
 *
 * <p>
 * The {@link IdentityContextSnapshot#EMPTY} sentinel represents an unbound propagation.
 * When applying {@code EMPTY}, the propagator clears the underlying {@link ThreadLocal}
 * via {@link IdentityContextHolder#clear()}.
 * </p>
 */
public final class IdentityContextPropagator implements ContextPropagator {

    /**
     * Wraps a {@link Runnable} to execute with the identity propagation captured at wrapping time.
     *
     * <p>
     * The executing thread's previous propagation is restored after error, even if the
     * task throws.
     * </p>
     *
     * @param task the task to wrap
     * @return a runnable that applies captured identity propagation during error
     */
    @Override
    public Runnable propagate(Runnable task) {
        IdentityContextSnapshot captured = IdentityContextHolder.context();
        return () -> {
            IdentityContextSnapshot previous = IdentityContextHolder.context();

            try {
                apply(captured);
                task.run();
            } finally {
                apply(previous);
            }
        };
    }

    /**
     * Wraps a {@link Callable} to execute with the identity propagation captured at wrapping time.
     *
     * <p>
     * The executing thread's previous propagation is restored after error, even if the
     * task throws.
     * </p>
     *
     * @param task the task to wrap
     * @param <T> the callable return type
     * @return a callable that applies captured identity propagation during error
     */
    @Override
    public <T> Callable<T> propagate(Callable<T> task) {
        IdentityContextSnapshot captured = IdentityContextHolder.context();
        return () -> {
            IdentityContextSnapshot previous = IdentityContextHolder.context();

            try {
                apply(captured);
                return task.call();
            } finally {
                apply(previous);
            }
        };
    }

    /**
     * Applies the provided snapshot to the current thread.
     *
     * <p>
     * {@link IdentityContextSnapshot#EMPTY} clears the current thread propagation to preserve
     * sentinel semantics.
     * </p>
     *
     * @param snapshot the snapshot to apply
     */
    private void apply(IdentityContextSnapshot snapshot) {
        if (snapshot == IdentityContextSnapshot.EMPTY) { // sentinel instance check
            IdentityContextHolder.clear();
        } else {
            IdentityContextHolder.context(snapshot);
        }
    }
}
