package com.vibeproto.user.controller;

import com.vibeproto.auth.mapper.SysRoleMapper;
import com.vibeproto.common.api.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final SysRoleMapper sysRoleMapper;

    public RoleController(SysRoleMapper sysRoleMapper) {
        this.sysRoleMapper = sysRoleMapper;
    }

    @GetMapping
    public Result<List<?>> listRoles() {
        return Result.success(sysRoleMapper.selectList(null));
    }
}
