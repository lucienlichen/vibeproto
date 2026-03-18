package com.vibeproto.user.vo;

import java.time.LocalDateTime;

public record UserVO(
    Long id,
    String username,
    String nickname,
    String email,
    String status,
    String roleCode,
    String roleName,
    LocalDateTime createdAt
) {}
