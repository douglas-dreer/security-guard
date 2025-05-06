package br.com.soejin.framework.security_guard.controller.response;

import java.time.LocalDateTime;

public record TokenFullResponse(
        long id,
        UserResponse user,
        String token,
        String refreshToken,
        LocalDateTime expirationDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean revoked
) {
}
