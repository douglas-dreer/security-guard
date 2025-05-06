package br.com.soejin.framework.security_guard.controller.response;

import java.time.LocalDateTime;

public record MessageResponse(
        String title,
        String message,
        int code,
        LocalDateTime timestamp
) {
}
