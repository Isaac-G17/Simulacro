package com.stockbridge.inventory.infrastructure.security;

import com.stockbridge.inventory.application.port.out.TokenProviderPort;
import com.stockbridge.inventory.domain.model.CurrentUser;
import com.stockbridge.inventory.domain.model.User;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Component
public class JwtTokenProvider implements TokenProviderPort {

    private final SecretKey key;
    private final long expirationMs;

    public JwtTokenProvider(@Value("${app.jwt.secret}") String secret,
                            @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); // mínimo 32 caracteres
        this.expirationMs = expirationMs;
    }

    @Override
    public String generateToken(User user) {
        // TODO:
        // Date now = new Date();
        // return Jwts.builder()
        //         .subject(user.getEmail())
        //         .claim("uid", user.getId())
        //         .claim("role", user.getRole().name())
        //         .issuedAt(now)
        //         .expiration(new Date(now.getTime() + expirationMs))
        //         .signWith(key)
        //         .compact();
        throw new UnsupportedOperationException("TODO");
    }

    /** Valida firma y expiración. Si el token fue manipulado o expiró -> Optional.empty(). */
    public Optional<CurrentUser> parse(String token) {
        // TODO:
        // try {
        //     Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        //     return Optional.of(new CurrentUser(claims.get("uid", Long.class), claims.getSubject(),
        //             Role.valueOf(claims.get("role", String.class))));
        // } catch (JwtException | IllegalArgumentException e) {
        //     return Optional.empty();
        // }
        return Optional.empty();
    }
}
