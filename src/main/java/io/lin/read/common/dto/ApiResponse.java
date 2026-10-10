package io.lin.read.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        int code,
        String message,
        T data,
        Object error
) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, 200, "Request processed successfully.", data, null);
    }

    public static ApiResponse<Void> okVoid() {
        return new ApiResponse<>(true, 200, "Request processed successfully.", null, null);
    }

    public static <T> ApiResponse<T> fail(int code, String message, Object errorDetail) {
        return new ApiResponse<>(false, code, message, null, errorDetail);
    }
}
