package com.vibeproto.release.controller;

import com.vibeproto.common.api.Result;
import com.vibeproto.release.service.ReleaseVersionService;
import com.vibeproto.release.vo.ReleaseVersionVO;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/releases")
public class ReleaseVersionController {

    private final ReleaseVersionService releaseVersionService;

    public ReleaseVersionController(ReleaseVersionService releaseVersionService) {
        this.releaseVersionService = releaseVersionService;
    }

    @GetMapping
    public Result<List<ReleaseVersionVO>> list(@RequestParam(required = false) Long projectId) {
        return Result.success(releaseVersionService.list(projectId));
    }

    @GetMapping("/{id}")
    public Result<ReleaseVersionVO> detail(@PathVariable Long id) {
        return Result.success(releaseVersionService.getById(id));
    }

    @PostMapping("/{id}/set-current")
    public Result<ReleaseVersionVO> setCurrent(@PathVariable Long id) {
        return Result.success(releaseVersionService.setCurrent(id));
    }

    @PostMapping("/{id}/rollback")
    public Result<ReleaseVersionVO> rollback(@PathVariable Long id) {
        return Result.success(releaseVersionService.rollback(id));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        releaseVersionService.delete(id);
        return Result.success("删除成功", null);
    }
}
