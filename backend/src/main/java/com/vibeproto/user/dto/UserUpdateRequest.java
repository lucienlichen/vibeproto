package com.vibeproto.user.dto;

public record UserUpdateRequest(
    String nickname,
    String email,
    String status,
    String roleCode
) {}
