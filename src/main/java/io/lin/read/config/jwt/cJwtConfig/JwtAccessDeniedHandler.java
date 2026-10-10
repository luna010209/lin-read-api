package io.lin.read.config.jwt.cJwtConfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {
    private static final Logger logger = LoggerFactory.getLogger(JwtAccessDeniedHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MessageSource messageSource;

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException ex
    ) throws IOException, ServletException {
        logger.warn("Access denied: {} {} - {}", request.getMethod(), request.getRequestURI(), ex.getMessage());

        String message = messageSource.getMessage(
                "error.access_denied",
                null,
                "Access denied",
                LocaleContextHolder.getLocale()
        );
        // 403 Forbidden Response
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now(ZoneOffset.UTC).toString());
        body.put("status", HttpServletResponse.SC_FORBIDDEN);
        body.put("error", "FORBIDDEN");
        body.put("message", message);
        body.put("path", request.getRequestURI());

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
