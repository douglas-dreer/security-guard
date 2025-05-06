package br.com.soejin.framework.security_guard.repository;

import br.com.soejin.framework.security_guard.model.Blacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BlacklistRepository extends JpaRepository<Blacklist, Long> {
    @Query(value = "SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Blacklist b WHERE b.token = :token")
    boolean existsBlacklistByToken(@Param("token") String token);
}
