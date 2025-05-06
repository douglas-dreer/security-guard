package br.com.soejin.framework.security_guard.service;

import br.com.soejin.framework.security_guard.model.Token;
import br.com.soejin.framework.security_guard.model.User;

public interface TokenService {
    /**
     * Busca um token pelo usuario fornecido que não esteja revogado.
     * @param userId {@link Long} ID do usuário.
     */
    Token tokenByUserId(Long userId);

    /**
     * Cria um novo token para o usuário fornecido.
     *
     * @param user O usuário para o qual o token será criado.
     * @return O token criado.
     */
    Token createToken(User user);

    /**
     * Salva o token fornecido no repositório.
     *
     * @param token O token a ser salvo.
     * @return O token salvo.
     */
    Token save(Token token);
}
