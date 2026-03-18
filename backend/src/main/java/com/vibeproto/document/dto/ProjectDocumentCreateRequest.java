package com.vibeproto.document.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProjectDocumentCreateRequest(
    @NotNull(message = "项目 ID 不能为空")
    Long projectId,
    @NotBlank(message = "文档类型不能为空")
    String docType,
    @NotBlank(message = "标题不能为空")
    @Size(max = 255, message = "标题长度不能超过 255")
    String title,
    @NotBlank(message = "内容不能为空")
    String content,
    Long relatedReleaseId
) {
}
