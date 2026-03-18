package com.vibeproto.common.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "vibeproto.storage")
public record StorageProperties(String rootPath) {
}
