package br.com.soejin.framework.security_guard.service.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import br.com.soejin.framework.security_guard.exception.TokenInvalidException;
import br.com.soejin.framework.security_guard.exception.TokenNotFoundException;
import br.com.soejin.framework.security_guard.factory.TokenFactory;
import br.com.soejin.framework.security_guard.factory.UserFactory;
import br.com.soejin.framework.security_guard.model.Token;
import br.com.soejin.framework.security_guard.model.User;
import br.com.soejin.framework.security_guard.repository.TokenRepository;
import br.com.soejin.framework.security_guard.util.JwtUtil;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class TokenServiceImplTest {
    @InjectMocks
    private TokenServiceImpl tokenService;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private BlacklistServiceImpl blacklistService;

    @Mock
    private TokenRepository repository;

    @Spy
    private JwtUtil jwtUtil;

    private Token entity = new Token();
    private User user = new User();
    private String TOKEN_FAKE = "";

    @BeforeEach
    void setup() {
        entity = TokenFactory.createToken();
        user = UserFactory.createUser();
        TOKEN_FAKE = entity.getToken();
    }

    /** Retorna sucesso quando tenta salvar um novo token */
    @Test
    void mustReturnSuccessWhenSave() {
        when(repository.save(any())).thenReturn(entity);

        Token tokenSaved = tokenService.save(entity);

        assertNotNull(tokenSaved);

        verify(repository, times(1)).save(any());        
    }

    /**  Retorna sucesso qunado tenta buscar pelo token */
    @Test
    void mustReturnSuccessWhenFindByToken() {
        Optional<Token> tokenOptional = Optional.of(entity);

        when(repository.findByToken(anyString())).thenReturn(tokenOptional);

        Token tokenFound = tokenService.findByToken(TOKEN_FAKE);

        assertNotNull(tokenFound);

        verify(repository, times(1)).findByToken(any());
    }

    /** Retorna TokenNotFoundException quando tenta buscar por token */
    @Test
    void mustReturnTokenNotFoundExceptionWhenFindByToken() {
        Optional<Token> tokenOptional = Optional.empty();
        
        when(repository.findByToken(anyString())).thenReturn(tokenOptional);

        assertThrows(TokenNotFoundException.class, () -> tokenService.findByToken(TOKEN_FAKE));
        verify(repository, times(1)).findByToken(anyString());
    }

    /** Retorna sucesso quando tenta desativar um token */
    @Test
    void mustReturnSuccessWhenDesactive() {
        
        final Optional<Token> tokenOptional = Optional.of(entity);
        
        when(repository.findByToken(anyString())).thenReturn(tokenOptional);
        doNothing().when(repository).delete(any());

        tokenService.desactive(TOKEN_FAKE);

        verify(repository, times(1)).findByToken(anyString());
        verify(repository, times(1)).delete(any());
    }

    /** Retorno TokenNotException quando tenta desativar um token */
    @Test
    void mustReturnTokenNotFoundWhenDesactive() {
        final String tokenFake = entity.getToken();
        final Optional<Token> tokenOptional = Optional.empty();

        when(repository.findByToken(anyString())).thenReturn(tokenOptional);

        assertThrows(TokenNotFoundException.class, () -> tokenService.desactive(TOKEN_FAKE));

        verify(repository, times(1)).findByToken(anyString());
    }

    /** Retorna sucesso quando tentar buscar todos os tokens por status */
    @Test
    void mustReturnSuccessWhenFindAllByStatus() {
        final int page = 1;
        final int pageSize = 50;
        final boolean status = true;
        final Pageable pageable = PageRequest.of(page, pageSize);
        final Page<Token> resultPaged = new PageImpl<>(List.of(entity));

        when(repository.findTokenByRevokedIs(pageable, status)).thenReturn(resultPaged);

        Page<Token> tokenPagined = tokenService.findAllByStatus(page, pageSize, status);

        assertNotNull(tokenPagined);
        verify(repository, times(1)).findTokenByRevokedIs(any(), anyBoolean());
    }

    /** Retorna sucesso quando buscar por usuário por id */
    @Test
    void mustReturnSuccessWhenTokenByUserId() {
        final Long USER_ID = 1L;
        when(repository.findTokenByUserIdAndRevokedFalse(anyLong())).thenReturn(entity);

        Token tokenFound = tokenService.tokenByUserId(USER_ID);

        assertNotNull(tokenFound);

        verify(repository, times(1)).findTokenByUserIdAndRevokedFalse(anyLong());
    }

    /** Retorno TokenInvalidException quando validar token de acesso nulo */
    @Test
    void mustReturnTokenInvalidExceptionValidadeAcessToken() throws TokenInvalidException {
        assertThrows(TokenInvalidException.class, () ->  tokenService.validateAccessToken(null));        
    }

    /** Retorno TokenInvalidException quando validar token de acesso está com usernanme nulo */
    @Test
    void mustReturnTokenInvalidExceptionValidadeAcessTokenWithUsernameIsNull() throws TokenInvalidException {
        when(blacklistService.isBlacklisted(anyString())).thenReturn(false);
        when(jwtUtil.extractUsername(anyString())).thenReturn(null);

        assertThrows(TokenInvalidException.class, () ->  tokenService.validateAccessToken(TOKEN_FAKE));        
        verify(blacklistService, times(1)).isBlacklisted(anyString());
        verify(jwtUtil, times(2)).extractUsername(anyString());
    }

    /** Retorno TokenInvalidException quando validar token de acesso está na blacklist */
    @Test
    void mustReturnTokenInvalidExceptionValidadeAcessTokenWithBlacklistTrue() throws TokenInvalidException {
        when(blacklistService.isBlacklisted(anyString())).thenReturn(false);
        when(jwtUtil.extractUsername(anyString())).thenReturn(null);

        assertThrows(TokenInvalidException.class, () ->  tokenService.validateAccessToken(TOKEN_FAKE));        
        verify(blacklistService, times(1)).isBlacklisted(anyString());
    }

     /** Retorno TokenInvalidException quando validar token de acesso está com usernanme nulo */
     @Test
     void mustReturnTokenInvalidExceptionValidadeAcessTokenWithNotFoundUser() throws TokenInvalidException {
         final String USERNAME = "john.doe";

         when(blacklistService.isBlacklisted(anyString())).thenReturn(false);
         when(userDetailsService.loadUserByUsername(anyString())).thenReturn(user);
         when(jwtUtil.extractUsername(anyString())).thenReturn(USERNAME);
         
         assertThrows(TokenInvalidException.class, () ->  tokenService.validateAccessToken(TOKEN_FAKE));
     }
  
}
