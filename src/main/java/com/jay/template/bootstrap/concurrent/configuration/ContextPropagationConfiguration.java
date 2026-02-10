package com.jay.voyager.bootstrap.concurrent.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.jay.voyager.infra.concurrent.propagation.ContextPropagator;
import com.jay.voyager.infra.concurrent.propagation.identity.IdentityContextPropagator;
import com.jay.voyager.infra.concurrent.propagation.mdc.MdcContextPropagator;

@Configuration
public class ContextPropagationConfiguration {

    @Bean
    public ContextPropagator identityContextPropagator() {
        return new IdentityContextPropagator();
    }

    @Bean
    public ContextPropagator mdcContextPropagator() {
        return new MdcContextPropagator();
    }
}
