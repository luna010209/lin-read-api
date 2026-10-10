package io.lin.read.feature.user.dto;

import io.lin.read.config.auth.dto.AuthProfile;

import java.time.LocalDateTime;
import java.util.List;

public record MeResponse(
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
    public static MeResponse from(AuthProfile profile) {
        return new MeResponse(
                profile.id(),
                profile.username(),
                profile.displayName(),
                profile.email(),
                profile.phone(),
                profile.uid(),
                profile.avatar(),
                profile.lastLogin(),
                profile.createdAt(),
                profile.roles() != null ? profile.roles() : List.of()
        );
    }
}
