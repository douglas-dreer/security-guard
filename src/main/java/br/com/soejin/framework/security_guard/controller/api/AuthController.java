package br.com.soejin.framework.security_guard.controller.api;


import br.com.soejin.framework.security_guard.controller.request.CreateUserRequest;
import br.com.soejin.framework.security_guard.controller.request.LoginRequest;
import br.com.soejin.framework.security_guard.controller.response.TokenResponse;
import br.com.soejin.framework.security_guard.service.AuthService;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * REST controller for handling authentication-related operations.
 * Provides endpoints for user login, logout, token refresh, and registration.
 * 
 * @see br.com.soejin.framework.security_guard.service.AuthService
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "API para autenticação e registro de usuários")
public class AuthController {
    private final AuthService service;

    /**
     * Constructs an AuthController with the specified AuthService.
     * 
     * @param authService The service that handles authentication operations
     */
    public AuthController(AuthService authService) {
        this.service = authService;
    }

    /**
     * Authenticates a user and returns a JWT token.
     * 
     * @param request The login request containing username and password
     * @return ResponseEntity containing the JWT token
     * @throws BadRequestException If the credentials are invalid
     * @see br.com.soejin.framework.security_guard.controller.request.LoginRequest
     * @see br.com.soejin.framework.security_guard.controller.response.TokenResponse
     * @see br.com.soejin.framework.security_guard.service.AuthService#authenticate(LoginRequest)
     */
    @PostMapping("/login")
    @Operation(summary = "Autenticar usuário", description = "Endpoint para realizar o login do usuário no sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login realizado com sucesso",
            content = @Content(schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "400", description = "Credenciais inválidas"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<TokenResponse> login(
            @Parameter(description = "Credenciais de login", required = true)
            @RequestBody @Valid LoginRequest request) throws BadRequestException {
        TokenResponse tokenResponse = service.authenticate(request);
        return ResponseEntity.ok(tokenResponse);
    }

    /**
     * Logs out a user by invalidating their JWT token.
     * 
     * @param token The JWT token to invalidate (from Authorization header)
     * @return ResponseEntity with a success message
     * @see br.com.soejin.framework.security_guard.service.AuthService#logout(String)
     */
    @PostMapping("/logout")
    @Operation(summary = "Desconectar usuário", description = "Endpoint para realizar o logout do usuário do sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logout realizado com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<String> logout(
            @Parameter(description = "Token JWT de autenticação (Bearer token)", required = true)
            @RequestHeader("Authorization") String token) {
        service.logout(token.replace("Bearer ", ""));
        return ResponseEntity.ok("Logout realizado com sucesso");
    }

    /**
     * Refreshes a JWT token using a refresh token.
     * 
     * @param refreshToken The refresh token (from Authorization header)
     * @return ResponseEntity containing the new JWT token
     * @see br.com.soejin.framework.security_guard.controller.response.TokenResponse
     * @see br.com.soejin.framework.security_guard.service.AuthService#refreshToken(String)
     */
    @PostMapping("/refresh-token")
    @Operation(summary = "Atualizar token JWT", description = "Endpoint para atualizar o token de autenticação JWT")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token atualizado com sucesso"),
        @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<TokenResponse> refreshToken(
            @Parameter(description = "Token de atualização (Bearer token)", required = true)
            @RequestHeader("Authorization") String refreshToken) {
        TokenResponse newToken = service.refreshToken(refreshToken.replace("Bearer ", ""));
        return ResponseEntity.ok(newToken);
    }

    /**
     * Registers a new user in the system.
     * 
     * @param request The request containing user details for registration
     * @see br.com.soejin.framework.security_guard.controller.request.CreateUserRequest
     * @see br.com.soejin.framework.security_guard.service.AuthService#createUser(CreateUserRequest)
     */
    @PostMapping("/register")
    @Operation(summary = "Registrar novo usuário", description = "Endpoint para criar uma nova conta de usuário no sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuário registrado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou usuário já existe"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public void register(
            @Parameter(description = "Dados do novo usuário", required = true)
            @RequestBody @Valid CreateUserRequest request) {
        service.createUser(request);
    }
}
