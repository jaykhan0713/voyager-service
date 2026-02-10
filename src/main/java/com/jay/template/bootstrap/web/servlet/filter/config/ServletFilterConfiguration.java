package com.jay.voyager.bootstrap.web.servlet.filter.config;

import io.github.resilience4j.bulkhead.BulkheadRegistry;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.jay.voyager.core.port.observability.mdc.MdcFieldNamesProvider;
import com.jay.voyager.core.port.transport.http.IdentityHeadersProvider;
import com.jay.voyager.web.servlet.filter.BulkheadFilter;
import com.jay.voyager.web.servlet.filter.IdentityFilter;
import com.jay.voyager.web.servlet.filter.MdcFilter;
import com.jay.voyager.web.servlet.error.ErrorResponseWriter;

@Configuration
public class ServletFilterConfiguration {

    private static final String API_WILDCARD = "/api/*";
    private static final String BULKHEAD_FILTER_INSTANCE_NAME = "webBulkheadFilter";

    @Bean
    public FilterRegistrationBean<IdentityFilter> identityFilter(IdentityHeadersProvider identityHeadersProvider) {
        FilterRegistrationBean<IdentityFilter> registration = new FilterRegistrationBean<>();
        IdentityFilter identityFilter = new IdentityFilter(identityHeadersProvider.identityHeaders());

        registration.setFilter(identityFilter);
        registration.setOrder(FilterOrders.IDENTITY.order());
        registration.addUrlPatterns(API_WILDCARD);

        return registration;
    }

    @Bean
    public FilterRegistrationBean<MdcFilter> mdcFilter(MdcFieldNamesProvider mdcFieldNamesProvider) {
        FilterRegistrationBean<MdcFilter> registration = new FilterRegistrationBean<>();
        MdcFilter mdcFilter = new MdcFilter(mdcFieldNamesProvider.mdcFieldNames());

        registration.setFilter(mdcFilter);
        registration.setOrder(FilterOrders.MDC.order());
        registration.addUrlPatterns(API_WILDCARD);

        return registration;
    }

    @Bean
    public FilterRegistrationBean<BulkheadFilter> bulkheadFilter(
        BulkheadRegistry bulkheadRegistry,
        ErrorResponseWriter errorResponseWriter
    ) {
        FilterRegistrationBean<BulkheadFilter> registration = new FilterRegistrationBean<>();

        BulkheadFilter bulkheadFilter = new BulkheadFilter(
                bulkheadRegistry.bulkhead(BULKHEAD_FILTER_INSTANCE_NAME),
                errorResponseWriter
        );

        registration.setFilter(bulkheadFilter);
        registration.setOrder(FilterOrders.BULKHEAD.order());
        registration.addUrlPatterns(API_WILDCARD);

        return registration;
    }

    private enum FilterOrders {
        IDENTITY(0),
        MDC(1),
        BULKHEAD(2);

        private final int order;

        FilterOrders(int order) {
            this.order = order;
        }

        public int order() {
            return order;
        }
    }
}
