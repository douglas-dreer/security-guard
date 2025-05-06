package br.com.soejin.framework.security_guard.repository;

import br.com.soejin.framework.security_guard.model.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
    boolean existsTokenByToken(String token);
    void deleteTokenByToken(String token);
    Token findTokenByToken(String token);
    Token findTokenByUserId(Long userId);
    Token findTokenByUserIdAndRevokedFalse(Long userId);
}
