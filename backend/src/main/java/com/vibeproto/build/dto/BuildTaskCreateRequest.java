package com.vibeproto.build.dto;

import jakarta.validation.constraints.NotNull;

public record BuildTaskCreateRequest(
    @NotNull(message = "项目 ID 不能为空")
    Long projectId,
    @NotNull(message = "源码版本 ID 不能为空")
    Long sourceVersionId,
    @NotNull(message = "构建配置 ID 不能为空")
    Long buildProfileId
) {
}
