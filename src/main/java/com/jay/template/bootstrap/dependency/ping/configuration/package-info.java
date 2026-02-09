/**
 * Bootstrap wiring for the Ping dependency.
 *
 * <p>
 * The core layer declares the
 * {@link com.jay.template.core.port.dependency.ping.PingDependency}
 * port. The infra layer provides one or more implementations of that port,
 * such as
 * {@link com.jay.template.infra.outbound.http.client.rest.adapter.ping.PingRestClientAdapter}.
 * </p>
 *
 * <p>
 * This package is responsible for choosing which adapter implementation
 * satisfies the port at runtime and exposing it as a Spring bean. In other
 * words, bootstrap binds the port to the adapter here.
 * </p>
 */
package com.jay.template.bootstrap.dependency.ping.configuration;