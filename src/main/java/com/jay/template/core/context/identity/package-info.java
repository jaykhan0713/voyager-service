/**
 * Request-scoped identity context.
 *
 * <p>
 * This package provides primitives for carrying and accessing request identity
 * (for example userId and requestId) throughout the execution of a request.
 * Identity is metadata associated with the current execution, similar
 * in spirit to mdc or trace context.
 * </p>
 *
 * <p>
 * Identity may be read by any layer and may be propagated to downstream
 * dependencies as part of outbound requests. Binding and clearing are performed
 * at inbound boundaries.
 * </p>
 */
package com.jay.template.core.context.identity;
