package com.stilum.citas.domain.subscription;

import com.stilum.citas.domain.plan.Plan;
import com.stilum.citas.domain.tenant.Tenant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Entidad Subscription — vínculo entre un tenant y su plan activo.
 * SK-B-05: Transiciones de estado solo por métodos de dominio.
 * RN-TENANT-002: Suscripción vencida bloquea operaciones.
 */
@Entity
@Table(name = "subscriptions")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "subscription_estado")
    private SubscriptionEstado estado = SubscriptionEstado.PRUEBA;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(name = "precio_mensual", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioMensual;

    @Column(name = "notas_pago", columnDefinition = "TEXT")
    private String notasPago;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    /** Crea una nueva suscripción para un tenant. */
    public static Subscription crear(Tenant tenant, Plan plan, LocalDate fechaFin, String notas) {
        Subscription sub = new Subscription();
        sub.tenant = tenant;
        sub.plan = plan;
        sub.estado = SubscriptionEstado.PRUEBA;
        sub.fechaInicio = LocalDate.now();
        sub.fechaFin = fechaFin;
        sub.precioMensual = plan.getPrecioMensual();
        sub.notasPago = notas;
        return sub;
    }

    /** Activa la suscripción al recibir el pago. */
    public void activar(String notas) {
        this.estado = SubscriptionEstado.ACTIVA;
        this.notasPago = notas;
    }

    /** Marca la suscripción como vencida (job automático). */
    public void vencer() {
        this.estado = SubscriptionEstado.VENCIDA;
    }

    /** Cancela la suscripción manualmente. */
    public void cancelar(String motivo) {
        this.estado = SubscriptionEstado.CANCELADA;
        this.notasPago = motivo;
    }

    /** Renueva: extiende fecha_fin y reactiva. */
    public void renovar(LocalDate nuevaFechaFin, String notas) {
        this.estado = SubscriptionEstado.ACTIVA;
        this.fechaFin = nuevaFechaFin;
        this.notasPago = notas;
    }

    public boolean estaVigente() {
        return estado.estaVigente() && !fechaFin.isBefore(LocalDate.now());
    }
}
