package br.com.soejin.framework.security_guard.repository;

import br.com.soejin.framework.security_guard.model.Blacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenBlacklistRepository extends JpaRepository<Blacklist, Long> {
    boolean existsBlacklistByToken(String token);
}
