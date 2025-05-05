package br.com.soejin.framework.security_guard.service.impl;

import br.com.soejin.framework.security_guard.controller.request.LoginRequest;
import br.com.soejin.framework.security_guard.controller.response.TokenResponse;
import br.com.soejin.framework.security_guard.exception.BadCredentialsException;
import br.com.soejin.framework.security_guard.model.User;
import br.com.soejin.framework.security_guard.service.BlacklistService;
import br.com.soejin.framework.security_guard.service.UserService;
import br.com.soejin.framework.security_guard.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private BlacklistService blacklistService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private LoginRequest loginRequest;
    private String testToken;
    private String testRefreshToken;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");

        loginRequest = new LoginRequest("test@example.com", "password");
        
        testToken = "test.access.token";
        testRefreshToken = "test.refresh.token";
    }

    @Test
    void authenticate_Success() throws Exception {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(jwtUtil.generateToken(testUser)).thenReturn(testToken);
        when(jwtUtil.generateRefreshToken(testUser)).thenReturn(testRefreshToken);

        // Act
        TokenResponse response = authService.authenticate(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals(testToken, response.accessToken());
        assertEquals(testRefreshToken, response.refreshToken());
        verify(userService).updateLastLogin(testUser);
    }

    @Test
    void authenticate_Failure() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new org.springframework.security.authentication.BadCredentialsException("Bad credentials"));

        // Act & Assert
        assertThrows(org.apache.coyote.BadRequestException.class, () -> {
            authService.authenticate(loginRequest);
        });
    }

    @Test
    void refreshToken_Success() {
        // Arrange
        when(jwtUtil.extractUsername(testRefreshToken)).thenReturn("test@example.com");
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(testUser);
        when(jwtUtil.isTokenValid(testRefreshToken, testUser)).thenReturn(true);
        when(jwtUtil.generateToken(testUser)).thenReturn(testToken);
        when(jwtUtil.generateRefreshToken(testUser)).thenReturn("new.refresh.token");
        when(blacklistService.isBlacklisted(testRefreshToken)).thenReturn(false);

        // Act
        TokenResponse response = authService.refreshToken(testRefreshToken);

        // Assert
        assertNotNull(response);
        assertEquals(testToken, response.accessToken());
        assertEquals("new.refresh.token", response.refreshToken());
        verify(blacklistService).addTokenToBlacklist(testRefreshToken);
    }

    @Test
    void refreshToken_InvalidToken() {
        // Arrange
        when(jwtUtil.extractUsername(testRefreshToken)).thenReturn(null);

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> {
            authService.refreshToken(testRefreshToken);
        });
    }

    @Test
    void refreshToken_BlacklistedToken() {
        // Arrange
        when(blacklistService.isBlacklisted(testRefreshToken)).thenReturn(true);

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> {
            authService.refreshToken(testRefreshToken);
        });
    }

    @Test
    void logout_Success() {
        // Arrange
        when(blacklistService.isBlacklisted(testToken)).thenReturn(false);

        // Act
        authService.logout(testToken);

        // Assert
        verify(blacklistService).addTokenToBlacklist(testToken);
    }

    @Test
    void logout_AlreadyBlacklisted() {
        // Arrange
        when(blacklistService.isBlacklisted(testToken)).thenReturn(true);

        // Act
        authService.logout(testToken);

        // Assert
        verify(blacklistService, never()).addTokenToBlacklist(testToken);
    }

    @Test
    void logout_NullToken() {
        // Act
        authService.logout(null);

        // Assert
        verify(blacklistService, never()).addTokenToBlacklist(anyString());
    }
}