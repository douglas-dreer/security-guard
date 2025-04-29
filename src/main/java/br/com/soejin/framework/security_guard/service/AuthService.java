package br.com.soejin.framework.security_guard.service;


import br.com.soejin.framework.security_guard.controller.request.CreateUserRequest;
import br.com.soejin.framework.security_guard.controller.request.LoginRequest;
import br.com.soejin.framework.security_guard.controller.response.TokenResponse;
import org.apache.coyote.BadRequestException;

public interface AuthService {
    TokenResponse authenticate(LoginRequest request) throws BadRequestException;
    TokenResponse refreshToken(String refreshToken);
    void logout(String token);
    void createUser(CreateUserRequest request);
}
