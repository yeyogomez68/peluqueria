package com.stilum.citas.application.auth;

import com.stilum.citas.application.auth.dto.LoginRequest;
import com.stilum.citas.application.auth.dto.LoginResponse;
import com.stilum.citas.domain.shared.AccesoNoAutorizadoException;
import com.stilum.citas.domain.user.User;
import com.stilum.citas.domain.user.UserRepository;
import com.stilum.citas.infrastructure.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: autenticación de usuarios.
 * SK-B-01: Application layer orquesta domain + infrastructure.
 */
@Service
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Autentica al usuario y genera un JWT.
     * RN-TENANT-001: el JWT incluye tenantId para el aislamiento automático.
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new AccesoNoAutorizadoException("Credenciales inválidas"));

        if (!user.isActivo()) {
            throw new AccesoNoAutorizadoException("El usuario está desactivado");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new AccesoNoAutorizadoException("Credenciales inválidas");
        }

        user.registrarLogin();
        userRepository.save(user);

        String token = jwtUtil.generarToken(
                user.getId(),
                user.getEmail(),
                user.getTenantId(),
                user.getRol()
        );

        String tenantNombre = null;
        if (user.getTenant() != null) {
            tenantNombre = user.getTenant().getNombreNegocio();
        }

        return new LoginResponse(
                token,
                user.getId(),
                user.getNombre(),
                user.getEmail(),
                user.getRol(),
                user.getTenantId(),
                tenantNombre
        );
    }
}
