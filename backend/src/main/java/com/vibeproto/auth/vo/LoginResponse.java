package com.vibeproto.auth.vo;

public record LoginResponse(
    String token,
    UserInfoVO userInfo
) {
}
