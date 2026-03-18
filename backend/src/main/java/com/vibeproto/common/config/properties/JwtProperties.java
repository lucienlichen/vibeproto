package com.vibeproto.common.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "vibeproto.jwt")
public record JwtProperties(String secret, Long expiration) {
}
