package com.vibeproto.release.vo;

import java.time.LocalDateTime;

public record ReleaseVersionVO(
    Long id,
    Long projectId,
    Long sourceVersionId,
    Long buildTaskId,
    String versionNo,
    String releasePath,
    String previewUrl,
    Integer isCurrent,
    Long releasedBy,
    LocalDateTime createdAt
) {
}
