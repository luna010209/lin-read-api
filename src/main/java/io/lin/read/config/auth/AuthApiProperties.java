package io.lin.read.config.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth.api")
public record AuthApiProperties(
        String baseUrl
) {
}
