package com.stilum.citas.application.profesional;

import com.stilum.citas.application.profesional.dto.CreateProfesionalRequest;
import com.stilum.citas.application.profesional.dto.HorarioDto;
import com.stilum.citas.application.profesional.dto.MisIngresosResponse;
import com.stilum.citas.application.profesional.dto.ProfesionalResponse;
import com.stilum.citas.application.profesional.dto.UpdateProfesionalRequest;
import com.stilum.citas.domain.cita.Cita;
import com.stilum.citas.domain.cita.CitaRepository;
import com.stilum.citas.domain.profesional.HorarioProfesional;
import com.stilum.citas.domain.profesional.Profesional;
import com.stilum.citas.domain.profesional.ProfesionalRepository;
import com.stilum.citas.domain.shared.AccesoNoAutorizadoException;
import com.stilum.citas.domain.shared.ConflictoException;
import com.stilum.citas.domain.shared.RecursoNoEncontradoException;
import com.stilum.citas.domain.subscription.Subscription;
import com.stilum.citas.domain.subscription.SubscriptionRepository;
import com.stilum.citas.domain.tenant.Tenant;
import com.stilum.citas.domain.tenant.TenantRepository;
import com.stilum.citas.domain.user.User;
import com.stilum.citas.domain.user.UserRepository;
import com.stilum.citas.domain.user.UserRol;
import com.stilum.citas.infrastructure.security.TenantContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Casos de uso del módulo Profesionales.
 * RN-PROF-001: Límite de profesionales activos según plan.
 * RN-TENANT-001: Aislamiento automático por TenantContext + @Filter.
 */
@Service
@Transactional(readOnly = true)
public class ProfesionalService {

    private final ProfesionalRepository profesionalRepository;
    private final TenantRepository tenantRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final CitaRepository citaRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfesionalService(ProfesionalRepository profesionalRepository,
                              TenantRepository tenantRepository,
                              SubscriptionRepository subscriptionRepository,
                              CitaRepository citaRepository,
                              UserRepository userRepository,
                              PasswordEncoder passwordEncoder) {
        this.profesionalRepository = profesionalRepository;
        this.tenantRepository = tenantRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.citaRepository = citaRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Lista todos los profesionales del tenant (activos e inactivos). */
    public List<ProfesionalResponse> listar() {
        UUID tenantId = requireTenantContext();
        return profesionalRepository.findAllByTenantId(tenantId).stream()
                .map(ProfesionalResponse::from)
                .toList();
    }

    /** Lista solo los profesionales activos del tenant. */
    public List<ProfesionalResponse> listarActivos() {
        UUID tenantId = requireTenantContext();
        return profesionalRepository.findActivosByTenantId(tenantId).stream()
                .map(ProfesionalResponse::from)
                .toList();
    }

    /** Obtiene un profesional por ID — verifica pertenencia al tenant. */
    public ProfesionalResponse obtener(UUID id) {
        Profesional p = findAndVerify(id);
        return ProfesionalResponse.from(p);
    }

    /**
     * Crea un nuevo profesional.
     * RN-PROF-001: Valida límite de profesionales activos del plan vigente.
     */
    @Transactional
    public ProfesionalResponse crear(CreateProfesionalRequest req) {
        UUID tenantId = requireTenantContext();
        Tenant tenant = loadTenant(tenantId);

        // RN-PROF-001: Límite según plan
        Subscription sub = subscriptionRepository.findVigenteByTenantId(tenantId)
                .orElseThrow(() -> new ConflictoException("SIN_SUSCRIPCION",
                        "El tenant no tiene una suscripción vigente"));

        int activos = profesionalRepository.countActivosByTenantId(tenantId);
        if (activos >= sub.getPlan().getMaxProfesionales()) {
            throw new ConflictoException("LIMITE_PROFESIONALES",
                    "El plan " + sub.getPlan().getNombre() + " permite máximo "
                            + sub.getPlan().getMaxProfesionales() + " profesionales activos");
        }

        if (userRepository.existsByEmail(req.email())) {
            throw new ConflictoException("EMAIL_DUPLICADO",
                    "Ya existe un usuario con el email " + req.email());
        }

        Profesional p = Profesional.crear(tenant, req.nombre(), req.especialidad(),
                req.bio(), req.colorAgenda());

        if (req.horarios() != null) {
            req.horarios().forEach(h -> p.getHorarios().add(
                    HorarioProfesional.crear(p, tenant,
                            h.diaSemana(), h.horaInicio(), h.horaFin())
            ));
        }

        profesionalRepository.save(p);

        User user = User.crearParaTenant(tenant, req.nombre(), req.email(),
                passwordEncoder.encode(req.password()), UserRol.PROFESIONAL);
        userRepository.save(user);

        return ProfesionalResponse.from(p);
    }

    /** Actualiza datos y horarios de un profesional. */
    @Transactional
    public ProfesionalResponse actualizar(UUID id, UpdateProfesionalRequest req) {
        Profesional p = findAndVerify(id);
        p.actualizar(req.nombre(), req.especialidad(), req.bio(),
                req.fotoUrl(), req.colorAgenda());

        if (req.horarios() != null) {
            Tenant tenant = loadTenant(p.getTenantId());
            p.getHorarios().clear();
            req.horarios().forEach(h -> p.getHorarios().add(
                    HorarioProfesional.crear(p, tenant,
                            h.diaSemana(), h.horaInicio(), h.horaFin())
            ));
        }

        return ProfesionalResponse.from(profesionalRepository.save(p));
    }

    /** Activa un profesional desactivado — verifica límite de plan. */
    @Transactional
    public ProfesionalResponse activar(UUID id) {
        Profesional p = findAndVerify(id);
        if (p.isActivo()) {
            return ProfesionalResponse.from(p);
        }

        UUID tenantId = p.getTenantId();
        Subscription sub = subscriptionRepository.findVigenteByTenantId(tenantId)
                .orElseThrow(() -> new ConflictoException("SIN_SUSCRIPCION",
                        "El tenant no tiene una suscripción vigente"));

        int activos = profesionalRepository.countActivosByTenantId(tenantId);
        if (activos >= sub.getPlan().getMaxProfesionales()) {
            throw new ConflictoException("LIMITE_PROFESIONALES",
                    "No se puede activar: límite de "
                            + sub.getPlan().getMaxProfesionales() + " profesionales alcanzado");
        }

        p.activar();
        return ProfesionalResponse.from(profesionalRepository.save(p));
    }

    /** Desactiva un profesional. */
    @Transactional
    public ProfesionalResponse desactivar(UUID id) {
        Profesional p = findAndVerify(id);
        p.desactivar();
        return ProfesionalResponse.from(profesionalRepository.save(p));
    }

    public MisIngresosResponse misIngresos(UUID profesionalId, LocalDate inicio, LocalDate fin) {
        List<Cita> citas = citaRepository.findCompletadasPorProfesionalYRango(profesionalId, inicio, fin);

        BigDecimal totalFacturado = citas.stream()
                .map(c -> c.getPrecioCobrado() != null ? c.getPrecioCobrado() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalComision = citas.stream()
                .map(c -> c.getComisionCalculada() != null ? c.getComisionCalculada() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<MisIngresosResponse.IngresoDetalle> detalle = citas.stream()
                .map(c -> new MisIngresosResponse.IngresoDetalle(
                        c.getFechaHoraInicio(),
                        c.getServicio().getNombre(),
                        c.getCliente().getNombre(),
                        c.getPrecioCobrado(),
                        c.getComisionCalculada(),
                        c.getMetodoPago()
                )).toList();

        return new MisIngresosResponse(inicio, fin, citas.size(), totalFacturado, totalComision, detalle);
    }

    @Transactional
    public ProfesionalResponse actualizarComision(UUID profesionalId, java.math.BigDecimal porcentaje) {
        Profesional p = profesionalRepository.findById(profesionalId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Profesional", profesionalId));
        p.actualizarComision(porcentaje);
        return ProfesionalResponse.from(profesionalRepository.save(p));
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private UUID requireTenantContext() {
        UUID tenantId = TenantContext.get();
        if (tenantId == null) {
            throw new AccesoNoAutorizadoException("Operación requiere contexto de tenant");
        }
        return tenantId;
    }

    private Profesional findAndVerify(UUID id) {
        Profesional p = profesionalRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Profesional", id));
        UUID tenantId = TenantContext.get();
        if (tenantId != null && !tenantId.equals(p.getTenantId())) {
            throw new AccesoNoAutorizadoException("No tienes acceso a este profesional");
        }
        return p;
    }

    private Tenant loadTenant(UUID tenantId) {
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Tenant", tenantId));
    }
}
