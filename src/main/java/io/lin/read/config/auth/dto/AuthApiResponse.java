package io.lin.read.config.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AuthApiResponse<T>(
        boolean success,
        int code,
        String message,
        T data
) {
}
