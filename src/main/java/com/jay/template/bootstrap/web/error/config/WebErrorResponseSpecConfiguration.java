package com.jay.template.bootstrap.web.error.config;

import io.micrometer.tracing.Tracer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.jay.template.web.error.ErrorResponseSpecFactory;

@Configuration
public class WebErrorResponseSpecConfiguration {

    @Bean
    public ErrorResponseSpecFactory errorResponseSpecFactory(Tracer tracer) {
        return new ErrorResponseSpecFactory(tracer);
    }
}
