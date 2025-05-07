package br.com.soejin.framework.security_guard.service;

import br.com.soejin.framework.security_guard.exception.TokenInvalidException;
import br.com.soejin.framework.security_guard.model.Token;
import br.com.soejin.framework.security_guard.model.User;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Interface de serviço para gerenciamento de tokens.
 * Define operações para criação, validação, busca e gerenciamento de tokens de autenticação.
 */
public interface TokenService {
    /**
     * Busca uma entidade token com base no valor do token fornecido.
     * Este método é comumente usado para recuperar detalhes sobre um token,
     * como seu usuário associado, tipo e status de expiração.
     *
     * @param token A string única que representa o token a ser pesquisado.
     * @return A entidade Token associada à string fornecida, ou null se nenhum token existir.
     */
    Token findByToken(String token);

    /**
     * Busca um token pelo usuário fornecido que não esteja revogado.
     * 
     * @param userId {@link Long} ID do usuário.
     * @return O token válido para o usuário especificado.
     * @throws TokenInvalidException Se nenhum token válido for encontrado.
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
     * Recupera uma lista paginada de tokens com base em seu status.
     *
     * @param page O número da página a recuperar, começando de 0.
     * @param pageSize O número de tokens a incluir em cada página.
     * @param status O status dos tokens para filtrar (ex: ativo ou revogado).
     * @return Uma página de tokens que correspondem ao status especificado.
     */
    Page<Token> findAllByStatus(int page, int pageSize, boolean status);
    
    /**
     * Valida um token de acesso.
     * Verifica se o token é nulo ou vazio, se já está na blacklist e se é válido.
     * 
     * @param token Token de acesso a ser validado
     * @throws TokenInvalidException Se o token for inválido ou já estiver na blacklist
     */
    void validateAccessToken(String token) throws TokenInvalidException;
    
    /**
     * Valida um token de refresh.
     * Verifica se o token é nulo ou vazio, se já está na blacklist e se é válido.
     * 
     * @param refreshToken Token de refresh a ser validado
     * @throws TokenInvalidException Se o token for inválido ou já estiver na blacklist
     */
    void validateRefreshToken(String refreshToken) throws TokenInvalidException;
}
