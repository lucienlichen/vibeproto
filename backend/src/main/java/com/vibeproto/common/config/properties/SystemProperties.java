package com.vibeproto.common.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "vibeproto.system")
public record SystemProperties(String basePreviewUrl) {
}
