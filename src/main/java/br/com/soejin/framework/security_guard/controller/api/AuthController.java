package br.com.soejin.framework.security_guard.controller.api;


import br.com.soejin.framework.security_guard.controller.request.CreateUserRequest;
import br.com.soejin.framework.security_guard.controller.request.LoginRequest;
import br.com.soejin.framework.security_guard.controller.response.TokenResponse;
import br.com.soejin.framework.security_guard.service.AuthService;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService service;

    public AuthController(AuthService authService) {
        this.service = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuário", description = "Endpoint para realizar o login do usuário no sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login realizado com sucesso",
            content = @Content(schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "400", description = "Credenciais inválidas"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid LoginRequest request) throws BadRequestException {
        TokenResponse tokenResponse = service.authenticate(request);
        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/logout")
    @Operation(summary = "Desconectar usuário", description = "Endpoint para realizar o logout do usuário do sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logout realizado com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<String> logout() {
        return ResponseEntity.ok("Logout realizado com sucesso");
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Atualizar token JWT", description = "Endpoint para atualizar o token de autenticação JWT")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token atualizado com sucesso"),
        @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<String> refreshToken() {
        return ResponseEntity.ok("Token atualizado com sucesso");
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar novo usuário", description = "Endpoint para criar uma nova conta de usuário no sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuário registrado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou usuário já existe"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public void register(@RequestBody @Valid CreateUserRequest request) {
        service.createUser(request);
    }
}
