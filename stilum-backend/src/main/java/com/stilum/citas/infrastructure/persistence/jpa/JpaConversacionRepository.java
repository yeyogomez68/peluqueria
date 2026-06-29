package com.stilum.citas.infrastructure.persistence.jpa;

import com.stilum.citas.domain.whatsapp.ConversacionRepository;
import com.stilum.citas.domain.whatsapp.ConversacionWhatsApp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaConversacionRepository
        extends JpaRepository<ConversacionWhatsApp, UUID>, ConversacionRepository {

    @Query("SELECT c FROM ConversacionWhatsApp c WHERE c.tenant.id = :tenantId AND c.telefono = :telefono")
    Optional<ConversacionWhatsApp> findByTenantIdAndTelefono(@Param("tenantId") UUID tenantId, @Param("telefono") String telefono);

    @Modifying
    @Query("DELETE FROM ConversacionWhatsApp c WHERE c.tenant.id = :tenantId AND c.telefono = :telefono")
    void deleteByTenantIdAndTelefono(@Param("tenantId") UUID tenantId, @Param("telefono") String telefono);
}
