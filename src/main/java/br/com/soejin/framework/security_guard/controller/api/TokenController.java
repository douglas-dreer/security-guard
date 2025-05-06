package br.com.soejin.framework.security_guard.controller.api;

import br.com.soejin.framework.security_guard.controller.mapper.TokenMapper;
import br.com.soejin.framework.security_guard.controller.response.PageResponse;
import br.com.soejin.framework.security_guard.controller.response.TokenFullResponse;
import br.com.soejin.framework.security_guard.model.Token;
import br.com.soejin.framework.security_guard.service.TokenService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tokens")
public class TokenController {
    private final TokenService tokenService;
    private final TokenMapper tokenMapper;

    public TokenController(TokenService tokenService, TokenMapper tokenMapper) {
        this.tokenService = tokenService;
        this.tokenMapper = tokenMapper;
    }

    @GetMapping(params = {"page", "pageSize", "status"})
    public ResponseEntity<PageResponse<TokenFullResponse>> getTokens(
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "pageSize", required = false, defaultValue = "50") int pageSize,
            @RequestParam(value = "status", defaultValue = "true") boolean status
    ) {

        Page<Token> tokenPage = tokenService.findAllByStatus(page, pageSize, status);
        PageResponse<TokenFullResponse> pageResponse = tokenMapper.toPageResponse(tokenPage);
        return ResponseEntity.ok(pageResponse);
    }
}
