package com.jay.template.app.smoke.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.jay.template.app.dependency.error.DependencyExceptionTranslator;
import com.jay.template.core.domain.dependency.ping.PingResult;
import com.jay.template.core.domain.smoke.SmokeCheckResult;
import com.jay.template.core.port.dependency.ping.PingDependency;

@Service
@Profile("smoke")
public class SmokeService {
    private final PingDependency pingDependency;

    public SmokeService(PingDependency pingDependency) {
        this.pingDependency = pingDependency;
    }

    public SmokeCheckResult runSmokeCheck() {

        PingResult pingResult = DependencyExceptionTranslator.execute(pingDependency::ping);

        //map ping + other dependencies to business use-case (smoke check in this case)
        var pingCheckResult = new SmokeCheckResult.PingCheckResult(pingResult.ok(), pingResult.msg());

        return new SmokeCheckResult(pingCheckResult);
    }
}
