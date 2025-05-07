package br.com.soejin.framework.security_guard.service.impl;

import br.com.soejin.framework.security_guard.enums.RoleTypeEnum;
import br.com.soejin.framework.security_guard.exception.AlreadyDataRegisterException;
import br.com.soejin.framework.security_guard.exception.NotFoundException;
import br.com.soejin.framework.security_guard.exception.UserNotFoundException;
import br.com.soejin.framework.security_guard.model.User;
import br.com.soejin.framework.security_guard.repository.UserRepository;
import br.com.soejin.framework.security_guard.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Implementação do serviço de usuários.
 * Esta classe fornece a implementação concreta dos métodos definidos na interface UserService,
 * incluindo criação, atualização e exclusão de usuários.
 */
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Set<String> roleDefault = Set.of("ROLE_USER");

    /**
     * Construtor da classe UserServiceImpl.
     *
     * @param userRepository Repositório de usuários
     * @param passwordEncoder Codificador de senhas
     */
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Cria um novo usuário no sistema.
     * O método valida se o username e email já existem antes de criar o usuário.
     * A senha é codificada antes de ser armazenada.
     *
     * @param username Nome de usuário único
     * @param email Email do usuário
     * @param password Senha do usuário em texto puro
     * @return O usuário criado
     * @throws NotFoundException Se o username ou email já existirem
     * @see UserRepository#save(Object)
     */
    @Override
    @Transactional(rollbackOn = Exception.class)
    public User createUser(String username, String email, String password) {
        User user = User.builder()
                .username(username).email(email)
                .password(passwordEncoder.encode(password))
                .roles(roleDefault)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();

            validateUserToCreate(user);

        return userRepository.save(user);
    }

    /**
     * Atualiza a senha de um usuário existente.
     *
     * @param username Nome de usuário do usuário a ter a senha atualizada
     * @param password Nova senha do usuário
     * @return O usuário com a senha atualizada
     */
    @Override
    public User updatePassword(String username, String password) {
        return null;
    }

    /**
     * Remove um usuário do sistema.
     *
     * @param username Nome de usuário do usuário a ser removido
     */
    @Override
    public void deleteUser(String username) {

    }

    /**
     * Atualiza o registro do último login do usuário.
     *
     * @param user Usuário que realizou o login
     */
    @Override
    public void updateLastLogin(User user) {
        user.setLastLogin(LocalDateTime.now());
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }

    
    /**
     * Atualiza a role de um usuário existente.
     * 
     * @param userId ID do usuário que terá a role atualizada
     * @param role Nova role a ser adicionada ao usuário
     * @throws UserNotFoundException Se não encontrar um usuário com o ID informado
     */
    @Override
    public void updateRole(Long userId, RoleTypeEnum role) {
        User userFound = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException("Usuário não encontrado com o ID " + userId)
        );

        userFound.addRole(role.getRole());
        userRepository.save(userFound);
    }

    /**
     * Valida se o usuário pode ser criado com os parâmetros passados.
     * Verifica se o username e email já existem no sistema.
     *
     * @param user Entidade User a ser validada
     * @throws NotFoundException Se o username ou email já existirem
     */
    private void validateUserToCreate(User user) {
        final boolean usernameExists = userRepository.existsByUsername(user.getUsername());
        final boolean emailExists = userRepository.existsByEmail(user.getEmail());

        if (usernameExists) {
            throw new AlreadyDataRegisterException("Username alright exist");
        }

        if (emailExists) {
            throw new AlreadyDataRegisterException("Email alright exist");
        }
    }
}
