package io.lin.read.common.security;

import io.lin.read.config.jwt.aUserDetails.CustomUserDetails;
import io.lin.read.exception.CustomException;
import io.lin.read.security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class CurrentUserContext {

    private CurrentUserContext() {
    }

    public static AuthenticatedUser get() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        if (authentication.getPrincipal() instanceof CustomUserDetails details) {
            return details.getUser();
        }
        return null;
    }

    public static AuthenticatedUser require() {
        AuthenticatedUser user = get();
        if (user == null) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "error.auth.login_required");
        }
        return user;
    }
}
