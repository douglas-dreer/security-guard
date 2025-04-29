package br.com.soejin.framework.security_guard.service.impl;


import br.com.soejin.framework.security_guard.controller.request.CreateUserRequest;
import br.com.soejin.framework.security_guard.controller.request.LoginRequest;
import br.com.soejin.framework.security_guard.controller.response.TokenResponse;
import br.com.soejin.framework.security_guard.model.User;
import br.com.soejin.framework.security_guard.service.AuthService;
import br.com.soejin.framework.security_guard.service.UserService;
import br.com.soejin.framework.security_guard.util.JwtUtil;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.apache.coyote.BadRequestException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final UserDetailsServiceImpl userDetailsServiceImpl;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(UserService userService, UserDetailsServiceImpl userDetailsServiceImpl, AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.userService = userService;
        this.userDetailsServiceImpl = userDetailsServiceImpl;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }


    /**
     * Do authenticate the user with username and password
     * @param request {@link LoginRequest }
     * @return {@link TokenResponse}
     */
    @Override
    @Transactional(rollbackOn = Exception.class)
    public TokenResponse authenticate(LoginRequest request) throws BadRequestException {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.username(),
                            request.password()
                    )
            );

            User user = (User) authentication.getPrincipal();
            userService.updateLastLogin(user);

            return createTokenResponse(user);
        } catch (AuthenticationException e) {
            throw new BadRequestException("Invalid username/password supplied");
        }
    }

    @Override
    public TokenResponse refreshToken(String refreshToken) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'refreshToken'");
    }

    @Override
    public void logout(String token) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'logout'");
    }

    /**
     * Registra um novo usuário no sistema
     * @param request Dados do usuário a ser registrado
     */

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void createUser(@Valid @NotNull CreateUserRequest request) {
        User userSaved = userService.createUser(
                request.username(),
                request.email(),
                request.password()
        );
    }

    private TokenResponse createTokenResponse(User user) {
        String accessToken = jwtUtil.generateToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);
        return new TokenResponse(accessToken, refreshToken);
    }


}
