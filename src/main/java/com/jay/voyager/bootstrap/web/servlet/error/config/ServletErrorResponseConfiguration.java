package com.jay.voyager.bootstrap.web.servlet.error.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

import com.jay.voyager.web.error.ErrorResponseSpecFactory;
import com.jay.voyager.web.servlet.error.ErrorResponseWriter;

@Configuration
public class ServletErrorResponseConfiguration {

    @Bean
    public ErrorResponseWriter errorResponseWriter(
            ErrorResponseSpecFactory factory,
            ObjectMapper objectMapper
    ) {
        return new ErrorResponseWriter(factory, objectMapper);
    }
}
