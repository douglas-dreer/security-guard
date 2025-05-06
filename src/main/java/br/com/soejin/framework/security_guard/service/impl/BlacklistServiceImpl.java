package br.com.soejin.framework.security_guard.service.impl;

import br.com.soejin.framework.security_guard.model.Blacklist;
import br.com.soejin.framework.security_guard.model.User;
import br.com.soejin.framework.security_guard.repository.BlacklistRepository;
import br.com.soejin.framework.security_guard.service.BlacklistService;
import br.com.soejin.framework.security_guard.util.JwtUtil;
import org.springframework.stereotype.Service;

@Service
public class BlacklistServiceImpl implements BlacklistService {
    private final BlacklistRepository repository;
    private final JwtUtil jwtUtil;

    public BlacklistServiceImpl(BlacklistRepository repository, JwtUtil jwtUtil) {
        this.repository = repository;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean isBlacklisted(String token) {
        return repository.existsBlacklistByToken(token);
    }

    @Override
    public void addTokenToBlacklist(String token, Long userId) {
        addTokenToBlacklist(token, userId, null);
    }

    @Override
    public void addTokenToBlacklist(final String token, final Long userId, final String description) {
        Blacklist blacklist = new Blacklist();
        User user = new User();
        user.setId(userId);
        blacklist.setUser(user);
        blacklist.setToken(token);
        blacklist.setDescription(description);
        repository.save(blacklist);
    }
}
