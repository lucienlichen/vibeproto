package com.vibeproto.system.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SystemConfigUpdateRequest(
    @NotNull List<ConfigItem> items
) {
    public record ConfigItem(
        @NotNull String configKey,
        @NotNull String configValue
    ) {
    }
}
