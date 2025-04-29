package br.com.soejin.framework.security_guard.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;

@Component
public class JwtUtil {
    private static final Logger logger = Logger.getLogger(JwtUtil.class.getName());

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.expiration}")
    private long jwtExpiration;

    @Value("${security.jwt.refresh-token.expiration}")
    private long refreshExpiration;

    public String extractUsername(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (JwtException e) {
            logger.warning("Erro ao extrair username do token: " + e.getMessage());
            return null;
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        try {
            Claims claims = extractAllClaims(token);
            return claimsResolver.apply(claims);
        } catch (JwtException e) {
            logger.warning("Erro ao extrair claims do token: " + e.getMessage());
            return null;
        }
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, refreshExpiration);
    }

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        try {
            Instant now = Instant.now();
            SecretKey key = getSignInKey();

            return Jwts.builder()
                    .header()
                    .and()
                    .claims(extraClaims)
                    .subject(userDetails.getUsername())
                    .issuedAt(Date.from(now))
                    .expiration(Date.from(now.plusMillis(expiration)))
                    .signWith(key)
                    .compact();
        } catch (Exception e) {
            logger.severe("Erro ao gerar token: " + e.getMessage());
            throw new JwtException("Erro ao gerar token", e);
        }
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);
            return username != null &&
                    username.equals(userDetails.getUsername()) &&
                    !isTokenExpired(token);
        } catch (JwtException e) {
            logger.warning("Token inválido: " + e.getMessage());
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(Date.from(Instant.now()));
        } catch (JwtException e) {
            logger.warning("Erro ao verificar expiração do token: " + e.getMessage());
            return true;
        }
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        try {
            SecretKey key = getSignInKey();
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            logger.warning("Erro ao extrair claims do token: " + e.getMessage());
            throw e;
        }
    }

    private SecretKey getSignInKey() {
        try {
            byte[] keyBytes = secretKey.getBytes();
            if (keyBytes.length < 64) {
                throw new IllegalArgumentException("A chave secreta deve ter pelo menos 64 bytes");
            }
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            logger.severe("Erro ao gerar chave de assinatura: " + e.getMessage());
            throw new JwtException("Erro ao gerar chave de assinatura", e);
        }
    }
}
