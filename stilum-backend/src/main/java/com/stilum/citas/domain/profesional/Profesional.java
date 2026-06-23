package com.stilum.citas.domain.profesional;

import com.stilum.citas.domain.tenant.Tenant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad Profesional — peluquero/barbero que atiende citas.
 * RN-TENANT-001: @Filter garantiza aislamiento automático por tenant_id.
 * SK-B-05: Estado solo cambia por métodos de dominio.
 */
@Entity
@Table(name = "profesionales")
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Profesional {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 150)
    private String especialidad;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "foto_url", columnDefinition = "TEXT")
    private String fotoUrl;

    @Column(name = "color_agenda", length = 7)
    private String colorAgenda;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "comision_porcentaje", nullable = false, precision = 5, scale = 2)
    private BigDecimal comisionPorcentaje = new BigDecimal("30.00");

    @OneToMany(mappedBy = "profesional", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<HorarioProfesional> horarios = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    public static Profesional crear(Tenant tenant, String nombre, String especialidad,
                                    String bio, String colorAgenda) {
        Profesional p = new Profesional();
        p.tenant = tenant;
        p.nombre = nombre;
        p.especialidad = especialidad;
        p.bio = bio;
        p.colorAgenda = colorAgenda;
        return p;
    }

    public void actualizar(String nombre, String especialidad, String bio,
                           String fotoUrl, String colorAgenda) {
        if (nombre != null && !nombre.isBlank()) this.nombre = nombre;
        this.especialidad = especialidad;
        this.bio = bio;
        this.fotoUrl = fotoUrl;
        if (colorAgenda != null) this.colorAgenda = colorAgenda;
    }

    public void activar()    { this.activo = true; }
    public void desactivar() { this.activo = false; }

    public void actualizarComision(BigDecimal porcentaje) {
        if (porcentaje == null || porcentaje.compareTo(BigDecimal.ZERO) < 0
                || porcentaje.compareTo(new BigDecimal("100")) > 0) {
            throw new com.stilum.citas.domain.shared.ReglaNegocioException(
                    "COMISION_INVALIDA", "El porcentaje debe estar entre 0 y 100");
        }
        this.comisionPorcentaje = porcentaje;
    }

    public UUID getTenantId() { return tenant.getId(); }
}
