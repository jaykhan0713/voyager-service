package com.jay.template.bootstrap.concurrent.configuration;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.jay.template.infra.concurrent.PlatformVirtualThreadFactory;
import com.jay.template.infra.concurrent.propagation.ContextPropagator;

@Configuration
public class VirtualThreadsConfiguration {

    @Bean
    ThreadFactory platformVirtualThreadFactory(List<ContextPropagator> propagators) {
        return new PlatformVirtualThreadFactory(List.copyOf(propagators));
    }

    /*
     * FUTURE-NOTE: When real async orchestration use cases show up, prefer CompletableFuture with this
     *  executor (do not use the ForkJoin common pool). Choose per-use-case policies such as fail-fast,
     *  join-all, or partial results with explicit exception handling. If async patterns repeat across
     *  services, consider introducing a small infra helper or port at that time.
     */
    @Bean(name = "platformVirtualThreadExecutorService", destroyMethod = "close")
    ExecutorService platformVirtualThreadExecutorService(ThreadFactory platformVirtualThreadFactory) {
        return Executors.newThreadPerTaskExecutor(platformVirtualThreadFactory);
    }
}
