package io.lin.read.exception;

import io.lin.read.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
public class ExceptionController {

    private final MessageSource messageSource;

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleException(CustomException e, HttpServletRequest request) {

        String message = messageSource.getMessage(
                e.getMessageCode(),
                e.getArgs(),
                e.getMessageCode(),
                LocaleContextHolder.getLocale()
        );

        Map<String, Object> errorDetail = new HashMap<>();
        errorDetail.put("path", request.getRequestURI());

        return ResponseEntity
                .status(e.getStatus())
                .body(ApiResponse.fail(e.getStatus().value(), message, errorDetail));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, String> fields = new HashMap<>();
        Locale locale = LocaleContextHolder.getLocale();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {

            String msg = messageSource.getMessage(
                    error.getDefaultMessage(),
                    error.getArguments(),
                    error.getDefaultMessage(),
                    locale
            );

            fields.put(error.getField(), msg);
        }

        String message = messageSource.getMessage(
                "valid.failed",
                null,
                "Validation failed",
                LocaleContextHolder.getLocale()
        );

        Map<String, Object> errorDetail = new HashMap<>();
        errorDetail.put("path", request.getRequestURI());
        errorDetail.put("fields", fields);

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.fail(HttpStatus.BAD_REQUEST.value(), message, errorDetail));
    }
}
