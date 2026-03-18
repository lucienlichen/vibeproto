package com.vibeproto.build.vo;

import java.time.LocalDateTime;

public record BuildProfileVO(
    Long id,
    Long projectId,
    String profileName,
    String nodeVersion,
    String installCommand,
    String buildCommand,
    String outputDir,
    String envJson,
    Integer isDefault,
    Integer enabled,
    Long createdBy,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
