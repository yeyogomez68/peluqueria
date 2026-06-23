package com.stilum.citas.application.subscription;

import com.stilum.citas.application.subscription.dto.ActivarSubscriptionRequest;
import com.stilum.citas.application.subscription.dto.SubscriptionResponse;
import com.stilum.citas.domain.plan.PlanRepository;
import com.stilum.citas.domain.shared.RecursoNoEncontradoException;
import com.stilum.citas.domain.subscription.Subscription;
import com.stilum.citas.domain.subscription.SubscriptionRepository;
import com.stilum.citas.domain.tenant.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Caso de uso: gestión de suscripciones de tenants.
 * RN-TENANT-002: suscripción vencida bloquea operaciones del tenant.
 */
@Service
@Transactional(readOnly = true)
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final TenantRepository tenantRepository;
    private final PlanRepository planRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository,
                               TenantRepository tenantRepository,
                               PlanRepository planRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.tenantRepository = tenantRepository;
        this.planRepository = planRepository;
    }

    /** Historial completo de suscripciones de un tenant. */
    public List<SubscriptionResponse> listarPorTenant(UUID tenantId) {
        tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Tenant", tenantId));
        return subscriptionRepository.findAllByTenantId(tenantId).stream()
                .map(this::toResponse)
                .toList();
    }

    /** Suscripción vigente actual de un tenant. */
    public Optional<SubscriptionResponse> obtenerVigente(UUID tenantId) {
        return subscriptionRepository.findVigenteByTenantId(tenantId)
                .map(this::toResponse);
    }

    /** Activa la suscripción (tras recibir pago). */
    @Transactional
    public SubscriptionResponse activar(UUID subscriptionId, ActivarSubscriptionRequest req) {
        Subscription sub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Subscription", subscriptionId));
        sub.activar(req.notas());
        return toResponse(subscriptionRepository.save(sub));
    }

    /** Renueva la suscripción con nueva fecha de vencimiento. */
    @Transactional
    public SubscriptionResponse renovar(UUID subscriptionId, ActivarSubscriptionRequest req) {
        Subscription sub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Subscription", subscriptionId));
        sub.renovar(req.fechaFin(), req.notas());
        return toResponse(subscriptionRepository.save(sub));
    }

    /** Cancela la suscripción. */
    @Transactional
    public SubscriptionResponse cancelar(UUID subscriptionId, String motivo) {
        Subscription sub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Subscription", subscriptionId));
        sub.cancelar(motivo);
        return toResponse(subscriptionRepository.save(sub));
    }

    // ── Mapper ─────────────────────────────────────────────────────────────

    private SubscriptionResponse toResponse(Subscription s) {
        return new SubscriptionResponse(
                s.getId(),
                s.getTenant().getId(),
                s.getTenant().getNombreNegocio(),
                s.getPlan().getId(),
                s.getPlan().getNombre(),
                s.getEstado(),
                s.getFechaInicio(),
                s.getFechaFin(),
                s.getPrecioMensual(),
                s.getNotasPago(),
                s.estaVigente()
        );
    }
}
