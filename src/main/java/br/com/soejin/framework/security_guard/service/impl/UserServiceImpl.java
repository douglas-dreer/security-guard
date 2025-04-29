package br.com.soejin.framework.security_guard.service.impl;

import br.com.soejin.framework.security_guard.exception.NotFoundException;
import br.com.soejin.framework.security_guard.model.User;
import br.com.soejin.framework.security_guard.repository.UserRepository;
import br.com.soejin.framework.security_guard.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Set<String> roleDefault = Set.of("ROLE_USER");

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Create a new user
     * @param username username
     * @param email email
     * @param password password pure
     *
     * @see UserRepository#save(Object)
     */
    @Override
    @Transactional(rollbackOn = Exception.class)
    public User createUser(String username, String email, String password) {
        User user = User.builder()
                .username(username).email(email)
                .password(passwordEncoder.encode(password))
                .roles(roleDefault)
                .build();

        validateUserToCreate(user);

        return userRepository.save(user);
    }

    @Override
    public User updatePassword(String username, String password) {
        return null;
    }

    @Override
    public void deleteUser(String username) {

    }

    @Override
    public void updateLastLogin(User user) {
        user.setLastLogin(LocalDateTime.now());
    }

    /**
     * Valida se o usuário pode ser criado com os parâmetros passados
     * @param user entity {@link User}
     */
    private void validateUserToCreate(User user) {
        final boolean usernameExists = userRepository.existsByUsername(user.getUsername());
        final boolean emailExists = userRepository.existsByEmail(user.getEmail());

        if (usernameExists) {
            throw new NotFoundException("Username alright exist");
        }

        if (emailExists) {
            throw new NotFoundException("Email alright exist");
        }
    }
}
