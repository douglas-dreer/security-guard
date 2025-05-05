package br.com.soejin.framework.security_guard.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record LoginRequest(
//        @NotBlank(message = "Username is required")
//        @NotNull(message = "Username is required")
//        @Length(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
//        String username,

        @NotBlank(message = "Email is required")
        @NotNull(message = "Email is required")
        String email,

        @NotNull(message = "Password is required")
        @NotBlank(message = "Password is required")
        @Length(min = 6, max = 20, message = "Password must be between 6 and 20 characters")
        String password
) {
}
