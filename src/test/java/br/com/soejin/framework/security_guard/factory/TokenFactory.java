package br.com.soejin.framework.security_guard.factory;

import java.time.LocalDateTime;

import br.com.soejin.framework.security_guard.enums.TokenTypeEnum;
import br.com.soejin.framework.security_guard.model.Token;
import br.com.soejin.framework.security_guard.model.User;

public class TokenFactory {
    private static final Long id = 1L;
    private static final User user = UserFactory.createUser();
    private static final String token = "eyJhbGciOiJIUzI1NiJ9.eyJJc3N1ZXIiOiJJc3N1ZXIiLCJVc2VybmFtZSI6IkpvZSBDb2xsaW5nIiwiUm9sZXMiOiJST0xFX1VTRVIiLCJleHAiOjE3NDY2MzgzODksImlhdCI6MTc0NjYzODM4OX0.wQJfIFNm-yNusM4f038Tww3p_AgWUjlk_pxkPB3_MYM";
    private static final TokenTypeEnum tokenType = TokenTypeEnum.BEARER;
    private static final String refreshToken = token;
    private static final LocalDateTime createdAt = LocalDateTime.now().minusMonths(6);
    private static final LocalDateTime updatedAt = LocalDateTime.now().minusDays(7);
    private static final LocalDateTime expirationDate = createdAt.plusDays(7);
    private static final boolean revoked = false;


    /**
     * Cria uma nova instancia do Token com todos os dados preenchidos
     * @return
     */
    public static Token createToken() {
        return Token.builder()
            .id(id)
            .user(user)
            .token(token)
            .tokenType(tokenType)
            .refreshToken(refreshToken)
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .expirationDate(expirationDate)
            .revoked(revoked)
        .build();
    }



}
