package com.vibeproto.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vibeproto.auth.entity.SysRole;
import com.vibeproto.auth.entity.SysUser;
import com.vibeproto.auth.entity.SysUserRole;
import com.vibeproto.auth.mapper.SysRoleMapper;
import com.vibeproto.auth.mapper.SysUserMapper;
import com.vibeproto.auth.mapper.SysUserRoleMapper;
import com.vibeproto.common.exception.BizException;
import com.vibeproto.common.model.PageResponse;
import com.vibeproto.user.dto.ChangePasswordRequest;
import com.vibeproto.user.dto.UserCreateRequest;
import com.vibeproto.user.dto.UserUpdateRequest;
import com.vibeproto.user.service.UserService;
import com.vibeproto.user.vo.UserVO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(SysUserMapper sysUserMapper,
                           SysRoleMapper sysRoleMapper,
                           SysUserRoleMapper sysUserRoleMapper,
                           PasswordEncoder passwordEncoder) {
        this.sysUserMapper = sysUserMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public PageResponse<UserVO> pageUsers(long pageNum, long pageSize) {
        Page<SysUser> page = sysUserMapper.selectPage(
            Page.of(pageNum, pageSize),
            new LambdaQueryWrapper<SysUser>().orderByDesc(SysUser::getCreatedAt)
        );
        List<UserVO> records = page.getRecords().stream().map(this::toVO).toList();
        return new PageResponse<>(page.getTotal(), page.getCurrent(), page.getSize(), records);
    }

    @Override
    @Transactional
    public UserVO createUser(UserCreateRequest request) {
        // check username not taken
        if (sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getUsername, request.username()).last("LIMIT 1")) != null) {
            throw new BizException("用户名已存在");
        }

        SysRole role = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
            .eq(SysRole::getRoleCode, request.roleCode()).last("LIMIT 1"));
        if (role == null) {
            throw new BizException("角色不存在");
        }

        SysUser user = new SysUser();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setNickname(request.nickname());
        user.setEmail(request.email());
        user.setStatus("active");
        sysUserMapper.insert(user);

        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(role.getId());
        sysUserRoleMapper.insert(userRole);

        return toVO(user);
    }

    @Override
    @Transactional
    public UserVO updateUser(Long id, UserUpdateRequest request) {
        SysUser user = requireUser(id);

        if (StringUtils.hasText(request.nickname())) {
            user.setNickname(request.nickname());
        }
        if (request.email() != null) {
            user.setEmail(request.email());
        }
        if (StringUtils.hasText(request.status())) {
            user.setStatus(request.status());
        }
        sysUserMapper.updateById(user);

        if (StringUtils.hasText(request.roleCode())) {
            SysRole role = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, request.roleCode()).last("LIMIT 1"));
            if (role == null) {
                throw new BizException("角色不存在");
            }
            sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, id));
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(id);
            userRole.setRoleId(role.getId());
            sysUserRoleMapper.insert(userRole);
        }

        return toVO(sysUserMapper.selectById(id));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        SysUser user = requireUser(id);

        // prevent deleting last SUPER_ADMIN
        SysUserRole userRole = sysUserRoleMapper.selectOne(new LambdaQueryWrapper<SysUserRole>()
            .eq(SysUserRole::getUserId, id).last("LIMIT 1"));
        if (userRole != null) {
            SysRole role = sysRoleMapper.selectById(userRole.getRoleId());
            if (role != null && "SUPER_ADMIN".equals(role.getRoleCode())) {
                long superAdminCount = sysUserRoleMapper.selectCount(new LambdaQueryWrapper<SysUserRole>()
                    .eq(SysUserRole::getRoleId, role.getId()));
                if (superAdminCount <= 1) {
                    throw new BizException("不能删除最后一个超级管理员");
                }
            }
        }

        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));
        sysUserMapper.deleteById(user.getId());
    }

    @Override
    @Transactional
    public void changePassword(Long id, ChangePasswordRequest request) {
        SysUser user = requireUser(id);
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        sysUserMapper.updateById(user);
    }

    private SysUser requireUser(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BizException(4040, "用户不存在");
        }
        return user;
    }

    private UserVO toVO(SysUser user) {
        String roleCode = null;
        String roleName = null;
        SysUserRole userRole = sysUserRoleMapper.selectOne(new LambdaQueryWrapper<SysUserRole>()
            .eq(SysUserRole::getUserId, user.getId()).last("LIMIT 1"));
        if (userRole != null) {
            SysRole role = sysRoleMapper.selectById(userRole.getRoleId());
            if (role != null) {
                roleCode = role.getRoleCode();
                roleName = role.getRoleName();
            }
        }
        return new UserVO(
            user.getId(),
            user.getUsername(),
            user.getNickname(),
            user.getEmail(),
            user.getStatus(),
            roleCode,
            roleName,
            user.getCreatedAt()
        );
    }
}
