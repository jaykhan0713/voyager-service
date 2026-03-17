package com.jay.voyager.smoke;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import com.jay.voyager.common.FunctionalTestBase;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationStartupTest extends FunctionalTestBase {

    private final ApplicationContext appContext;

    ApplicationStartupTest(ApplicationContext appContext) {
        this.appContext = appContext;
    }

    @Test
    void contextLoads() {
        assertNotNull(appContext);
    }
}
