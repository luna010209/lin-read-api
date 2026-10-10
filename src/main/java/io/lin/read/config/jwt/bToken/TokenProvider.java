package io.lin.read.config.jwt.bToken;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;
import io.lin.read.config.jwt.JwtProperties;
import io.lin.read.config.jwt.aUserDetails.CustomUserDetails;
import io.lin.read.exception.CustomException;
import io.lin.read.security.AuthenticatedUser;
import io.lin.read.security.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class TokenProvider {

    private static final Logger staticLog = LoggerFactory.getLogger(TokenProvider.class);

    private final SecretKey signingKey;

    public TokenProvider(JwtProperties properties) {
        this.signingKey = Keys.hmacShaKeyFor(
                Base64.getEncoder()
                        .withoutPadding().encode(properties.secretKey().getBytes(StandardCharsets.UTF_8))
        );
    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        if ("refresh".equals(claims.get("typ", String.class))) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "error.token.invalid");
        }

        Long userId = resolveUserId(claims);
        if (userId == null) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "error.token.invalid");
        }

        Set<Role> roles = parseApplicationRoles(claims.get("auth", String.class));

        AuthenticatedUser user = new AuthenticatedUser(
                userId,
                claims.getSubject(),
                claims.get("displayName", String.class),
                roles
        );

        Collection<GrantedAuthority> applicationAuthorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .collect(Collectors.toList());

        return new UsernamePasswordAuthenticationToken(
                new CustomUserDetails(user),
                null,
                applicationAuthorities
        );
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return true;

        } catch (ExpiredJwtException e) {
            staticLog.warn("JWT expired at {}", e.getClaims().getExpiration());
            throw new CustomException(HttpStatus.UNAUTHORIZED, "error.token.expired");

        } catch (MalformedJwtException e) {
            staticLog.warn("JWT malformed: {}", e.getMessage());
            throw new CustomException(HttpStatus.UNAUTHORIZED, "error.token.malformed");

        } catch (SecurityException | WeakKeyException | IllegalArgumentException e) {
            staticLog.warn("JWT invalid: {}", e.getMessage());
            throw new CustomException(HttpStatus.UNAUTHORIZED, "error.token.invalid");

        } catch (Exception e) {
            staticLog.error("Unexpected JWT validation error", e);
            throw new CustomException(HttpStatus.UNAUTHORIZED, "error.token.invalid");
        }
    }

    public boolean validateRefreshToken(String token) {
        if (Objects.isNull(token)) {
            return false;
        }

        try {
            Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Set<Role> parseApplicationRoles(String authClaim) {
        if (authClaim == null || authClaim.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(authClaim.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(this::parseRole)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private Role parseRole(String authority) {
        if (authority == null || authority.isBlank() || !authority.startsWith("ROLE_")) {
            return null;
        }
        try {
            return Role.valueOf(authority);
        } catch (IllegalArgumentException ex) {
            staticLog.debug("Ignoring unknown JWT role authority: {}", authority);
            return null;
        }
    }

    private Long resolveUserId(Claims claims) {
        Object raw = claims.get("userId");
        if (raw instanceof Number number) {
            return number.longValue();
        }
        if (raw instanceof String value && !value.isBlank()) {
            try {
                return Long.parseLong(value.trim());
            } catch (NumberFormatException ex) {
                staticLog.warn("Invalid userId claim in JWT: {}", value);
            }
        }
        return null;
    }
}
