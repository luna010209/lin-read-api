package io.lin.read.config.jwt.cJwtConfig;

import io.lin.read.config.auth.RequestAuthSupport;
import io.lin.read.config.jwt.bToken.TokenProvider;
import io.lin.read.exception.CustomException;
import io.lin.read.security.TokenUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);
    private final TokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        SecurityContextHolder.clearContext();

        String jwt = TokenUtil.resolveToken(request);
        String requestURI = request.getRequestURI();

        if (StringUtils.hasText(jwt)) {
            try {
                Authentication authentication = tokenProvider.getAuthentication(jwt);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                request.setAttribute(RequestAuthSupport.ACCESS_TOKEN_ATTRIBUTE, jwt);
                logger.debug("Authentication saved in Security Context for user: {}, uri: {}",
                        authentication.getName(), requestURI);
            } catch (CustomException e) {
                if (Objects.equals("error.token.expired", e.getMessageCode())) {
                    response.setHeader(TokenUtil.TOKEN_EXPIRED_HEADER, "true");
                    logger.warn("JWT expired. uri: {}", requestURI);
                } else {
                    logger.warn("JWT rejected ({}). uri: {}", e.getMessageCode(), requestURI);
                }
            } catch (Exception e) {
                logger.warn("JWT processing failed. uri: {}", requestURI, e);
            }
        } else {
            logger.debug("No JWT provided. uri: {}", requestURI);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
