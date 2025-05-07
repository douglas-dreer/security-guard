package br.com.soejin.framework.security_guard.controller.api;

import br.com.soejin.framework.security_guard.controller.mapper.TokenMapper;
import br.com.soejin.framework.security_guard.controller.response.PageResponse;
import br.com.soejin.framework.security_guard.controller.response.TokenFullResponse;
import br.com.soejin.framework.security_guard.model.Token;
import br.com.soejin.framework.security_guard.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tokens")
@Tag(name = "Token", description = "API para gerenciamento de tokens")
public class TokenController {
    private final TokenService tokenService;
    private final TokenMapper tokenMapper;

    public TokenController(TokenService tokenService, TokenMapper tokenMapper) {
        this.tokenService = tokenService;
        this.tokenMapper = tokenMapper;
    }

    @Operation(
        summary = "Listar tokens",
        description = "Retorna uma lista paginada de tokens filtrados por status (requer permissão de administrador)",
        tags = {"Token"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Operação bem-sucedida",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PageResponse.class)
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
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping(params = {"page", "pageSize", "status"})
    public ResponseEntity<PageResponse<TokenFullResponse>> getTokens(
            @Parameter(description = "Número da página (começa em 0)", example = "0")
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,

            @Parameter(description = "Tamanho da página", example = "50")
            @RequestParam(value = "pageSize", required = false, defaultValue = "50") int pageSize,

            @Parameter(description = "Status do token (true = ativo, false = inativo)", example = "true")
            @RequestParam(value = "status", defaultValue = "true") boolean status
    ) {

        Page<Token> tokenPage = tokenService.findAllByStatus(page, pageSize, status);
        PageResponse<TokenFullResponse> pageResponse = tokenMapper.toPageResponse(tokenPage);
        return ResponseEntity.ok(pageResponse);
    }

}
