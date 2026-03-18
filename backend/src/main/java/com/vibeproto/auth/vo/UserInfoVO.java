package com.vibeproto.auth.vo;

public record UserInfoVO(
    Long id,
    String username,
    String nickname,
    String roleCode
) {
}
