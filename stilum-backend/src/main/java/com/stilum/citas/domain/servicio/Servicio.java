package com.stilum.citas.domain.servicio;

import com.stilum.citas.domain.tenant.Tenant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Entidad Servicio — catálogo de servicios ofrecidos por el tenant.
 * RN-SERV-001: duracion_min > 0 garantizada por constraint de DB y dominio.
 * RN-TENANT-001: @Filter aísla por tenant_id.
 */
@Entity
@Table(name = "servicios")
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "duracion_min", nullable = false)
    private int duracionMin;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(nullable = false)
    private boolean activo = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    public static Servicio crear(Tenant tenant, String nombre, String descripcion,
                                 int duracionMin, BigDecimal precio) {
        if (duracionMin <= 0) throw new IllegalArgumentException("La duración debe ser mayor a 0 minutos");
        if (precio.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("El precio no puede ser negativo");
        Servicio s = new Servicio();
        s.tenant = tenant;
        s.nombre = nombre;
        s.descripcion = descripcion;
        s.duracionMin = duracionMin;
        s.precio = precio;
        return s;
    }

    public void actualizar(String nombre, String descripcion, int duracionMin, BigDecimal precio) {
        if (nombre != null && !nombre.isBlank()) this.nombre = nombre;
        this.descripcion = descripcion;
        if (duracionMin > 0) this.duracionMin = duracionMin;
        if (precio != null && precio.compareTo(BigDecimal.ZERO) >= 0) this.precio = precio;
    }

    public void activar()    { this.activo = true; }
    public void desactivar() { this.activo = false; }

    public UUID getTenantId() { return tenant.getId(); }
}
