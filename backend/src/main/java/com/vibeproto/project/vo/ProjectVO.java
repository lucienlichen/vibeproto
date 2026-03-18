package com.vibeproto.project.vo;

import java.time.LocalDateTime;

public record ProjectVO(
    Long id,
    String name,
    String code,
    String description,
    String projectType,
    String status,
    Long defaultBuildProfileId,
    Long currentReleaseId,
    Long ownerId,
    Long createdBy,
    String currentVersion,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
