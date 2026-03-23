package com.vibeproto.source.controller;

import com.vibeproto.auth.security.AuthUser;
import com.vibeproto.common.api.Result;
import com.vibeproto.source.dto.GitImportRequest;
import com.vibeproto.source.dto.HtmlCreateRequest;
import com.vibeproto.source.service.SourceVersionService;
import com.vibeproto.source.vo.SourceVersionVO;
import jakarta.validation.Valid;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/source-versions")
public class SourceVersionController {

    private final SourceVersionService sourceVersionService;

    public SourceVersionController(SourceVersionService sourceVersionService) {
        this.sourceVersionService = sourceVersionService;
    }

    @PostMapping("/upload")
    public Result<SourceVersionVO> upload(@RequestParam("projectId") Long projectId,
                                          @RequestParam(value = "remark", required = false) String remark,
                                          @RequestPart("file") MultipartFile file,
                                          Authentication authentication) throws IOException {
        return Result.success(sourceVersionService.uploadZip(projectId, remark, file, currentUserId(authentication)));
    }

    @PostMapping("/html-create")
    public Result<SourceVersionVO> htmlCreate(@Valid @ModelAttribute HtmlCreateRequest request,
                                              Authentication authentication) throws IOException {
        return Result.success(sourceVersionService.createFromHtml(request, currentUserId(authentication)));
    }

    @PostMapping("/git-import")
    public Result<SourceVersionVO> gitImport(@Valid @ModelAttribute GitImportRequest request, Authentication authentication) {
        return Result.success(sourceVersionService.createFromGit(request, currentUserId(authentication)));
    }

    @GetMapping
    public Result<List<SourceVersionVO>> list(@RequestParam Long projectId) {
        return Result.success(sourceVersionService.listByProjectId(projectId));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        Path file = sourceVersionService.getDownloadFile(id);
        String filename = file.getFileName().toString();
        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .body(resource);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sourceVersionService.delete(id);
        return Result.success("删除成功", null);
    }

    private Long currentUserId(Authentication authentication) {
        return authentication != null && authentication.getPrincipal() instanceof AuthUser authUser
            ? authUser.getId()
            : 1L;
    }
}
