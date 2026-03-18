package com.vibeproto.build.vo;

import java.time.LocalDateTime;

public record BuildTaskVO(
    Long id,
    Long projectId,
    Long sourceVersionId,
    Long buildProfileId,
    String taskNo,
    String status,
    String logPath,
    LocalDateTime startTime,
    LocalDateTime endTime,
    Long durationMs,
    String errorMessage,
    Long triggeredBy,
    LocalDateTime createdAt,
    String projectName,
    String sourceVersionNo,
    String buildProfileName
) {
}
