package br.com.soejin.framework.security_guard.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class AlreadyDataRegisterException extends RuntimeException {
    public AlreadyDataRegisterException(String message) {
        super(message);
    }
}
