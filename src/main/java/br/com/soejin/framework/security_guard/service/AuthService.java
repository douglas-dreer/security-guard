package br.com.soejin.framework.security_guard.service;

import br.com.soejin.framework.security_guard.controller.request.CreateUserRequest;
import br.com.soejin.framework.security_guard.controller.request.LoginRequest;
import br.com.soejin.framework.security_guard.controller.response.TokenResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.apache.coyote.BadRequestException;

/**
 * Interface de serviço para autenticação e gerenciamento de usuários.
 * Esta interface segue o princípio de segregação de interfaces (ISP) do SOLID,
 * focando apenas em operações relacionadas à autenticação.
 */
public interface AuthService {

    /**
     * Autentica um usuário e gera tokens JWT de acesso e refresh.
     *
     * @param request Dados de login (username e senha)
     * @return Resposta contendo tokens de acesso e refresh
     * @throws BadRequestException Se as credenciais forem inválidas
     */
    TokenResponse authenticate(LoginRequest request) throws BadRequestException;

    /**
     * Renova um token de acesso expirado usando o token de refresh.
     *
     * @param refreshToken Token de refresh válido
     * @return Novos tokens de acesso e refresh
     */
    TokenResponse refreshToken(String refreshToken);

    /**
     * Realiza o logout do usuário, invalidando seus tokens.
     *
     * @param token Token de acesso a ser invalidado
     */
    void logout(String token);

    /**
     * Cria um novo usuário com os dados fornecidos.
     * Essa operação poderia ser movida para um UserRegistrationService específico
     * em uma refatoração futura para seguir melhor o SRP.
     *
     * @param request Dados do usuário a ser criado
     */
    void createUser(@Valid @NotNull CreateUserRequest request);
}
