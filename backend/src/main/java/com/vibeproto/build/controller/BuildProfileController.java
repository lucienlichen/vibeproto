package com.vibeproto.build.controller;

import com.vibeproto.auth.security.AuthUser;
import com.vibeproto.build.dto.BuildProfileCreateRequest;
import com.vibeproto.build.dto.BuildProfileUpdateRequest;
import com.vibeproto.build.service.BuildProfileService;
import com.vibeproto.build.vo.BuildProfileVO;
import com.vibeproto.common.api.Result;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/build-profiles")
public class BuildProfileController {

    private final BuildProfileService buildProfileService;

    public BuildProfileController(BuildProfileService buildProfileService) {
        this.buildProfileService = buildProfileService;
    }

    @GetMapping
    public Result<List<BuildProfileVO>> list(@RequestParam Long projectId) {
        return Result.success(buildProfileService.listByProjectId(projectId));
    }

    @PostMapping
    public Result<BuildProfileVO> create(@Valid @RequestBody BuildProfileCreateRequest request, Authentication authentication) {
        return Result.success(buildProfileService.create(request, currentUserId(authentication)));
    }

    @PutMapping("/{id}")
    public Result<BuildProfileVO> update(@PathVariable Long id, @Valid @RequestBody BuildProfileUpdateRequest request) {
        return Result.success(buildProfileService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        buildProfileService.delete(id);
        return Result.success("删除成功", null);
    }

    private Long currentUserId(Authentication authentication) {
        return authentication != null && authentication.getPrincipal() instanceof AuthUser authUser
            ? authUser.getId()
            : 1L;
    }
}
