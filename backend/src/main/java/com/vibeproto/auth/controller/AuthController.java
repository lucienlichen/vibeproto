package com.vibeproto.auth.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vibeproto.auth.dto.LoginRequest;
import com.vibeproto.auth.entity.SysRole;
import com.vibeproto.auth.entity.SysUser;
import com.vibeproto.auth.entity.SysUserRole;
import com.vibeproto.auth.mapper.SysRoleMapper;
import com.vibeproto.auth.mapper.SysUserMapper;
import com.vibeproto.auth.mapper.SysUserRoleMapper;
import com.vibeproto.auth.security.AuthUser;
import com.vibeproto.auth.security.JwtUtils;
import com.vibeproto.auth.vo.LoginResponse;
import com.vibeproto.auth.vo.UserInfoVO;
import com.vibeproto.common.api.Result;
import com.vibeproto.common.exception.BizException;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;

    public AuthController(JwtUtils jwtUtils,
                          AuthenticationManager authenticationManager,
                          SysUserMapper sysUserMapper,
                          SysUserRoleMapper sysUserRoleMapper,
                          SysRoleMapper sysRoleMapper) {
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
        this.sysUserMapper = sysUserMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.sysRoleMapper = sysRoleMapper;
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );
        } catch (BadCredentialsException e) {
            throw new BizException(4001, "用户名或密码错误");
        }

        SysUser sysUser = sysUserMapper.selectOne(
            new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, request.username()).last("LIMIT 1")
        );
        if (sysUser == null || !"active".equals(sysUser.getStatus())) {
            throw new BizException(4001, "用户名或密码错误");
        }

        // find role
        SysUserRole userRole = sysUserRoleMapper.selectOne(
            new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, sysUser.getId()).last("LIMIT 1"));
        String roleCode = "VIEWER";
        if (userRole != null) {
            SysRole role = sysRoleMapper.selectById(userRole.getRoleId());
            if (role != null) roleCode = role.getRoleCode();
        }

        AuthUser authUser = new AuthUser(sysUser.getId(), sysUser.getUsername(),
            sysUser.getNickname() != null ? sysUser.getNickname() : sysUser.getUsername(), roleCode);
        String token = jwtUtils.generateToken(authUser);
        return Result.success(new LoginResponse(token, authUser.toUserInfo()));
    }

    @GetMapping("/me")
    public Result<UserInfoVO> currentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthUser authUser)) {
            throw new BizException(4010, "未登录");
        }
        return Result.success(authUser.toUserInfo());
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success("退出成功", null);
    }
}
