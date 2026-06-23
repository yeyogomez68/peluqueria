package com.stilum.citas.domain.whatsapp;

import java.util.Optional;
import java.util.UUID;

public interface ConversacionRepository {
    Optional<ConversacionWhatsApp> findByTenantIdAndTelefono(UUID tenantId, String telefono);
    ConversacionWhatsApp save(ConversacionWhatsApp conv);
    void deleteByTenantIdAndTelefono(UUID tenantId, String telefono);
}
