package br.com.soejin.framework.security_guard.service.impl;

import br.com.soejin.framework.security_guard.enums.TokenTypeEnum;
import br.com.soejin.framework.security_guard.model.Token;
import br.com.soejin.framework.security_guard.model.User;
import br.com.soejin.framework.security_guard.repository.TokenRepository;
import br.com.soejin.framework.security_guard.service.BlacklistService;
import br.com.soejin.framework.security_guard.service.TokenService;
import br.com.soejin.framework.security_guard.util.JwtUtil;
import org.springframework.stereotype.Service;

@Service
public class TokenServiceImpl implements TokenService {
    private final BlacklistService blacklistService;
    private final TokenRepository tokenRepository;
    private final JwtUtil jwtUtil;

    public TokenServiceImpl(BlacklistService blacklistService, TokenRepository tokenRepository, JwtUtil jwtUtil) {
        this.blacklistService = blacklistService;
        this.tokenRepository = tokenRepository;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Token save(Token token) {
        return tokenRepository.save(token);
    }

    @Override
    public Token createToken(User user) {
        final String tokenHash = jwtUtil.generateToken(user);
        Token token = new Token();
        token.setUser(user);
        token.setTokenType(TokenTypeEnum.BEARER);
        token.setToken(tokenHash);
        token.setRefreshToken(jwtUtil.generateRefreshToken(user));
        token.setExpirationDate(jwtUtil.getExpirationDate(tokenHash));
        token.setRevoked(false);
        return token;
    }

    @Override
    public Token tokenByUserId(Long userId) {
        return tokenRepository.findTokenByUserIdAndRevokedFalse(userId);
    }


}
