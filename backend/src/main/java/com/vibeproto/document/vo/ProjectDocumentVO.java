package com.vibeproto.document.vo;

import java.time.LocalDateTime;

public record ProjectDocumentVO(
    Long id,
    Long projectId,
    String docType,
    String title,
    String content,
    Long relatedReleaseId,
    Long createdBy,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
