package br.com.soejin.framework.security_guard.service.impl;

import br.com.soejin.framework.security_guard.controller.mapper.UserMapper;
import br.com.soejin.framework.security_guard.controller.request.CreateUserRequest;
import br.com.soejin.framework.security_guard.controller.response.UserResponse;
import br.com.soejin.framework.security_guard.model.User;
import br.com.soejin.framework.security_guard.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implementação do serviço de detalhes do usuário.
 * Esta classe fornece a implementação concreta do UserDetailsService do Spring Security,
 * responsável por carregar os detalhes do usuário durante o processo de autenticação.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * Construtor da classe UserDetailsServiceImpl.
     *
     * @param userRepository Repositório de usuários
     * @param userMapper Mapper para conversão entre entidades e DTOs
     */
    public UserDetailsServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    /**
     * Cria um novo usuário no sistema.
     * Converte a requisição em uma entidade User, salva no banco de dados
     * e retorna a resposta com os dados do usuário criado.
     *
     * @param createUserRequest Dados do usuário a ser criado
     * @return Resposta contendo os dados do usuário criado
     */
    public UserResponse createUser(CreateUserRequest createUserRequest) {
        User user = userMapper.toEntity(createUserRequest);
        userRepository.save(user);
        return userMapper.toResponse(user);
    }

    /**
     * Carrega os detalhes do usuário pelo email.
     * Este método é usado pelo Spring Security durante o processo de autenticação.
     *
     * @param username Email do usuário a ser carregado
     * @return Detalhes do usuário
     * @throws UsernameNotFoundException Se o usuário não for encontrado
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }

    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }
}
