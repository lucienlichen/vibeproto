package com.vibeproto.build.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BuildProfileCreateRequest(
    @NotNull(message = "项目 ID 不能为空")
    Long projectId,
    @NotBlank(message = "配置名称不能为空")
    @Size(max = 128, message = "配置名称长度不能超过 128")
    String profileName,
    @NotBlank(message = "Node 版本不能为空")
    @Size(max = 32, message = "Node 版本长度不能超过 32")
    String nodeVersion,
    @NotBlank(message = "安装命令不能为空")
    @Size(max = 255, message = "安装命令长度不能超过 255")
    String installCommand,
    @NotBlank(message = "构建命令不能为空")
    @Size(max = 255, message = "构建命令长度不能超过 255")
    String buildCommand,
    @NotBlank(message = "产物目录不能为空")
    @Size(max = 255, message = "产物目录长度不能超过 255")
    String outputDir,
    String envJson,
    @NotNull(message = "默认状态不能为空")
    Integer isDefault,
    @NotNull(message = "启用状态不能为空")
    Integer enabled
) {
}
