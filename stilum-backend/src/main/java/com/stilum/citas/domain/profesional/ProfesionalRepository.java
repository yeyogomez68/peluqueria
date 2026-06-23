package com.stilum.citas.domain.profesional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProfesionalRepository {
    Optional<Profesional> findById(UUID id);
    List<Profesional> findAllByTenantId(UUID tenantId);
    List<Profesional> findActivosByTenantId(UUID tenantId);
    int countActivosByTenantId(UUID tenantId);
    Profesional save(Profesional profesional);
}
