package com.stockbridge.inventory.infrastructure.security;

import com.stockbridge.inventory.application.port.out.TokenProviderPort;
import com.stockbridge.inventory.domain.model.CurrentUser;
import com.stockbridge.inventory.domain.model.Role;
import com.stockbridge.inventory.domain.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Component
public class JwtTokenProvider implements TokenProviderPort {

    private static final String CLAIM_USER_ID = "uid";
    private static final String CLAIM_ROLE = "role";

    private final SecretKey key;
    private final long expirationMs;

    public JwtTokenProvider(@Value("${app.jwt.secret}") String secret,
                            @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); // mínimo 32 caracteres
        this.expirationMs = expirationMs;
    }

    @Override
    public String generateToken(User user) {
        Date now = new Date();
        return Jwts.builder()
                .subject(user.getEmail())
                .claim(CLAIM_USER_ID, user.getId())
                .claim(CLAIM_ROLE, user.getRole().name())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                // HMAC: la firma depende del contenido y del secreto. JJWT elige HS256/384/512 según el
                // largo de la clave (el secreto por defecto, de 60 caracteres, da HS384)
                .signWith(key)
                .compact();
    }

    /** Valida firma y expiración. Si el token fue manipulado o expiró -> Optional.empty(). */
    public Optional<CurrentUser> parse(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            return Optional.of(new CurrentUser(claims.get(CLAIM_USER_ID, Long.class), claims.getSubject(),
                    Role.valueOf(claims.get(CLAIM_ROLE, String.class))));
        } catch (JwtException | IllegalArgumentException | NullPointerException e) {
            // Firma inválida, token expirado, mal formado o con claims faltantes/desconocidos
            log.warn("Token JWT rechazado: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
