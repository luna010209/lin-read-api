package io.lin.read.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException extends RuntimeException {
    private final HttpStatus status;
    private final String messageCode;
    private final Object[] args;

    public CustomException(HttpStatus status, String messageCode, Object... args) {
        super(messageCode);
        this.status = status;
        this.messageCode = messageCode;
        this.args = args;
    }
}
