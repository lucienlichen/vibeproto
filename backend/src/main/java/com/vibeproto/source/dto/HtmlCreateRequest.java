package com.vibeproto.source.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record HtmlCreateRequest(
    @NotNull(message = "项目 ID 不能为空")
    Long projectId,
    @NotBlank(message = "页面名称不能为空")
    @Size(max = 255, message = "页面名称长度不能超过 255")
    String sourceName,
    @NotBlank(message = "HTML 内容不能为空")
    String htmlContent,
    @Size(max = 500, message = "版本说明长度不能超过 500")
    String remark
) {
}
