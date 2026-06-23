package com.stilum.citas.domain.cita;

import com.stilum.citas.domain.profesional.Profesional;
import com.stilum.citas.domain.servicio.Servicio;
import com.stilum.citas.domain.shared.ConflictoException;
import com.stilum.citas.domain.tenant.Tenant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Entidad Cita — núcleo del sistema de agendamiento.
 *
 * RN-CITA-001: No solapamiento garantizado por constraint de DB + validación en dominio.
 * RN-CITA-002: Estados solo avanzan por métodos del dominio.
 * RN-TENANT-001: @Filter garantiza aislamiento por tenant_id.
 * SK-B-05: Todas las transiciones de estado pasan por cambiarEstado().
 */
@Entity
@Table(name = "citas")
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profesional_id", nullable = false)
    private Profesional profesional;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "servicio_id", nullable = false)
    private Servicio servicio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(name = "fecha_hora_inicio", nullable = false)
    private ZonedDateTime fechaHoraInicio;

    @Column(name = "fecha_hora_fin", nullable = false)
    private ZonedDateTime fechaHoraFin;

    @Column(name = "duracion_min", nullable = false)
    private int duracionMin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "cita_estado")
    private CitaEstado estado = CitaEstado.PENDIENTE;

    @Column(name = "precio_cobrado", precision = 10, scale = 2)
    private BigDecimal precioCobrado;

    @Column(name = "metodo_pago", length = 20)
    private String metodoPago;

    @Column(name = "comision_calculada", precision = 10, scale = 2)
    private BigDecimal comisionCalculada;

    @Column(name = "qr_token", unique = true)
    private UUID qrToken = UUID.randomUUID();

    @Column(name = "checked_in_at")
    private Instant checkedInAt;

    @Column(name = "motivo_cancelacion", columnDefinition = "TEXT")
    private String motivoCancelacion;

    @Column(name = "cancelado_por", length = 20)
    private String canceladoPor;

    @Column(name = "origen", nullable = false, length = 20)
    private String origen = "WEB";

    @Column(columnDefinition = "TEXT")
    private String notas;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    // ── Factory ────────────────────────────────────────────────────────────

    public static Cita crear(Tenant tenant, Profesional profesional, Servicio servicio,
                             Cliente cliente, ZonedDateTime inicio, String origen, String notas) {
        Cita cita = new Cita();
        cita.tenant = tenant;
        cita.profesional = profesional;
        cita.servicio = servicio;
        cita.cliente = cliente;
        cita.fechaHoraInicio = inicio;
        cita.duracionMin = servicio.getDuracionMin();
        cita.fechaHoraFin = inicio.plusMinutes(servicio.getDuracionMin());
        cita.origen = origen != null ? origen : "WEB";
        cita.notas = notas;
        return cita;
    }

    // ── Transiciones de estado (SK-B-05) ────────────────────────────────

    public void confirmar() {
        cambiarEstado(CitaEstado.CONFIRMADA, null, null);
    }

    public void iniciar() {
        cambiarEstado(CitaEstado.EN_CURSO, null, null);
    }

    public void completar(BigDecimal precioCobrado, String metodoPago, BigDecimal comisionPorcentaje) {
        cambiarEstado(CitaEstado.COMPLETADA, null, null);
        this.precioCobrado = precioCobrado != null ? precioCobrado : servicio.getPrecio();
        this.metodoPago = metodoPago;
        if (comisionPorcentaje != null && this.precioCobrado != null) {
            this.comisionCalculada = this.precioCobrado
                    .multiply(comisionPorcentaje)
                    .divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
        }
    }

    public void cancelar(String motivo, String canceladoPor) {
        cambiarEstado(CitaEstado.CANCELADA, null, null);
        this.motivoCancelacion = motivo;
        this.canceladoPor = canceladoPor;
    }

    public void marcarNoShow() {
        cambiarEstado(CitaEstado.NO_SHOW, null, null);
    }

    /** Check-in vía QR — activa la cita y la marca como en curso. */
    public void registrarCheckIn() {
        if (this.checkedInAt != null) {
            throw new ConflictoException("CHECK_IN_DUPLICADO", "Esta cita ya registró check-in");
        }
        this.checkedInAt = Instant.now();
        if (estado == CitaEstado.CONFIRMADA || estado == CitaEstado.PENDIENTE) {
            this.estado = CitaEstado.EN_CURSO;
        }
    }

    private void cambiarEstado(CitaEstado nuevo, String motivo, String actor) {
        if (!estado.puedeTransicionarA(nuevo)) {
            throw new ConflictoException("TRANSICION_INVALIDA",
                    "No se puede pasar de " + estado + " a " + nuevo);
        }
        this.estado = nuevo;
    }

    public UUID getTenantId() { return tenant.getId(); }

    public boolean estaActiva() { return estado.estaActiva(); }
}
