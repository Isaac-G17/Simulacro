package com.stockbridge.inventory.infrastructure.security;

import com.stockbridge.inventory.domain.model.CurrentUser;
import com.stockbridge.inventory.domain.model.Role;
import com.stockbridge.inventory.domain.model.User;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

/** Sin Spring: el provider se construye a mano con un secreto de prueba. */
class JwtTokenProviderTest {

    // 60 caracteres, como el secreto por defecto: sin forzar el algoritmo, JJWT elegiría HS384
    private static final String SECRET = "cambia-este-secreto-por-uno-de-al-menos-32-caracteres-123456";

    private final JwtTokenProvider provider = new JwtTokenProvider(SECRET, 3_600_000);
    private final User user = new User(7L, "Ana", "ana@test.com", "$2a$hash", Role.BODEGA, true);

    @Test
    void generateToken_shouldUseHS256() {
        String header = new String(Base64.getUrlDecoder().decode(provider.generateToken(user).split("\\.")[0]),
                StandardCharsets.UTF_8);

        assertThat(header).contains("\"alg\":\"HS256\"");
    }

    @Test
    void parse_shouldReturnUserFromValidToken() {
        assertThat(provider.parse(provider.generateToken(user)))
                .contains(new CurrentUser(7L, "ana@test.com", Role.BODEGA));
    }

    @Test
    void parse_shouldRejectTamperedToken() {
        String token = provider.generateToken(user);
        String tampered = token.substring(0, token.length() - 2) + (token.endsWith("AA") ? "BB" : "AA");

        assertThat(provider.parse(tampered)).isEmpty();
    }

    @Test
    void parse_shouldRejectTokenSignedWithAnotherSecret() {
        var other = new JwtTokenProvider("otro-secreto-distinto-de-al-menos-32-caracteres!!", 3_600_000);

        assertThat(provider.parse(other.generateToken(user))).isEmpty();
    }

    @Test
    void parse_shouldRejectExpiredToken() {
        var expired = new JwtTokenProvider(SECRET, -1_000);

        assertThat(provider.parse(expired.generateToken(user))).isEmpty();
    }
}
