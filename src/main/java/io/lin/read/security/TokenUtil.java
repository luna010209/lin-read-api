package io.lin.read.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

public final class TokenUtil {
    private TokenUtil() {}

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String TOKEN_EXPIRED_HEADER = "X-Token-Expired";

    public static String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(TokenUtil.AUTHORIZATION_HEADER);
        if (!StringUtils.hasText(bearerToken)) {
            return null;
        }

        String trimmed = bearerToken.trim();
        if (trimmed.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return trimmed.substring(7).trim();
        }

        if (looksLikeJwt(trimmed)) {
            return trimmed;
        }

        return null;
    }

    private static boolean looksLikeJwt(String value) {
        return value.chars().filter(ch -> ch == '.').count() == 2;
    }
}
