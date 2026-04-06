package com.blogi.modules.system.controller;

import com.blogi.common.api.ApiResponse;
import java.time.Instant;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping
    public ApiResponse<Map<String, Object>> health() {
        return ApiResponse.ok(Map.of(
            "service", "blogi-server",
            "status", "UP",
            "timestamp", Instant.now().toString()
        ));
    }
}
