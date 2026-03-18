package com.vibeproto.source.vo;

import java.time.LocalDateTime;

public record SourceVersionVO(
    Long id,
    Long projectId,
    String versionNo,
    String sourceType,
    String sourceName,
    String filePath,
    String gitUrl,
    String gitBranch,
    String commitHash,
    String htmlContentPath,
    String remark,
    Long createdBy,
    LocalDateTime createdAt
) {
}
