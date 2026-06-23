package com.stilum.citas.domain.cita;

import com.stilum.citas.domain.tenant.Tenant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Cliente — persona que agenda citas en un tenant.
 * Identificado principalmente por teléfono (canal WhatsApp).
 */
@Entity
@Table(name = "clientes")
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 20)
    private String telefono;

    @Column(length = 150)
    private String email;

    @Column(columnDefinition = "TEXT")
    private String notas;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    public static Cliente crear(Tenant tenant, String nombre, String telefono, String email) {
        Cliente c = new Cliente();
        c.tenant = tenant;
        c.nombre = nombre;
        c.telefono = telefono;
        c.email = email;
        return c;
    }

    public void actualizarNombre(String nombre) {
        if (nombre != null && !nombre.isBlank()) this.nombre = nombre;
    }

    public UUID getTenantId() { return tenant.getId(); }
}
