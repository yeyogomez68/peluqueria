package com.stilum.citas.domain.servicio;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServicioRepository {
    Optional<Servicio> findById(UUID id);
    List<Servicio> findAllByTenantId(UUID tenantId);
    List<Servicio> findActivosByTenantId(UUID tenantId);
    Servicio save(Servicio servicio);
}
