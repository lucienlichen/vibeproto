package com.vibeproto.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProjectUpdateRequest(
    @NotBlank(message = "项目名称不能为空")
    @Size(max = 128, message = "项目名称长度不能超过 128")
    String name,
    @Size(max = 2000, message = "项目简介长度不能超过 2000")
    String description,
    @NotBlank(message = "项目类型不能为空")
    String projectType,
    @NotNull(message = "默认构建模板不能为空")
    Long defaultBuildProfileId,
    @NotBlank(message = "项目状态不能为空")
    String status
) {
}
