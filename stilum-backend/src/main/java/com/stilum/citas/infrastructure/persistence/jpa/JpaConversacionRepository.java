package com.stilum.citas.infrastructure.persistence.jpa;

import com.stilum.citas.domain.whatsapp.ConversacionRepository;
import com.stilum.citas.domain.whatsapp.ConversacionWhatsApp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaConversacionRepository
        extends JpaRepository<ConversacionWhatsApp, UUID>, ConversacionRepository {

    Optional<ConversacionWhatsApp> findByTenantIdAndTelefono(UUID tenantId, String telefono);

    void deleteByTenantIdAndTelefono(UUID tenantId, String telefono);
}
