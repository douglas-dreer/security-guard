package br.com.soejin.framework.security_guard.service.impl;

import br.com.soejin.framework.security_guard.enums.TokenTypeEnum;
import br.com.soejin.framework.security_guard.exception.BadCredentialsException;
import br.com.soejin.framework.security_guard.exception.TokenInvalidException;
import br.com.soejin.framework.security_guard.exception.TokenNotFoundException;
import br.com.soejin.framework.security_guard.model.Token;
import br.com.soejin.framework.security_guard.model.User;
import br.com.soejin.framework.security_guard.repository.TokenRepository;
import br.com.soejin.framework.security_guard.service.BlacklistService;
import br.com.soejin.framework.security_guard.service.TokenService;
import br.com.soejin.framework.security_guard.util.JwtUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Implementação do serviço de tokens.
 * Esta classe fornece a implementação concreta dos métodos definidos na interface TokenService,
 * lidando com criação, validação e gerenciamento de tokens de autenticação.
 */
@Service
public class TokenServiceImpl implements TokenService {
    private final BlacklistService blacklistService;
    private final TokenRepository tokenRepository;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    /**
     * Construtor com injeção de dependência via construtor.
     * 
     * @param blacklistService Serviço de blacklist para tokens invalidados
     * @param tokenRepository Repositório para persistência de tokens
     * @param jwtUtil Utilitário para manipulação de tokens JWT
     * @param userDetailsService Serviço de detalhes do usuário
     */
    public TokenServiceImpl(BlacklistService blacklistService, TokenRepository tokenRepository, 
                           JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.blacklistService = blacklistService;
        this.tokenRepository = tokenRepository;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Salva o token fornecido no repositório.
     * 
     * @param token O token a ser salvo
     * @return O token salvo
     */
    @Override
    public Token save(Token token) {
        return tokenRepository.save(token);
    }

    /**
     * Busca um token pelo valor do token.
     * 
     * @param token O valor do token a ser buscado
     * @return O token encontrado
     * @throws TokenNotFoundException Se o token não for encontrado
     */
    @Override
    public Token findByToken(String token) {
        return tokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenNotFoundException("Token não encontrado"));
    }

    /**
     * Desativa um token, tornando-o inválido para futuras operações.
     * 
     * @param token O valor do token a ser desativado
     */
    @Override
    public void desactive(String token) {
        Token tokenToDesactivate = findByToken(token);
        tokenRepository.delete(tokenToDesactivate);
    }

    /**
     * Recupera uma lista paginada de tokens com base em seu status.
     * 
     * @param page O número da página a recuperar, começando de 0
     * @param pageSize O número de tokens a incluir em cada página
     * @param status O status dos tokens para filtrar (ex: ativo ou revogado)
     * @return Uma página de tokens que correspondem ao status especificado
     */
    @Override
    public Page<Token> findAllByStatus(int page, int pageSize, boolean status) {
        Pageable pageable = PageRequest.of(page, pageSize);
        return tokenRepository.findTokenByRevokedIs(pageable, status);
    }

    /**
     * Cria um novo token para o usuário fornecido.
     * 
     * @param user O usuário para o qual o token será criado
     * @return O token criado
     */
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

    /**
     * Busca um token pelo ID do usuário que não esteja revogado.
     * 
     * @param userId ID do usuário
     * @return O token válido para o usuário especificado
     */
    @Override
    public Token tokenByUserId(Long userId) {
        return tokenRepository
                .findTokenByUserIdAndRevokedFalse(userId);
    }

    /**
     * Valida um token de acesso.
     * Verifica se o token é nulo ou vazio, se já está na blacklist e se é válido.
     * 
     * @param token Token de acesso a ser validado
     * @throws TokenInvalidException Se o token for inválido ou já estiver na blacklist
     */
    @Override
    public void validateAccessToken(String token) throws TokenInvalidException {
        if (token == null || token.isEmpty()) {
            throw new TokenInvalidException("Token nulo ou vazio");
        }
        
        if (blacklistService.isBlacklisted(token)) {
            throw new TokenInvalidException("Token inválido ou já invalidado");
        }
        
        String username = jwtUtil.extractUsername(token);
        if (username == null) {
            throw new TokenInvalidException("Token inválido ou expirado");
        }
        
        User user = (User) userDetailsService.loadUserByUsername(username);
        if (!jwtUtil.isTokenValid(token, user)) {
            throw new TokenInvalidException("Token inválido ou expirado");
        }
    }

    /**
     * Valida um token de refresh.
     * Verifica se o token é nulo ou vazio, se já está na blacklist e se é válido.
     * 
     * @param refreshToken Token de refresh a ser validado
     * @throws TokenInvalidException Se o token for inválido ou já estiver na blacklist
     */
    @Override
    public void validateRefreshToken(String refreshToken) throws TokenInvalidException {
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new TokenInvalidException("Token de refresh nulo ou vazio");
        }
        
        if (blacklistService.isBlacklisted(refreshToken)) {
            throw new BadCredentialsException("Token de refresh invalidado");
        }
        
        String username = jwtUtil.extractUsername(refreshToken);
        if (username == null) {
            throw new BadCredentialsException("Token de refresh inválido ou expirado");
        }
        
        User user = (User) userDetailsService.loadUserByUsername(username);
        if (!jwtUtil.isTokenValid(refreshToken, user)) {
            throw new BadCredentialsException("Token de refresh inválido ou expirado");
        }
    }
}
