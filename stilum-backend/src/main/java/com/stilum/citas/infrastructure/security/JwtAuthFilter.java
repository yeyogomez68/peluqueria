package com.stilum.citas.infrastructure.security;

import com.stilum.citas.domain.user.UserRol;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Filtro JWT: extrae el token del header Authorization, valida, y configura
 * el SecurityContext + TenantContext para el request actual.
 * SK-B-11 / RN-TENANT-001.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String token = extraerToken(request);

            if (token != null && jwtUtil.esValido(token)) {
                String email = jwtUtil.extraerEmail(token);
                UserRol rol = jwtUtil.extraerRol(token);
                UUID tenantId = jwtUtil.extraerTenantId(token);

                // Inyectar tenantId en el contexto del hilo (para Hibernate @Filter)
                if (tenantId != null) {
                    TenantContext.set(tenantId);
                }

                // Configurar autenticación en Spring Security
                var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
                var auth = new UsernamePasswordAuthenticationToken(email, null, authorities);
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (Exception e) {
            // Token inválido → continúa sin autenticación
            SecurityContextHolder.clearContext();
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Siempre limpiar TenantContext al finalizar el request
            TenantContext.clear();
        }
    }

    private String extraerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
