package io.lin.read.config.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        int expirationMinutes,
        int refreshExpirationMinutes,
        String secretKey
) {
}
