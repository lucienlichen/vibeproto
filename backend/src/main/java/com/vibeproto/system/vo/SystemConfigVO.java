package com.vibeproto.system.vo;

import java.time.LocalDateTime;

public record SystemConfigVO(
    Long id,
    String configKey,
    String configValue,
    String description,
    LocalDateTime updatedAt
) {
}
