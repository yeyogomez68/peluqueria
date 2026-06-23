package com.stilum.citas.domain.user;

import com.stilum.citas.domain.tenant.Tenant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidad User — usuarios del sistema (super admin + usuarios por tenant).
 * SK-B-03: Lombok solo en entidades JPA.
 * SK-B-05: Cambios de estado solo por métodos del dominio.
 * RN-TENANT-001: tenant_id NULL solo para SUPER_ADMIN.
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
    private Tenant tenant; // NULL para SUPER_ADMIN

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "user_rol")
    private UserRol rol;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "ultimo_login")
    private Instant ultimoLogin;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    /** Factory para crear un Super Admin (sin tenant). */
    public static User crearSuperAdmin(String nombre, String email, String passwordHash) {
        User user = new User();
        user.nombre = nombre;
        user.email = email;
        user.passwordHash = passwordHash;
        user.rol = UserRol.SUPER_ADMIN;
        user.tenant = null;
        return user;
    }

    /** Factory para crear un usuario de un tenant. */
    public static User crearParaTenant(Tenant tenant, String nombre, String email,
                                       String passwordHash, UserRol rol) {
        if (rol == UserRol.SUPER_ADMIN) {
            throw new IllegalArgumentException("Usa crearSuperAdmin() para SUPER_ADMIN");
        }
        User user = new User();
        user.tenant = tenant;
        user.nombre = nombre;
        user.email = email;
        user.passwordHash = passwordHash;
        user.rol = rol;
        return user;
    }

    public void registrarLogin() {
        this.ultimoLogin = Instant.now();
    }

    public void desactivar() {
        this.activo = false;
    }

    public void activar() {
        this.activo = true;
    }

    public void cambiarPassword(String nuevaPasswordHash) {
        this.passwordHash = nuevaPasswordHash;
    }

    public UUID getTenantId() {
        return tenant != null ? tenant.getId() : null;
    }

    public boolean esSuperAdmin() {
        return rol == UserRol.SUPER_ADMIN;
    }
}
