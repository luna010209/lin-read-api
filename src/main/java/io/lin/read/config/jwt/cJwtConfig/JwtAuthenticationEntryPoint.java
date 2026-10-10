package io.lin.read.config.jwt.cJwtConfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.lin.read.security.TokenUtil;
import io.lin.read.utils.i18n.I18nUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final I18nUtil languageUtil;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {
        String uri = request.getRequestURI();

        if (!(uri.contains(".env") ||
                uri.endsWith(".cgi") ||
                uri.contains("php") ||
                uri.contains("wp-") ||
                uri.contains("admin") ||
                uri.contains(".git"))) {
            logger.debug("Unauthorized access: {} {}", request.getMethod(), uri);
        }

        String tokenExpiredHeader = request.getHeader(TokenUtil.TOKEN_EXPIRED_HEADER);
        if (Objects.equals("true", tokenExpiredHeader)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "TOKEN_EXPIRED");
            errorResponse.put("message", languageUtil.m("error.token.expired"));
            errorResponse.put("status", HttpServletResponse.SC_UNAUTHORIZED);

            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
            return;
        }

        // 일반적인 인증 실패 응답
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "UNAUTHORIZED");
        errorResponse.put("message", languageUtil.m("error.auth.login_required"));
        errorResponse.put("status", HttpServletResponse.SC_UNAUTHORIZED);

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
