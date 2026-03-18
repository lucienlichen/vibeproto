package com.vibeproto.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UserCreateRequest(
    @NotBlank String username,
    @NotBlank String password,
    @NotBlank String nickname,
    String email,
    @NotBlank String roleCode
) {}
