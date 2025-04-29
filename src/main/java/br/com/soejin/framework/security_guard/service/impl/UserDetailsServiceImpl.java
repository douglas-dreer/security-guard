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

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserDetailsServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public UserResponse createUser(CreateUserRequest createUserRequest) {
        User user = userMapper.toEntity(createUserRequest);
        userRepository.save(user);
        return userMapper.toResponse(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository
                .findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }

}
