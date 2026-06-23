package com.stilum.citas.domain.tenant;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida para la persistencia de tenants.
 * SK-B-01: Interfaz en dominio; implementación en infrastructure.
 */
public interface TenantRepository {

    Optional<Tenant> findById(UUID id);

    Optional<Tenant> findByEmailContacto(String email);

    List<Tenant> findAll();

    List<Tenant> findAllActivos();

    Tenant save(Tenant tenant);

    boolean existsByEmailContacto(String email);
}
