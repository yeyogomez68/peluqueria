package com.stilum.citas.domain.plan;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida para la persistencia de planes.
 * SK-B-01: La interfaz vive en el dominio; la implementación en infrastructure.
 */
public interface PlanRepository {

    Optional<Plan> findById(UUID id);

    Optional<Plan> findByNombre(String nombre);

    List<Plan> findAllActivos();

    Plan save(Plan plan);
}
