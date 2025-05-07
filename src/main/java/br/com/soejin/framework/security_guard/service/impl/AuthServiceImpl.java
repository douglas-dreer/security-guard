package br.com.soejin.framework.security_guard.service.impl;

import br.com.soejin.framework.security_guard.controller.mapper.TokenMapper;
import br.com.soejin.framework.security_guard.controller.request.CreateUserRequest;
import br.com.soejin.framework.security_guard.controller.request.LoginRequest;
import br.com.soejin.framework.security_guard.controller.response.TokenResponse;
import br.com.soejin.framework.security_guard.exception.BadCredentialsException;
import br.com.soejin.framework.security_guard.exception.TokenInvalidException;
import br.com.soejin.framework.security_guard.model.Token;
import br.com.soejin.framework.security_guard.model.User;
import br.com.soejin.framework.security_guard.service.AuthService;
import br.com.soejin.framework.security_guard.service.BlacklistService;
import br.com.soejin.framework.security_guard.service.TokenService;
import br.com.soejin.framework.security_guard.service.UserService;
import br.com.soejin.framework.security_guard.util.JwtUtil;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.apache.coyote.BadRequestException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementação do serviço de autenticação.
 * Esta classe fornece a implementação concreta dos métodos definidos na interface AuthService,
 * incluindo autenticação de usuários, gerenciamento de tokens e criação de novos usuários.
 */
@Service
public class AuthServiceImpl implements AuthService {
    private static final Logger logger = Logger.getLogger(AuthServiceImpl.class.getName());

    private final UserService userService;
    private final UserDetailsServiceImpl userDetailsServiceImpl;
    private final BlacklistService blacklistService;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;

    private final JwtUtil jwtUtil;
    private final TokenMapper tokenMapper;

    /**
     * Construtor da classe AuthServiceImpl.
     *
     * @param userService Serviço de usuários
     * @param userDetailsServiceImpl Serviço de detalhes do usuário
     * @param authenticationManager Gerenciador de autenticação
     * @param jwtUtil Utilitário para manipulação de tokens JWT
     * @param blacklistService Serviço de blacklist para tokens invalidados
     * @param tokenService Serviço de tokens
     * @param tokenMapper Mapeador de tokens para respostas
     */
    public AuthServiceImpl(UserService userService, UserDetailsServiceImpl userDetailsServiceImpl,
                           AuthenticationManager authenticationManager, JwtUtil jwtUtil,
                           BlacklistService blacklistService, TokenService tokenService, TokenMapper tokenMapper) {
        this.userService = userService;
        this.userDetailsServiceImpl = userDetailsServiceImpl;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.blacklistService = blacklistService;
        this.tokenService = tokenService;
        this.tokenMapper = tokenMapper;
    }

    /**
     * Autentica um usuário no sistema usando username e senha.
     * Após a autenticação bem-sucedida, atualiza o último login e gera novos tokens.
     *
     * @param request Dados de login do usuário.
     * @return {@link TokenResponse} contendo os tokens de acesso e refresh.
     * @throws BadRequestException Se as credenciais forem inválidas.
     */
    @Override
    @Transactional(rollbackOn = Exception.class)
    public TokenResponse authenticate(LoginRequest request) throws BadRequestException {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );

            User user = (User) authentication.getPrincipal();

            Optional<Token> tokenValido = Optional.ofNullable(tokenService.tokenByUserId(user.getId()));
            return tokenValido
                    .map(tokenMapper::toResponse)
                    .orElseGet(() -> {
                        userService.updateLastLogin(user);
                        Token novoToken = tokenService.createToken(user);
                        return tokenMapper.toResponse(tokenService.save(novoToken));
                    });

        } catch (AuthenticationException | TokenInvalidException e) {
            throw new BadRequestException("Usuário ou senha inválidos", e);
        }
    }

    /**
     * Renova o token de acesso usando o token de refresh.
     *
     * @param refreshToken Token de refresh válido
     * @return Novos tokens de acesso e refresh
     * @throws BadCredentialsException Se o token de refresh for inválido
     */
    @Override
    @Transactional(rollbackOn = Exception.class)
    public TokenResponse refreshToken(String refreshToken) {
        try {
            // Validar o token de refresh através do TokenService
            tokenService.validateRefreshToken(refreshToken);
            
            String username = jwtUtil.extractUsername(refreshToken);
            User user = (User) userDetailsServiceImpl.loadUserByUsername(username);
            
            // Invalidar o token de refresh atual
            blacklistService.addTokenToBlacklist(refreshToken, user.getId());

            // Gerar novos tokens
            return createTokenResponse(user);
        } catch (TokenInvalidException e) {
            logger.log(Level.WARNING, "Token de refresh inválido: " + e.getMessage());
            throw new BadCredentialsException("Token de refresh inválido ou expirado: " + e.getMessage());
        } catch (Exception e) {
            if (e instanceof BadCredentialsException) {
                throw e;
            }
            throw new BadCredentialsException("Falha ao renovar o token: " + e.getMessage());
        }
    }

    /**
     * Realiza o logout do usuário, invalidando o token atual.
     * Adiciona o token à blacklist para impedir seu uso futuro.
     *
     * @param token Token de acesso a ser invalidado
     */
    @Override
    @Transactional(rollbackOn = Exception.class)
    public void logout(String token) {
        try {
            tokenService.validateAccessToken(token);

            String username = jwtUtil.extractUsername(token);
            User user = (User) userDetailsServiceImpl.loadUserByUsername(username);

            tokenService.desactive(token);
            blacklistService.addTokenToBlacklist(token, user.getId(), "User logout ");
        } catch (TokenInvalidException e) {
            logger.log(Level.WARNING, "Tentativa de logout com token inválido: " + e.getMessage());
            throw new BadCredentialsException("Token inválido ou já invalidado");
        }
    }

    /**
     * Registra um novo usuário no sistema.
     * Cria um usuário com os dados fornecidos e associa as permissões padrão.
     *
     * @param request Dados do usuário a ser registrado
     */
    @Override
    @Transactional(rollbackOn = Exception.class)
    public void createUser(@Valid @NotNull CreateUserRequest request) {
        User userSaved = userService.createUser(
                request.username(),
                request.email(),
                request.password()
        );
    }

    /**
     * Cria uma resposta de token contendo o token de acesso e refresh.
     *
     * @param user Usuário para o qual os tokens serão gerados
     * @return Resposta contendo os tokens gerados
     */
    private TokenResponse createTokenResponse(User user) {
        String accessToken = jwtUtil.generateToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);
        return new TokenResponse(accessToken, refreshToken);
    }

}
