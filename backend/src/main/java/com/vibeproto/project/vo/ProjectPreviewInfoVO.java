package com.vibeproto.project.vo;

public record ProjectPreviewInfoVO(
    Long projectId,
    String projectCode,
    String latestUrl,
    String currentReleaseUrl
) {
}
