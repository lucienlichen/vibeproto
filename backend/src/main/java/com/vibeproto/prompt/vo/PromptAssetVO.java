package com.vibeproto.prompt.vo;

import java.time.LocalDateTime;

public record PromptAssetVO(
    Long id,
    Long projectId,
    String title,
    String promptType,
    String content,
    Long relatedSourceVersionId,
    Long relatedReleaseId,
    Long createdBy,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
