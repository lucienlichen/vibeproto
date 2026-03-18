package com.vibeproto.user.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(@NotBlank String newPassword) {}
