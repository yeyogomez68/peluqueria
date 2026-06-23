package com.stilum.citas.domain.cita;

import java.util.Optional;
import java.util.UUID;

public interface ClienteRepository {
    Optional<Cliente> findById(UUID id);
    Optional<Cliente> findByTenantIdAndTelefono(UUID tenantId, String telefono);
    Cliente save(Cliente cliente);
}
