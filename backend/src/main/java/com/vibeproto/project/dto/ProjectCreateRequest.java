package com.vibeproto.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProjectCreateRequest(
    @NotBlank(message = "项目名称不能为空")
    @Size(max = 128, message = "项目名称长度不能超过 128")
    String name,
    @NotBlank(message = "项目标识不能为空")
    @Size(max = 64, message = "项目标识长度不能超过 64")
    String code,
    @Size(max = 2000, message = "项目简介长度不能超过 2000")
    String description,
    @NotBlank(message = "项目类型不能为空")
    String projectType,
    @NotNull(message = "默认构建模板不能为空")
    Long defaultBuildProfileId
) {
}
