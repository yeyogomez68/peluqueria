package com.stilum.citas.application.cita;

import com.stilum.citas.application.cita.dto.*;
import com.stilum.citas.domain.cita.*;
import com.stilum.citas.domain.cita.spec.CitaNoSolapaSpec;
import com.stilum.citas.domain.cita.spec.HorarioLaboralSpec;
import com.stilum.citas.domain.cita.spec.LimitePlanSpec;
import com.stilum.citas.domain.profesional.Profesional;
import com.stilum.citas.domain.profesional.ProfesionalRepository;
import com.stilum.citas.domain.servicio.Servicio;
import com.stilum.citas.domain.servicio.ServicioRepository;
import com.stilum.citas.domain.shared.AccesoNoAutorizadoException;
import com.stilum.citas.domain.shared.ConflictoException;
import com.stilum.citas.domain.shared.RecursoNoEncontradoException;
import com.stilum.citas.domain.subscription.Subscription;
import com.stilum.citas.domain.subscription.SubscriptionRepository;
import com.stilum.citas.domain.tenant.Tenant;
import com.stilum.citas.domain.tenant.TenantRepository;
import com.stilum.citas.infrastructure.security.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Casos de uso del módulo Citas.
 *
 * RN-CITA-001 (CitaNoSolapaSpec): no solapamiento de horarios por profesional.
 * RN-CITA-003 (HorarioLaboralSpec): la cita debe caer en el horario laboral.
 * RN-CITA-004 (LimitePlanSpec): no superar el límite mensual del plan.
 *
 * Las tres reglas se componen con Specification Pattern antes del save.
 */
@Service
@Transactional(readOnly = true)
public class CitaService {

    private final CitaRepository citaRepository;
    private final ClienteRepository clienteRepository;
    private final ProfesionalRepository profesionalRepository;
    private final ServicioRepository servicioRepository;
    private final TenantRepository tenantRepository;
    private final SubscriptionRepository subscriptionRepository;

    public CitaService(CitaRepository citaRepository,
                       ClienteRepository clienteRepository,
                       ProfesionalRepository profesionalRepository,
                       ServicioRepository servicioRepository,
                       TenantRepository tenantRepository,
                       SubscriptionRepository subscriptionRepository) {
        this.citaRepository = citaRepository;
        this.clienteRepository = clienteRepository;
        this.profesionalRepository = profesionalRepository;
        this.servicioRepository = servicioRepository;
        this.tenantRepository = tenantRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    // ── Consultas ──────────────────────────────────────────────────────────

    /** Citas del tenant en una fecha (vista calendario). */
    public List<CitaResponse> listarPorFecha(LocalDate fecha) {
        UUID tenantId = requireTenantContext();
        return citaRepository.findByTenantIdAndFecha(tenantId, fecha).stream()
                .map(CitaResponse::from)
                .toList();
    }

    /** Citas de un profesional en una fecha específica. */
    public List<CitaResponse> listarPorProfesionalYFecha(UUID profesionalId, LocalDate fecha) {
        return citaRepository.findByProfesionalIdAndFecha(profesionalId, fecha).stream()
                .map(CitaResponse::from)
                .toList();
    }

    /** Detalle de una cita. */
    public CitaResponse obtener(UUID id) {
        return CitaResponse.from(findAndVerify(id));
    }

    /**
     * Calcula slots disponibles para un profesional en una fecha y duración dadas.
     * Genera slots de 30 minutos dentro del horario laboral y filtra los ocupados.
     */
    public List<SlotDisponibleResponse> disponibilidad(UUID profesionalId,
                                                        LocalDate fecha,
                                                        int duracionMin) {
        Profesional profesional = profesionalRepository.findById(profesionalId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Profesional", profesionalId));

        int diaSemana = fecha.getDayOfWeek().getValue();
        ZoneId zone = ZoneId.systemDefault();

        List<SlotDisponibleResponse> slots = new ArrayList<>();

        profesional.getHorarios().stream()
                .filter(h -> h.getDiaSemana() == diaSemana && h.isActivo())
                .forEach(horario -> {
                    LocalTime cursor = horario.getHoraInicio();
                    LocalTime limite = horario.getHoraFin();

                    while (!cursor.plusMinutes(duracionMin).isAfter(limite)) {
                        ZonedDateTime inicio = ZonedDateTime.of(fecha, cursor, zone);
                        ZonedDateTime fin = inicio.plusMinutes(duracionMin);

                        boolean solapado = citaRepository.existeSolapamiento(
                                profesionalId, inicio, fin, null);

                        if (!solapado) {
                            slots.add(new SlotDisponibleResponse(
                                    profesionalId,
                                    profesional.getNombre(),
                                    inicio, fin, duracionMin
                            ));
                        }
                        cursor = cursor.plusMinutes(30); // granularidad 30 min
                    }
                });

        return slots;
    }

    // ── Comandos ───────────────────────────────────────────────────────────

    /**
     * Crea una cita nueva — aplica las tres specs en cadena.
     * RN-CITA-001, RN-CITA-003, RN-CITA-004.
     */
    @Transactional
    public CitaResponse crear(CreateCitaRequest req) {
        UUID tenantId = requireTenantContext();
        Tenant tenant = loadTenant(tenantId);

        Profesional profesional = profesionalRepository.findById(req.profesionalId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Profesional", req.profesionalId()));

        Servicio servicio = servicioRepository.findById(req.servicioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Servicio", req.servicioId()));

        // Buscar o crear cliente por teléfono (upsert por tenant+teléfono)
        Cliente cliente = clienteRepository
                .findByTenantIdAndTelefono(tenantId, req.clienteTelefono())
                .orElseGet(() -> {
                    String nombre = req.clienteNombre() != null
                            ? req.clienteNombre()
                            : "Cliente " + req.clienteTelefono();
                    return clienteRepository.save(
                            Cliente.crear(tenant, nombre, req.clienteTelefono(), null));
                });

        Cita cita = Cita.crear(tenant, profesional, servicio, cliente,
                req.fechaHoraInicio(), req.origen(), req.notas());

        // Subscription vigente para LimitePlanSpec
        Subscription sub = subscriptionRepository.findVigenteByTenantId(tenantId)
                .orElseThrow(() -> new ConflictoException("SIN_SUSCRIPCION",
                        "El tenant no tiene una suscripción vigente"));

        // ── Specifications ──────────────────────────────────────────────
        new HorarioLaboralSpec()
                .verificar(cita, "FUERA_HORARIO",
                        "La cita está fuera del horario laboral del profesional");

        new CitaNoSolapaSpec(citaRepository)
                .verificar(cita, "CITA_SOLAPADA",
                        "El profesional ya tiene una cita en ese horario");

        new LimitePlanSpec(citaRepository, sub)
                .verificar(cita, "LIMITE_PLAN",
                        "Se alcanzó el límite mensual de citas del plan " + sub.getPlan().getNombre());

        return CitaResponse.from(citaRepository.save(cita));
    }

    /** Confirma una cita PENDIENTE. */
    @Transactional
    public CitaResponse confirmar(UUID id) {
        Cita cita = findAndVerify(id);
        cita.confirmar();
        return CitaResponse.from(citaRepository.save(cita));
    }

    /** Inicia una cita CONFIRMADA (en curso). */
    @Transactional
    public CitaResponse iniciar(UUID id) {
        Cita cita = findAndVerify(id);
        cita.iniciar();
        return CitaResponse.from(citaRepository.save(cita));
    }

    /** Completa una cita EN_CURSO registrando el precio cobrado. */
    @Transactional
    public CitaResponse completar(UUID id, CompletarCitaRequest req) {
        Cita cita = findAndVerify(id);
        cita.completar(req.precioCobrado());
        return CitaResponse.from(citaRepository.save(cita));
    }

    /** Cancela una cita con motivo y actor. */
    @Transactional
    public CitaResponse cancelar(UUID id, CancelarCitaRequest req) {
        Cita cita = findAndVerify(id);
        cita.cancelar(req.motivo(), req.canceladoPor());
        return CitaResponse.from(citaRepository.save(cita));
    }

    /** Marca NO_SHOW una cita CONFIRMADA. */
    @Transactional
    public CitaResponse marcarNoShow(UUID id) {
        Cita cita = findAndVerify(id);
        cita.marcarNoShow();
        return CitaResponse.from(citaRepository.save(cita));
    }

    /** Check-in vía QR token. */
    @Transactional
    public CitaResponse checkIn(UUID qrToken) {
        Cita cita = citaRepository.findByQrToken(qrToken)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita (QR)", qrToken));
        cita.registrarCheckIn();
        return CitaResponse.from(citaRepository.save(cita));
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private UUID requireTenantContext() {
        UUID tenantId = TenantContext.get();
        if (tenantId == null) {
            throw new AccesoNoAutorizadoException("Operación requiere contexto de tenant");
        }
        return tenantId;
    }

    private Cita findAndVerify(UUID id) {
        Cita c = citaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita", id));
        UUID tenantId = TenantContext.get();
        if (tenantId != null && !tenantId.equals(c.getTenantId())) {
            throw new AccesoNoAutorizadoException("No tienes acceso a esta cita");
        }
        return c;
    }

    private Tenant loadTenant(UUID tenantId) {
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Tenant", tenantId));
    }
}
