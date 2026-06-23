package com.stilum.citas.domain.plan;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Entidad Plan — planes de suscripción disponibles en la plataforma.
 * SK-B-03: Lombok solo en entidades JPA.
 * SK-B-05: Estado solo se cambia mediante métodos de dominio.
 */
@Entity
@Table(name = "plans")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String nombre;

    @Column(name = "precio_mensual", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioMensual;

    @Column(name = "max_profesionales", nullable = false)
    private int maxProfesionales;

    @Column(name = "max_citas_mes", nullable = false)
    private int maxCitasMes;

    @Column(name = "whatsapp_habilitado", nullable = false)
    private boolean whatsappHabilitado;

    @Column(name = "recordatorios_habilitados", nullable = false)
    private boolean recordatoriosHabilitados;

    @Column(name = "reportes_avanzados", nullable = false)
    private boolean reportesAvanzados;

    @Column(nullable = false)
    private boolean activo = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    /** Factory para crear un plan nuevo. */
    public static Plan crear(String nombre, BigDecimal precioMensual,
                             int maxProfesionales, int maxCitasMes,
                             boolean whatsappHabilitado, boolean recordatoriosHabilitados,
                             boolean reportesAvanzados) {
        Plan plan = new Plan();
        plan.nombre = nombre;
        plan.precioMensual = precioMensual;
        plan.maxProfesionales = maxProfesionales;
        plan.maxCitasMes = maxCitasMes;
        plan.whatsappHabilitado = whatsappHabilitado;
        plan.recordatoriosHabilitados = recordatoriosHabilitados;
        plan.reportesAvanzados = reportesAvanzados;
        return plan;
    }

    /** Desactiva el plan — los tenants existentes conservan su suscripción. */
    public void desactivar() {
        this.activo = false;
    }

    public boolean permiteWhatsapp() {
        return whatsappHabilitado;
    }

    public boolean permiteRecordatorios() {
        return recordatoriosHabilitados;
    }

    public boolean permiteReportesAvanzados() {
        return reportesAvanzados;
    }
}
