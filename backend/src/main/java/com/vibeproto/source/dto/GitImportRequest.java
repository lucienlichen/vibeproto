package com.vibeproto.source.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record GitImportRequest(
    @NotNull(message = "项目 ID 不能为空")
    Long projectId,
    @NotBlank(message = "Git 地址不能为空")
    @Size(max = 255, message = "Git 地址长度不能超过 255")
    String gitUrl,
    @NotBlank(message = "分支不能为空")
    @Size(max = 128, message = "分支长度不能超过 128")
    String gitBranch,
    @Size(max = 128, message = "Commit Hash 长度不能超过 128")
    String commitHash,
    @Size(max = 500, message = "版本说明长度不能超过 500")
    String remark
) {
}
