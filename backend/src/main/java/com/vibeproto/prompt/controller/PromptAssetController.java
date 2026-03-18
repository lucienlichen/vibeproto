package com.vibeproto.prompt.controller;

import com.vibeproto.auth.security.AuthUser;
import com.vibeproto.common.api.Result;
import com.vibeproto.prompt.dto.PromptAssetCreateRequest;
import com.vibeproto.prompt.dto.PromptAssetUpdateRequest;
import com.vibeproto.prompt.service.PromptAssetService;
import com.vibeproto.prompt.vo.PromptAssetVO;
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
@RequestMapping("/api/prompts")
public class PromptAssetController {

    private final PromptAssetService promptAssetService;

    public PromptAssetController(PromptAssetService promptAssetService) {
        this.promptAssetService = promptAssetService;
    }

    @GetMapping
    public Result<List<PromptAssetVO>> list(@RequestParam(required = false) Long projectId) {
        return Result.success(promptAssetService.list(projectId));
    }

    @GetMapping("/{id}")
    public Result<PromptAssetVO> getById(@PathVariable Long id) {
        return Result.success(promptAssetService.getById(id));
    }

    @PostMapping
    public Result<PromptAssetVO> create(@Valid @RequestBody PromptAssetCreateRequest request,
                                        Authentication authentication) {
        return Result.success(promptAssetService.create(request, currentUserId(authentication)));
    }

    @PutMapping("/{id}")
    public Result<PromptAssetVO> update(@PathVariable Long id,
                                        @Valid @RequestBody PromptAssetUpdateRequest request) {
        return Result.success(promptAssetService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        promptAssetService.delete(id);
        return Result.success("删除成功", null);
    }

    private Long currentUserId(Authentication authentication) {
        return authentication != null && authentication.getPrincipal() instanceof AuthUser authUser
            ? authUser.getId()
            : 1L;
    }
}
