package br.com.soejin.framework.security_guard.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a user is not found in the system.
 * This exception is mapped to HTTP 404 (Not Found) status code.
 * 
 * @see br.com.soejin.framework.security_guard.service.UserService#findByUsername(String)
 * @see br.com.soejin.framework.security_guard.service.UserService#updateRole(Long, br.com.soejin.framework.security_guard.enums.RoleTypeEnum)
 * @see org.springframework.http.HttpStatus#NOT_FOUND
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends RuntimeException {

    /**
     * Constructs a new UserNotFoundException with the specified detail message.
     * 
     * @param message The detail message (which is saved for later retrieval by the getMessage() method)
     */
    public UserNotFoundException(String message) {
        super(message);
    }
}
