package br.com.soejin.framework.security_guard.service.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.soejin.framework.security_guard.controller.request.CreateUserRequest;
import br.com.soejin.framework.security_guard.enums.RoleTypeEnum;
import br.com.soejin.framework.security_guard.exception.AlreadyDataRegisterException;
import br.com.soejin.framework.security_guard.factory.UserFactory;
import br.com.soejin.framework.security_guard.model.User;
import br.com.soejin.framework.security_guard.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository repository;

    @Spy
    private PasswordEncoder passwordEncoder;

    private final static RoleTypeEnum ROLE_ADMIN  = RoleTypeEnum.ROLE_ADMIN;
    private User userResponse = new User();
    private CreateUserRequest userRequest;

  
    @BeforeEach
    void setup() {
        userResponse = UserFactory.createUser();
        userRequest = UserFactory.createCreateUserRequest();        
    }

    /** Retorna sucesso tentar ao criar um novo usuário */
    @Test
    void mustReturnSuccessWhenCreateUser() {
        when(repository.save(any())).thenReturn(userResponse);
        when(repository.existsByUsername(anyString())).thenReturn(false);
        when(repository.existsByEmail(anyString())).thenReturn(false);

        User userSaved = userService.createUser(
            userRequest.username(),
            userRequest.email(),
            userRequest.password()
        );

        assertNotNull(userSaved);
        
        verify(repository, times(1)).save(any());
        verify(repository, times(1)).existsByEmail(anyString());
        verify(repository, times(1)).existsByUsername(anyString());
    }
    
    /** Retorna AlreadyDataRegisterException quando tenta criar um usuário com um email já existente  */
    @Test
    void mustReturnAlreadyDataRegisterExceptionWhenCreateUserWithEmailAlrightExist() {
        when(repository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(AlreadyDataRegisterException.class, () -> userService.createUser(
            userRequest.username(),
            userRequest.email(),
            userRequest.password()
        ));

        verify(repository, times(1)).existsByEmail(anyString());

    }

    /** Retorna AlreadyDataRegisterException quando tenta criar um usuário com um username já existente  */
    @Test
    void mustReturnAlreadyDataRegisterExceptionWhenCreateUserWithUsernameAlrightExist() {
        when(repository.existsByUsername(anyString())).thenReturn(true);

        assertThrows(AlreadyDataRegisterException.class, () -> userService.createUser(
            userRequest.username(),
            userRequest.email(),
            userRequest.password()
        ));

        verify(repository, times(1)).existsByUsername(anyString());
    }

    /** Retorna sucesso quando tenta atualizar o role do usuário **/
    @Test
    void mustReturnSuccessWhenUpdateRole() {
        final Long id = 1L;

        Optional<User> userOptional = Optional.ofNullable(userResponse);
        when(repository.findById(anyLong())).thenReturn(userOptional);
        when(repository.save(any())).thenReturn(userResponse);

        userService.updateRole(id, ROLE_ADMIN);
        
        verify(repository, times(1)).findById(anyLong());
        verify(repository, times(1)).save(any());
    }

    
}
