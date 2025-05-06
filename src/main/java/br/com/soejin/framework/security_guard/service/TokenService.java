package br.com.soejin.framework.security_guard.service;

import br.com.soejin.framework.security_guard.exception.TokenInvalidException;
import br.com.soejin.framework.security_guard.model.Token;
import br.com.soejin.framework.security_guard.model.User;
import org.springframework.data.domain.Page;

import java.util.List;

public interface TokenService {
    /**
     * Finds a token entity based on the provided token string.
     * This method is commonly used to retrieve details about a token,
     * such as its associated user, type, and expiration status.
     *
     * @param token The unique string representing the token to be searched.
     * @return The Token entity associated with the provided string, or null if no such token exists.
     */
    Token findByToken(String token);

    /**
     * Busca um token pelo usuario fornecido que não esteja revogado.
     * @param userId {@link Long} ID do usuário.
     */
    Token tokenByUserId(Long userId) throws TokenInvalidException;

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

    /**
     * Desativa o token fornecido, tornando-o inválido para futuras operações.
     *
     * @param token O token que será desativado.
     */
    void desactive(String token);

    /**
     * Retrieves a paginated list of tokens based on their status.
     *
     * @param page The page number to retrieve, starting from 0.
     * @param pageSize The number of tokens to include in each page.
     * @param status The status of the tokens to filter by (e.g., active or revoked).
     * @return A page of tokens that match the specified status within the given page and size parameters.
     */
    Page<Token> findAllByStatus(int page, int pageSize, boolean status);
}
