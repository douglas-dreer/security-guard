package br.com.soejin.framework.security_guard.service;

import br.com.soejin.framework.security_guard.model.Blacklist;

public interface BlacklistService {
    boolean isBlacklisted(String token);
    void addTokenToBlacklist(final String token, final Long userId);
    void addTokenToBlacklist(final String token, final Long userId, final String description);
}
