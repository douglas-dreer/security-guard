package br.com.soejin.framework.security_guard.controller.api;

import br.com.soejin.framework.security_guard.controller.mapper.UserMapper;
import br.com.soejin.framework.security_guard.controller.request.UpdateRoleUserRequest;
import br.com.soejin.framework.security_guard.controller.response.MessageResponse;
import br.com.soejin.framework.security_guard.controller.response.UserResponse;
import br.com.soejin.framework.security_guard.model.User;
import br.com.soejin.framework.security_guard.service.UserService;
import br.com.soejin.framework.security_guard.util.JwtUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/users")
@Tag(name = "User", description = "API para gerenciamento de usuários")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }


    @Operation(
        summary = "Obter informações do usuário atual",
        description = "Retorna as informações do usuário autenticado",
        tags = {"User"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Operação bem-sucedida",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Não autorizado",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Acesso proibido",
            content = @Content
        )
    })
    @GetMapping("/me")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<UserResponse> getMyInfos(
            @Parameter(description = "Detalhes do usuário autenticado", hidden = true)
            @AuthenticationPrincipal UserDetails userDetails) {
        String userId = userDetails.getUsername();
        User userFound = userService.findByUsername(userId);
        UserResponse userResponse = userMapper.toResponse(userFound);
        return ResponseEntity.ok(userResponse);
    }

    @Operation(
        summary = "Atualizar role do usuário",
        description = "Atualiza a role de um usuário específico (requer permissão de administrador)",
        tags = {"User"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Role atualizado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = MessageResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Requisição inválida",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Não autorizado",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Acesso proibido",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Usuário não encontrado",
            content = @Content
        )
    })
    @PostMapping("/{userId}/role")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<MessageResponse> updateUserRole(
            @Parameter(description = "ID do usuário", required = true)
            @PathVariable("userId") Long userId,

            @Parameter(description = "Dados da nova role", required = true)
            @Valid @NotNull @RequestBody UpdateRoleUserRequest updateRoleUser
    ) {
        userService.updateRole(userId, updateRoleUser.role());
        MessageResponse messageResponse = createMessageResponseSuccess("Role atualizado com sucesso");
        return ResponseEntity.ok(messageResponse);
    }

    private MessageResponse createMessageResponseSuccess(String message) {
        return new MessageResponse(
                "Operação realizada com sucesso",
                message,
                HttpStatus.OK.value(),
                LocalDateTime.now()
        );
    }
}
