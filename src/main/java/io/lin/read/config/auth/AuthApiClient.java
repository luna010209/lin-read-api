package io.lin.read.config.auth;

import io.lin.read.config.auth.dto.AuthApiResponse;
import io.lin.read.config.auth.dto.AuthProfile;
import io.lin.read.config.auth.dto.AuthSummary;
import io.lin.read.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AuthApiClient {

    private final RestClient authRestClient;
    private final RequestAuthSupport requestAuthSupport;

    public AuthProfile fetchProfile() {
        try {
            AuthApiResponse<AuthProfile> response = authRestClient.get()
                    .uri("/auth")
                    .headers(this::applyAuthorization)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (response == null || response.data() == null) {
                throw new CustomException(HttpStatus.BAD_GATEWAY, "error.auth.api_unavailable");
            }
            return response.data();
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new CustomException(HttpStatus.UNAUTHORIZED, "error.auth.login_required");
            }
            throw new CustomException(HttpStatus.BAD_GATEWAY, "error.auth.api_unavailable");
        }
    }

    public Map<Long, AuthSummary> lookupUsers(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }

        List<Long> distinctIds = ids.stream().distinct().toList();
        AuthApiResponse<List<AuthSummary>> response = authRestClient.post()
                .uri("/auth/users/lookup")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(this::applyAuthorization)
                .body(Map.of("ids", distinctIds))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        if (response == null || response.data() == null) {
            return Map.of();
        }

        return response.data().stream()
                .collect(Collectors.toMap(AuthSummary::id, Function.identity(), (left, right) -> left));
    }

    public void requireUserExists(Long userId) {
        try {
            authRestClient.get()
                    .uri("/auth/users/{userId}/exists", userId)
                    .headers(this::applyAuthorization)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new CustomException(HttpStatus.NOT_FOUND, "error.auth.user_not_found");
            }
            throw new CustomException(HttpStatus.BAD_GATEWAY, "error.auth.api_unavailable");
        }
    }

    public Map<Long, String> lookupDisplayNamesOrEmpty(List<Long> ids) {
        try {
            return lookupUsers(ids).entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().displayName()));
        } catch (Exception ex) {
            return Collections.emptyMap();
        }
    }

    private void applyAuthorization(org.springframework.http.HttpHeaders headers) {
        requestAuthSupport.bearerAuthorizationHeader()
                .ifPresent(value -> headers.set("Authorization", value));
    }
}
