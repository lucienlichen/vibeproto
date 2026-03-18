package com.vibeproto.document.controller;

import com.vibeproto.auth.security.AuthUser;
import com.vibeproto.common.api.Result;
import com.vibeproto.document.dto.ProjectDocumentCreateRequest;
import com.vibeproto.document.dto.ProjectDocumentUpdateRequest;
import com.vibeproto.document.service.ProjectDocumentService;
import com.vibeproto.document.vo.ProjectDocumentVO;
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
@RequestMapping("/api/documents")
public class ProjectDocumentController {

    private final ProjectDocumentService projectDocumentService;

    public ProjectDocumentController(ProjectDocumentService projectDocumentService) {
        this.projectDocumentService = projectDocumentService;
    }

    @GetMapping
    public Result<List<ProjectDocumentVO>> list(@RequestParam(required = false) Long projectId,
                                                @RequestParam(required = false) Long relatedReleaseId) {
        return Result.success(projectDocumentService.list(projectId, relatedReleaseId));
    }

    @PostMapping
    public Result<ProjectDocumentVO> create(@Valid @RequestBody ProjectDocumentCreateRequest request,
                                            Authentication authentication) {
        return Result.success(projectDocumentService.create(request, currentUserId(authentication)));
    }

    @PutMapping("/{id}")
    public Result<ProjectDocumentVO> update(@PathVariable Long id, @Valid @RequestBody ProjectDocumentUpdateRequest request) {
        return Result.success(projectDocumentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        projectDocumentService.delete(id);
        return Result.success("删除成功", null);
    }

    private Long currentUserId(Authentication authentication) {
        return authentication != null && authentication.getPrincipal() instanceof AuthUser authUser
            ? authUser.getId()
            : 1L;
    }
}
