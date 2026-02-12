package com.jay.voyager.web.mvc.controller.smoke;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jay.voyager.app.smoke.service.SmokeService;
import com.jay.voyager.core.domain.smoke.SmokeCheckResult;
import com.jay.voyager.web.mvc.controller.smoke.api.model.SmokeResponse;
import com.jay.voyager.web.mvc.controller.smoke.mapping.SmokeResponseMapper;

@Profile("smoke")
@RestController
@Hidden //hidden from swagger
public class SmokeController {

    private final SmokeService smokeService;
    private final SmokeResponseMapper responseMapper;

    public SmokeController(
            SmokeService smokeService,
            SmokeResponseMapper responseMapper
    ) {
        this.smokeService = smokeService;
        this.responseMapper = responseMapper;
    }

    @GetMapping("/api/smoke")
    public SmokeResponse get() {

        SmokeCheckResult smokeCheckResult = smokeService.runSmokeCheck();

        //map app orchestration model to response DTO
        return responseMapper.mapSmokeCheckResult(smokeCheckResult);
    }
}
