package com.stilum.citas.domain.tenant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidad Tenant — negocio de peluquería/barbería suscrito a la plataforma.
 * SK-B-03: Lombok solo en entidades JPA.
 * SK-B-05: Estado se modifica solo por métodos del dominio.
 * RN-TENANT-001: Aislamiento garantizado por tenant_id en todas las queries.
 */
@Entity
@Table(name = "tenants")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "nombre_negocio", nullable = false, length = 100)
    private String nombreNegocio;

    @Column(name = "email_contacto", nullable = false, unique = true, length = 150)
    private String emailContacto;

    @Column(name = "telefono_contacto", length = 20)
    private String telefonoContacto;

    @Column(length = 100)
    private String ciudad;

    @Column(nullable = false, length = 80)
    private String pais = "Colombia";

    @Column(name = "logo_url", columnDefinition = "TEXT")
    private String logoUrl;

    @Column(nullable = false)
    private boolean activo = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "fecha_inactivacion")
    private Instant fechaInactivacion;

    /** Factory para crear un nuevo tenant. */
    public static Tenant crear(String nombreNegocio, String emailContacto,
                               String telefonoContacto, String ciudad, String pais) {
        Tenant tenant = new Tenant();
        tenant.nombreNegocio = nombreNegocio;
        tenant.emailContacto = emailContacto;
        tenant.telefonoContacto = telefonoContacto;
        tenant.ciudad = ciudad;
        tenant.pais = pais != null ? pais : "Colombia";
        return tenant;
    }

    /** Activa el tenant — permite el acceso al portal. */
    public void activar() {
        this.activo = true;
        this.fechaInactivacion = null;
    }

    /** Inactiva el tenant — bloquea el acceso sin borrar datos. */
    public void inactivar() {
        this.activo = false;
        this.fechaInactivacion = Instant.now();
    }

    /** Actualiza los datos básicos del negocio. */
    public void actualizarDatos(String nombreNegocio, String telefonoContacto,
                                String ciudad, String pais, String logoUrl) {
        if (nombreNegocio != null && !nombreNegocio.isBlank()) {
            this.nombreNegocio = nombreNegocio;
        }
        this.telefonoContacto = telefonoContacto;
        this.ciudad = ciudad;
        if (pais != null && !pais.isBlank()) {
            this.pais = pais;
        }
        this.logoUrl = logoUrl;
    }

    public boolean estaActivo() {
        return activo;
    }
}
