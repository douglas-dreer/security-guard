package br.com.soejin.framework.security_guard.service;

import br.com.soejin.framework.security_guard.model.User;

/**
 * Interface que define os serviços relacionados à gestão de usuários.
 * Esta interface fornece métodos para criar, atualizar e excluir usuários,
 * além de gerenciar informações de login.
 */
public interface UserService {
    /**
     * Cria um novo usuário no sistema.
     *
     * @param username Nome de usuário único
     * @param email Email do usuário
     * @param password Senha do usuário
     * @return O usuário criado
     */
    User createUser(String username, String email, String password);

    /**
     * Atualiza a senha de um usuário existente.
     *
     * @param username Nome de usuário do usuário a ter a senha atualizada
     * @param password Nova senha do usuário
     * @return O usuário com a senha atualizada
     */
    User updatePassword(String username, String password);

    /**
     * Remove um usuário do sistema.
     *
     * @param username Nome de usuário do usuário a ser removido
     */
    void deleteUser(String username);

    /**
     * Atualiza o registro do último login do usuário.
     *
     * @param user Usuário que realizou o login
     */
    void updateLastLogin(User user);
}
