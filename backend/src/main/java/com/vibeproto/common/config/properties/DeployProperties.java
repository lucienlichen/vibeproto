package com.vibeproto.common.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "vibeproto.deploy")
public record DeployProperties(String rootPath) {
}
