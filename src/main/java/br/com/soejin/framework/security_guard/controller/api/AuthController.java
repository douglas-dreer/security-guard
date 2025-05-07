package br.com.soejin.framework.security_guard.controller.api;

import br.com.soejin.framework.security_guard.controller.request.CreateUserRequest;
import br.com.soejin.framework.security_guard.controller.request.LoginRequest;
import br.com.soejin.framework.security_guard.controller.response.MessageResponse;
import br.com.soejin.framework.security_guard.controller.response.TokenResponse;
import br.com.soejin.framework.security_guard.service.AuthService;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.time.LocalDateTime;

/**
 * Controlador REST para gerenciar operações de autenticação.
 * Fornece endpoints para login, logout, atualização de token e registro de usuários.
 * 
 * @see br.com.soejin.framework.security_guard.service.AuthService
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "API para autenticação e registro de usuários")
public class AuthController {
    private final AuthService authService;

    /**
     * Construtor com injeção de dependência via construtor.
     * 
     * @param authService O serviço de autenticação
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Autentica um usuário e retorna um token JWT.
     * 
     * @param request O pedido de login contendo username e senha
     * @return ResponseEntity contendo o token JWT
     * @throws BadRequestException Se as credenciais forem inválidas
     */
    @PostMapping("/login")
    @Operation(summary = "Autenticar usuário", description = "Endpoint para realizar o login do usuário no sistema")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login realizado com sucesso",
            content = @Content(schema = @Schema(implementation = TokenResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Credenciais inválidas"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Não autorizado"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<TokenResponse> login(
            @Parameter(description = "Credenciais de login", required = true)
            @RequestBody @Valid LoginRequest request) throws BadRequestException {
        TokenResponse tokenResponse = authService.authenticate(request);
        return ResponseEntity.ok(tokenResponse);
    }

    /**
     * Desconecta um usuário invalidando seu token JWT.
     * 
     * @param token O token JWT a ser invalidado (do cabeçalho Authorization)
     * @return ResponseEntity com uma mensagem de sucesso
     */
    @PostMapping("/logout")
    @Operation(summary = "Desconectar usuário", description = "Endpoint para realizar o logout do usuário do sistema")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Logout realizado com sucesso"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Não autorizado"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<MessageResponse> logout(
            @Parameter(description = "Token JWT de autenticação (Bearer token)", required = true)
            @RequestHeader("Authorization") String token) {
        authService.logout(token.replace("Bearer ", ""));
        return ResponseEntity.ok(new MessageResponse(
            "Sucesso",
            "Logout realizado com sucesso",
            HttpStatus.OK.value(),
            LocalDateTime.now()
        ));
    }

    /**
     * Atualiza um token JWT usando um token de atualização.
     * 
     * @param refreshToken O token de atualização (do cabeçalho Authorization)
     * @return ResponseEntity contendo o novo token JWT
     */
    @PostMapping("/refresh-token")
    @Operation(summary = "Atualizar token JWT", description = "Endpoint para atualizar o token de autenticação JWT")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token atualizado com sucesso"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<TokenResponse> refreshToken(
            @Parameter(description = "Token de atualização (Bearer token)", required = true)
            @RequestHeader("Authorization") String refreshToken) {
        TokenResponse newToken = authService.refreshToken(refreshToken.replace("Bearer ", ""));
        return ResponseEntity.ok(newToken);
    }

    /**
     * Registra um novo usuário no sistema.
     * 
     * @param request Os dados do usuário para registro
     * @return ResponseEntity com o status da operação
     */
    @PostMapping("/register")
    @Operation(summary = "Registrar novo usuário", description = "Endpoint para criar uma nova conta de usuário no sistema")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Usuário registrado com sucesso"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dados inválidos ou usuário já existe"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<MessageResponse> register(
            @Parameter(description = "Dados do novo usuário", required = true)
            @RequestBody @Valid CreateUserRequest request) {
        authService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new MessageResponse(
                "Sucesso",
                "Usuário registrado com sucesso",
                HttpStatus.CREATED.value(),
                LocalDateTime.now()
            ));
    }
}
