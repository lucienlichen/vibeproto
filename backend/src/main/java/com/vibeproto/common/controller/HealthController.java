package com.vibeproto.common.controller;

import com.vibeproto.common.api.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping
    public Result<Map<String, Object>> health() {
        return Result.success(Map.of(
            "status", "UP",
            "service", "vibeproto-backend",
            "timestamp", Instant.now().toString()
        ));
    }
}
