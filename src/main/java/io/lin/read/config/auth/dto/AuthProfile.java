package io.lin.read.config.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.lin.read.security.AuthenticatedUser;

import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AuthProfile(
        Long id,
        String username,
        String displayName,
        String email,
        String phone,
        String uid,
        String avatar,
        LocalDateTime lastLogin,
        LocalDateTime createdAt,
        List<String> roles
) {
    public static AuthProfile fromJwt(AuthenticatedUser user) {
        return new AuthProfile(
                user.id(),
                user.username(),
                user.displayName(),
                null,
                null,
                null,
                null,
                null,
                null,
                user.roles().stream().map(Enum::name).toList()
        );
    }
}
