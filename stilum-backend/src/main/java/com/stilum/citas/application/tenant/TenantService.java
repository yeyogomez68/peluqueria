package com.stilum.citas.application.tenant;

import com.stilum.citas.application.subscription.dto.SubscriptionResponse;
import com.stilum.citas.application.tenant.dto.*;
import com.stilum.citas.domain.plan.Plan;
import com.stilum.citas.domain.plan.PlanRepository;
import com.stilum.citas.domain.shared.ConflictoException;
import com.stilum.citas.domain.shared.RecursoNoEncontradoException;
import com.stilum.citas.domain.subscription.Subscription;
import com.stilum.citas.domain.subscription.SubscriptionRepository;
import com.stilum.citas.domain.tenant.Tenant;
import com.stilum.citas.domain.tenant.TenantRepository;
import com.stilum.citas.domain.user.User;
import com.stilum.citas.domain.user.UserRepository;
import com.stilum.citas.domain.user.UserRol;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Caso de uso: gestión de tenants (Solo SUPER_ADMIN).
 * SK-B-01: Application layer orquesta domain + infrastructure.
 * RN-TENANT-001, RN-TENANT-002.
 */
@Service
@Transactional(readOnly = true)
public class TenantService {

    private final TenantRepository tenantRepository;
    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public TenantService(TenantRepository tenantRepository,
                         PlanRepository planRepository,
                         SubscriptionRepository subscriptionRepository,
                         UserRepository userRepository,
                         PasswordEncoder passwordEncoder) {
        this.tenantRepository = tenantRepository;
        this.planRepository = planRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Lista todos los tenants (activos e inactivos). */
    public List<TenantSummaryResponse> listarTodos() {
        return tenantRepository.findAll().stream()
                .map(this::toSummary)
                .toList();
    }

    /** Obtiene un tenant por ID con detalle completo. */
    public TenantResponse obtener(UUID id) {
        return toResponse(buscarTenant(id));
    }

    /**
     * Crea un nuevo tenant + suscripción de prueba (14 días) + usuario admin inicial.
     * RN-TENANT-001: datos completamente aislados desde el inicio.
     */
    @Transactional
    public TenantResponse crear(CreateTenantRequest req) {
        if (tenantRepository.existsByEmailContacto(req.emailContacto())) {
            throw new ConflictoException("EMAIL_DUPLICADO",
                    "Ya existe un tenant con el email: " + req.emailContacto());
        }
        if (userRepository.existsByEmail(req.emailContacto())) {
            throw new ConflictoException("EMAIL_DUPLICADO",
                    "El email ya está en uso por otro usuario");
        }

        Plan plan = planRepository.findById(req.planId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Plan", req.planId()));

        // 1. Crear tenant
        Tenant tenant = Tenant.crear(
                req.nombreNegocio(),
                req.emailContacto(),
                req.telefonoContacto(),
                req.ciudad(),
                req.pais()
        );
        tenant = tenantRepository.save(tenant);

        // 2. Crear suscripción de prueba — 14 días
        Subscription sub = Subscription.crear(
                tenant,
                plan,
                LocalDate.now().plusDays(14),
                "Período de prueba inicial"
        );
        subscriptionRepository.save(sub);

        // 3. Crear usuario admin del tenant con el email del negocio
        String hash = passwordEncoder.encode(req.adminPassword());
        User adminUser = User.crearParaTenant(tenant, req.adminNombre(),
                req.emailContacto(), hash, UserRol.ADMIN_TENANT);
        userRepository.save(adminUser);

        return toResponse(tenant);
    }

    /** Actualiza datos básicos del negocio. */
    @Transactional
    public TenantResponse actualizar(UUID id, UpdateTenantRequest req) {
        Tenant tenant = buscarTenant(id);
        tenant.actualizarDatos(
                req.nombreNegocio(),
                req.telefonoContacto(),
                req.ciudad(),
                req.pais(),
                req.logoUrl()
        );
        return toResponse(tenantRepository.save(tenant));
    }

    /** Activa el acceso del tenant a la plataforma. */
    @Transactional
    public TenantResponse activar(UUID id) {
        Tenant tenant = buscarTenant(id);
        tenant.activar();
        return toResponse(tenantRepository.save(tenant));
    }

    /** Bloquea el acceso del tenant sin borrar sus datos. */
    @Transactional
    public TenantResponse inactivar(UUID id) {
        Tenant tenant = buscarTenant(id);
        tenant.inactivar();
        return toResponse(tenantRepository.save(tenant));
    }

    // ── Mappers internos ───────────────────────────────────────────────────

    private Tenant buscarTenant(UUID id) {
        return tenantRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Tenant", id));
    }

    private TenantResponse toResponse(Tenant t) {
        return new TenantResponse(
                t.getId(), t.getNombreNegocio(), t.getEmailContacto(),
                t.getTelefonoContacto(), t.getCiudad(), t.getPais(),
                t.getLogoUrl(), t.isActivo(), t.getCreatedAt(), t.getFechaInactivacion()
        );
    }

    private TenantSummaryResponse toSummary(Tenant t) {
        return new TenantSummaryResponse(
                t.getId(), t.getNombreNegocio(), t.getEmailContacto(),
                t.getCiudad(), t.isActivo()
        );
    }
}
