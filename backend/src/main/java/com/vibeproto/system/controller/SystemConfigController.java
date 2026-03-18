package com.vibeproto.system.controller;

import com.vibeproto.common.api.Result;
import com.vibeproto.system.dto.SystemConfigUpdateRequest;
import com.vibeproto.system.service.SystemConfigService;
import com.vibeproto.system.vo.SystemConfigVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/system")
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    public SystemConfigController(SystemConfigService systemConfigService) {
        this.systemConfigService = systemConfigService;
    }

    @GetMapping("/configs")
    public Result<List<SystemConfigVO>> listAll() {
        return Result.success(systemConfigService.listAll());
    }

    @PutMapping("/configs")
    public Result<List<SystemConfigVO>> batchUpdate(@Valid @RequestBody SystemConfigUpdateRequest request) {
        return Result.success(systemConfigService.batchUpdate(request));
    }
}
