package br.com.soejin.framework.security_guard.service;

public interface BlacklistService {
    boolean isBlacklisted(String token);
    void addTokenToBlacklist(String token);
}
