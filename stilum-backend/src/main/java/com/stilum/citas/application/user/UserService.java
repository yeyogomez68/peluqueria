package com.stilum.citas.application.user;

import com.stilum.citas.application.user.dto.CreateUserRequest;
import com.stilum.citas.application.user.dto.UserResponse;
import com.stilum.citas.domain.shared.AccesoNoAutorizadoException;
import com.stilum.citas.domain.shared.ConflictoException;
import com.stilum.citas.domain.shared.RecursoNoEncontradoException;
import com.stilum.citas.domain.tenant.Tenant;
import com.stilum.citas.domain.tenant.TenantRepository;
import com.stilum.citas.domain.user.User;
import com.stilum.citas.domain.user.UserRepository;
import com.stilum.citas.domain.user.UserRol;
import com.stilum.citas.infrastructure.security.TenantContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Caso de uso: gestión de usuarios dentro de un tenant.
 * RN-TENANT-001: solo puede ver y gestionar usuarios de su propio tenant.
 */
@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       TenantRepository tenantRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Lista usuarios del tenant actual (extraído del TenantContext). */
    public List<UserResponse> listar() {
        UUID tenantId = requireTenantContext();
        return userRepository.findAllByTenantId(tenantId).stream()
                .map(this::toResponse)
                .toList();
    }

    /** Obtiene un usuario específico — verifica que pertenezca al tenant del caller. */
    public UserResponse obtener(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RecursoNoEncontradoException("User", userId));
        verificarPertenencia(user);
        return toResponse(user);
    }

    /** Crea un nuevo usuario (ADMIN_TENANT o PROFESIONAL) dentro del tenant actual. */
    @Transactional
    public UserResponse crear(CreateUserRequest req) {
        UUID tenantId = requireTenantContext();

        if (req.rol() == UserRol.SUPER_ADMIN) {
            throw new AccesoNoAutorizadoException("No se puede crear un SUPER_ADMIN desde este endpoint");
        }
        if (userRepository.existsByEmail(req.email())) {
            throw new ConflictoException("EMAIL_DUPLICADO", "El email ya está en uso");
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Tenant", tenantId));

        User user = User.crearParaTenant(
                tenant, req.nombre(), req.email(),
                passwordEncoder.encode(req.password()), req.rol()
        );
        return toResponse(userRepository.save(user));
    }

    /** Desactiva un usuario del tenant actual. */
    @Transactional
    public UserResponse desactivar(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RecursoNoEncontradoException("User", userId));
        verificarPertenencia(user);
        user.desactivar();
        return toResponse(userRepository.save(user));
    }

    /** Reactiva un usuario desactivado del tenant actual. */
    @Transactional
    public UserResponse activar(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RecursoNoEncontradoException("User", userId));
        verificarPertenencia(user);
        user.activar();
        return toResponse(userRepository.save(user));
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private UUID requireTenantContext() {
        UUID tenantId = TenantContext.get();
        if (tenantId == null) {
            throw new AccesoNoAutorizadoException("Operación requiere contexto de tenant");
        }
        return tenantId;
    }

    private void verificarPertenencia(User user) {
        UUID tenantId = TenantContext.get();
        if (tenantId != null && !tenantId.equals(user.getTenantId())) {
            throw new AccesoNoAutorizadoException("No tienes acceso a este usuario");
        }
    }

    private UserResponse toResponse(User u) {
        return new UserResponse(
                u.getId(), u.getNombre(), u.getEmail(),
                u.getRol(), u.isActivo(), u.getUltimoLogin(), u.getCreatedAt()
        );
    }
}
