package br.com.soejin.framework.security_guard.exception;

import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class BadCredentialsException extends BadRequestException {
    public BadCredentialsException(String message) {
        super(message);
    }
}
