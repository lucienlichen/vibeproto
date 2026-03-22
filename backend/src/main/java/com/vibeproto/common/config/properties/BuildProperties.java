package com.vibeproto.common.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "vibeproto.build")
public record BuildProperties(int timeoutSeconds, String dockerImage) {
}
