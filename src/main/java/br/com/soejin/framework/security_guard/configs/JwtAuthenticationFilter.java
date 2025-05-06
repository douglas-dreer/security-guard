package br.com.soejin.framework.security_guard.configs;

import br.com.soejin.framework.security_guard.model.User;
import br.com.soejin.framework.security_guard.service.BlacklistService;
import br.com.soejin.framework.security_guard.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final BlacklistService blacklistService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil,
                                   UserDetailsService userDetailsService,
                                   BlacklistService blacklistService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.blacklistService = blacklistService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String jwt = extractToken(request);
        if (jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }

        final boolean hasBlacklist =  blacklistService.isBlacklisted(jwt);

        if (hasBlacklist) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido");
            return;
        }

        authenticateUser(jwt, request);
        filterChain.doFilter(request, response);
    }

    /**
     * Extrai o token do header "Authorization", removendo o prefixo "Bearer ".
     */
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        return (authHeader != null && authHeader.startsWith("Bearer "))
                ? authHeader.substring(7)
                : null;
    }

    /**
     * Realiza a autenticação do usuário caso o token seja válido.
     * Caso o token esteja expirado, adiciona-o à blacklist.
     */
    private void authenticateUser(String jwt, HttpServletRequest request) {
        String username = jwtUtil.extractUsername(jwt);
        if (username == null || SecurityContextHolder.getContext().getAuthentication() != null) {
            return;
        }

        User user = (User) userDetailsService.loadUserByUsername(username);

        if (jwtUtil.isTokenValid(jwt, user)) {
            setAuthentication(user, request);
        } else if (jwtUtil.isTokenExpired(jwt)) {
            blacklistService.addTokenToBlacklist(jwt, user.getId(), "Token expirado");
        }
    }

    /**
     * Configura o contexto de segurança com os dados do usuário autenticado.
     */
    private void setAuthentication(UserDetails userDetails, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
}
