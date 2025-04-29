package br.com.soejin.framework.security_guard.service;

import br.com.soejin.framework.security_guard.controller.request.CreateUserRequest;
import br.com.soejin.framework.security_guard.controller.request.LoginRequest;
import br.com.soejin.framework.security_guard.controller.response.TokenResponse;
import org.apache.coyote.BadRequestException;

/**
 * Interface que define os serviços relacionados à autenticação e autorização.
 * Esta interface fornece métodos para autenticação de usuários, gerenciamento
 * de tokens e criação de novos usuários.
 */
public interface AuthService {
    /**
     * Autentica um usuário no sistema.
     *
     * @param request Dados de login do usuário
     * @return Resposta contendo os tokens de acesso e refresh
     * @throws BadRequestException Se as credenciais forem inválidas
     */
    TokenResponse authenticate(LoginRequest request) throws BadRequestException;

    /**
     * Renova o token de acesso usando o token de refresh.
     *
     * @param refreshToken Token de refresh válido
     * @return Novos tokens de acesso e refresh
     */
    TokenResponse refreshToken(String refreshToken);

    /**
     * Realiza o logout do usuário, invalidando o token atual.
     *
     * @param token Token de acesso a ser invalidado
     */
    void logout(String token);

    /**
     * Cria um novo usuário no sistema.
     *
     * @param request Dados do usuário a ser criado
     */
    void createUser(CreateUserRequest request);
}
