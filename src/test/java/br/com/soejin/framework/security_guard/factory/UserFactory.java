package br.com.soejin.framework.security_guard.factory;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import br.com.soejin.framework.security_guard.controller.request.CreateUserRequest;
import br.com.soejin.framework.security_guard.model.User;

public class UserFactory {
    
    private static final Long id = 1L;
    private static final String username = "joe.collien";
    private static final String email = "joe.collien@email.com";
    private static final String password = "j03.C07713n";
    private static final LocalDateTime createdAt = LocalDateTime.now().minusMonths(6);
    private static final LocalDateTime updatedAt = LocalDateTime.now().minusDays(7);
    private static final LocalDateTime lastLogin = LocalDateTime.now();
    private static final LocalDateTime passwordResetExpires = createdAt.plusMonths(3);
    private static final Set<String> roles = new HashSet<>();
    private static final boolean accountNonExpired = true;
    private static final boolean accountNonLocked = true;
    private static final boolean credentialsNonExpired = true;
    private static final boolean enabled = true;

    
    /**
     * Cria um instancia completa e preenchida de {@link User}
     * @return user
     */
    public static User createUser() {
        roles.add("ROLE_USER");
        return User.builder()
            .id(id)
            .username(username)
            .password(password)
            .email(email)
            .roles(roles)
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .lastLogin(lastLogin)
            .passwordResetExpires(passwordResetExpires)
            .accountNonExpired(accountNonExpired)
            .accountNonLocked(accountNonLocked)
            .credentialsNonExpired(credentialsNonExpired)
            .enabled(enabled)
        .build();
    }

    public static User createUserWithMininalInfo() {
        return User.builder()
            .username(username)
            .email(email)
            .password(password)
        .build();
    }

    /**
     * Cria uma instancia completa e preenchida de {@Link CreateUserRequest}
     * @return
     */
    public static CreateUserRequest createCreateUserRequest() {
        return new CreateUserRequest(username, email, password);
    }
}
