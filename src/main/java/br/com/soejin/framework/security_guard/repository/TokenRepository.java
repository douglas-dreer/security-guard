package br.com.soejin.framework.security_guard.repository;

import br.com.soejin.framework.security_guard.model.Token;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
    Token findTokenByUserIdAndRevokedFalse(Long userId);

    @Query(value = "SELECT t FROM Token t WHERE t.token = :token")
    Optional<Token> findByToken(String token);

    Page<Token> findTokenByRevokedIs(Pageable pageable, boolean revoked);
}
