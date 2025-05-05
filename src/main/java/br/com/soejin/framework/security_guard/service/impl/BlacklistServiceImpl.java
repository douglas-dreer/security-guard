package br.com.soejin.framework.security_guard.service.impl;

import br.com.soejin.framework.security_guard.model.Blacklist;
import br.com.soejin.framework.security_guard.repository.TokenBlacklistRepository;
import br.com.soejin.framework.security_guard.service.BlacklistService;
import org.springframework.stereotype.Service;

@Service
public class BlacklistServiceImpl implements BlacklistService {
    private final TokenBlacklistRepository repository;

    public BlacklistServiceImpl(TokenBlacklistRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean isBlacklisted(String token) {
        return repository.existsBlacklistByToken(token);
    }

    @Override
    public void addTokenToBlacklist(String token) {
        Blacklist blacklist = new Blacklist();
        blacklist.setToken(token);
        repository.save(blacklist);
    }
}
