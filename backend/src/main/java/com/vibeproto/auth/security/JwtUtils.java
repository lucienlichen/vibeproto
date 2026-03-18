package com.vibeproto.auth.security;

import com.vibeproto.common.config.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtils {

    private final SecretKey secretKey;
    private final JwtProperties jwtProperties;

    public JwtUtils(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        byte[] keyBytes = jwtProperties.secret().length() >= 32
            ? jwtProperties.secret().getBytes(StandardCharsets.UTF_8)
            : Decoders.BASE64.decode("VmliZVByb3RvSldUU2VjcmV0S2V5Rm9yRGV2RW52aXJvbm1lbnQ=");
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(AuthUser user) {
        long expiresAt = System.currentTimeMillis() + jwtProperties.expiration() * 1000;
        return Jwts.builder()
            .claims(Map.of(
                "uid", user.getId(),
                "username", user.getUsername(),
                "nickname", user.getNickname(),
                "roleCode", user.getRoleCode() != null ? user.getRoleCode() : "VIEWER"
            ))
            .subject(user.getUsername())
            .issuedAt(new Date())
            .expiration(new Date(expiresAt))
            .signWith(secretKey)
            .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
