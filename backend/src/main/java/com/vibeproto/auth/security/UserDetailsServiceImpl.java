package com.vibeproto.auth.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vibeproto.auth.entity.SysUser;
import com.vibeproto.auth.mapper.SysUserMapper;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserMapper sysUserMapper;

    public UserDetailsServiceImpl(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser sysUser = sysUserMapper.selectOne(
            new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username).last("LIMIT 1")
        );
        if (sysUser == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        return new User(
            sysUser.getUsername(),
            sysUser.getPassword(),
            List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
    }
}
