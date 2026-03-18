package com.vibeproto.build.controller;

import com.vibeproto.auth.security.AuthUser;
import com.vibeproto.build.dto.BuildTaskCreateRequest;
import com.vibeproto.build.service.BuildTaskService;
import com.vibeproto.build.vo.BuildTaskVO;
import com.vibeproto.common.api.Result;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/build-tasks")
public class BuildTaskController {

    private final BuildTaskService buildTaskService;

    public BuildTaskController(BuildTaskService buildTaskService) {
        this.buildTaskService = buildTaskService;
    }

    @PostMapping
    public Result<BuildTaskVO> create(@Valid @RequestBody BuildTaskCreateRequest request, Authentication authentication) {
        return Result.success(buildTaskService.create(request, currentUserId(authentication)));
    }

    @GetMapping
    public Result<List<BuildTaskVO>> list(@RequestParam(required = false) Long projectId) {
        return Result.success(buildTaskService.list(projectId));
    }

    @GetMapping("/{id}")
    public Result<BuildTaskVO> detail(@PathVariable Long id) {
        return Result.success(buildTaskService.getById(id));
    }

    @GetMapping("/{id}/log")
    public Result<String> log(@PathVariable Long id) {
        return Result.success(buildTaskService.getLog(id));
    }

    @PostMapping("/{id}/cancel")
    public Result<BuildTaskVO> cancel(@PathVariable Long id) {
        return Result.success(buildTaskService.cancel(id));
    }

    @PostMapping("/{id}/retry")
    public Result<BuildTaskVO> retry(@PathVariable Long id, Authentication authentication) {
        return Result.success(buildTaskService.retry(id, currentUserId(authentication)));
    }

    private Long currentUserId(Authentication authentication) {
        return authentication != null && authentication.getPrincipal() instanceof AuthUser authUser
            ? authUser.getId()
            : 1L;
    }
}
