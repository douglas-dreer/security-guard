package br.com.soejin.framework.security_guard.service.impl;

import br.com.soejin.framework.security_guard.controller.request.CreateUserRequest;
import br.com.soejin.framework.security_guard.controller.request.LoginRequest;
import br.com.soejin.framework.security_guard.controller.response.TokenResponse;
import br.com.soejin.framework.security_guard.model.User;
import br.com.soejin.framework.security_guard.service.AuthService;
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

/**
 * Implementação do serviço de autenticação.
 * Esta classe fornece a implementação concreta dos métodos definidos na interface AuthService,
 * incluindo autenticação de usuários, gerenciamento de tokens e criação de novos usuários.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final UserDetailsServiceImpl userDetailsServiceImpl;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    /**
     * Construtor da classe AuthServiceImpl.
     *
     * @param userService Serviço de usuários
     * @param userDetailsServiceImpl Serviço de detalhes do usuário
     * @param authenticationManager Gerenciador de autenticação
     * @param jwtUtil Utilitário para manipulação de tokens JWT
     */
    public AuthServiceImpl(UserService userService, UserDetailsServiceImpl userDetailsServiceImpl, AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.userService = userService;
        this.userDetailsServiceImpl = userDetailsServiceImpl;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Autentica um usuário no sistema usando username e senha.
     * Após a autenticação bem-sucedida, atualiza o registro do último login
     * e gera novos tokens de acesso e refresh.
     *
     * @param request Dados de login do usuário
     * @return Resposta contendo os tokens de acesso e refresh
     * @throws BadRequestException Se as credenciais forem inválidas
     */
    @Override
    @Transactional(rollbackOn = Exception.class)
    public TokenResponse authenticate(LoginRequest request) throws BadRequestException {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.username(),
                            request.password()
                    )
            );

            User user = (User) authentication.getPrincipal();
            userService.updateLastLogin(user);

            return createTokenResponse(user);
        } catch (AuthenticationException e) {
            throw new BadRequestException("Invalid username/password supplied");
        }
    }

    /**
     * Renova o token de acesso usando o token de refresh.
     *
     * @param refreshToken Token de refresh válido
     * @return Novos tokens de acesso e refresh
     * @throws UnsupportedOperationException Método ainda não implementado
     */
    @Override
    public TokenResponse refreshToken(String refreshToken) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'refreshToken'");
    }

    /**
     * Realiza o logout do usuário, invalidando o token atual.
     *
     * @param token Token de acesso a ser invalidado
     * @throws UnsupportedOperationException Método ainda não implementado
     */
    @Override
    public void logout(String token) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'logout'");
    }

    /**
     * Registra um novo usuário no sistema.
     * Cria um novo usuário com os dados fornecidos e associa as permissões padrão.
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
