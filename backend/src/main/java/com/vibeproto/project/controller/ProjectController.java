package com.vibeproto.project.controller;

import com.vibeproto.auth.security.AuthUser;
import com.vibeproto.common.api.Result;
import com.vibeproto.common.model.PageResponse;
import com.vibeproto.project.dto.ProjectCreateRequest;
import com.vibeproto.project.dto.ProjectUpdateRequest;
import com.vibeproto.project.service.ProjectService;
import com.vibeproto.project.vo.ProjectPreviewInfoVO;
import com.vibeproto.project.vo.ProjectVO;
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

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public Result<PageResponse<ProjectVO>> pageProjects(
        @RequestParam(defaultValue = "1") long pageNum,
        @RequestParam(defaultValue = "10") long pageSize,
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String projectType,
        @RequestParam(required = false) String status
    ) {
        return Result.success(projectService.pageProjects(pageNum, pageSize, name, projectType, status));
    }

    @PostMapping
    public Result<ProjectVO> createProject(@Valid @RequestBody ProjectCreateRequest request, Authentication authentication) {
        Long operatorId = authentication != null && authentication.getPrincipal() instanceof AuthUser authUser
            ? authUser.getId()
            : 1L;
        return Result.success(projectService.createProject(request, operatorId));
    }

    @GetMapping("/{id}")
    public Result<ProjectVO> getProject(@PathVariable Long id) {
        return Result.success(projectService.getProject(id));
    }

    @PutMapping("/{id}")
    public Result<ProjectVO> updateProject(@PathVariable Long id, @Valid @RequestBody ProjectUpdateRequest request) {
        return Result.success(projectService.updateProject(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return Result.success("删除成功", null);
    }

    @PostMapping("/{id}/archive")
    public Result<ProjectVO> archiveProject(@PathVariable Long id) {
        return Result.success(projectService.archiveProject(id));
    }

    @GetMapping("/{id}/preview-info")
    public Result<ProjectPreviewInfoVO> previewInfo(@PathVariable Long id) {
        return Result.success(projectService.getPreviewInfo(id));
    }
}
