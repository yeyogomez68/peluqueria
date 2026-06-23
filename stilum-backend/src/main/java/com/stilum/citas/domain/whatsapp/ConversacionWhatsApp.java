package com.stilum.citas.domain.whatsapp;

import com.stilum.citas.domain.tenant.Tenant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "whatsapp_conversaciones")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class ConversacionWhatsApp {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(nullable = false, length = 20)
    private String telefono;

    @Column(nullable = false, length = 30)
    private String estado = "INICIO";

    @Column(name = "datos_json", nullable = false, columnDefinition = "TEXT")
    private String datosJson = "{}";

    @Column(name = "ultimo_mensaje")
    private Instant ultimoMensaje = Instant.now();

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    public static ConversacionWhatsApp iniciar(Tenant tenant, String telefono) {
        ConversacionWhatsApp c = new ConversacionWhatsApp();
        c.tenant = tenant;
        c.telefono = telefono;
        return c;
    }

    public void avanzarEstado(String nuevoEstado, String datosJson) {
        this.estado = nuevoEstado;
        this.datosJson = datosJson != null ? datosJson : "{}";
        this.ultimoMensaje = Instant.now();
    }

    public UUID getTenantId() { return tenant.getId(); }
}
