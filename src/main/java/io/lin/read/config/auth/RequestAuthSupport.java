package io.lin.read.config.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

@Component
public class RequestAuthSupport {

    public static final String ACCESS_TOKEN_ATTRIBUTE = "io.lin.read.accessToken";

    public Optional<String> authorizationHeader() {
        return bearerAuthorizationHeader();
    }

    public Optional<String> bearerAuthorizationHeader() {
        var attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletAttributes)) {
            return Optional.empty();
        }

        HttpServletRequest request = servletAttributes.getRequest();

        Object tokenAttribute = request.getAttribute(ACCESS_TOKEN_ATTRIBUTE);
        if (tokenAttribute instanceof String token && StringUtils.hasText(token)) {
            return Optional.of("Bearer " + token.trim());
        }

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(header)) {
            return Optional.empty();
        }

        String trimmed = header.trim();
        if (trimmed.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return Optional.of("Bearer " + trimmed.substring(7).trim());
        }

        return Optional.of("Bearer " + trimmed);
    }
}
