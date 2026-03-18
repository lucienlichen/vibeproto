package com.vibeproto.auth.security;

import com.vibeproto.auth.vo.UserInfoVO;
import lombok.Getter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

@Getter
public class AuthUser {

    private final Long id;
    private final String username;
    private final String nickname;
    private final String roleCode;

    public AuthUser(Long id, String username, String nickname, String roleCode) {
        this.id = id;
        this.username = username;
        this.nickname = nickname;
        this.roleCode = roleCode;
    }

    public UsernamePasswordAuthenticationToken toAuthentication() {
        return new UsernamePasswordAuthenticationToken(
            this,
            null,
            List.of(new SimpleGrantedAuthority("ROLE_" + (roleCode != null ? roleCode : "VIEWER")))
        );
    }

    public UserInfoVO toUserInfo() {
        return new UserInfoVO(id, username, nickname, roleCode);
    }
}
