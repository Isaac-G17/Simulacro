package com.stockbridge.inventory.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/** Lee "Authorization: Bearer <token>" y, si es válido, autentica la petición (stateless). */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            String token = header.substring(BEARER_PREFIX.length());
            // TODO:
            // tokenProvider.parse(token).ifPresent(user -> {
            //     var auth = new UsernamePasswordAuthenticationToken(
            //             user, null, List.of(new SimpleGrantedAuthority("ROLE_" + user.role().name())));
            //     SecurityContextHolder.getContext().setAuthentication(auth);
            // });
            // El principal es CurrentUser: en los controllers usa @AuthenticationPrincipal CurrentUser user
        }
        filterChain.doFilter(request, response);
    }
}
