package com.stilum.citas.infrastructure.persistence.jpa;

import com.stilum.citas.domain.plan.Plan;
import com.stilum.citas.domain.plan.PlanRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementación JPA del repositorio de planes.
 * SK-B-01: Implementación en infrastructure, interfaz en domain.
 */
@Repository
public interface JpaPlanRepository extends JpaRepository<Plan, UUID>, PlanRepository {

    @Override
    Optional<Plan> findByNombre(String nombre);

    @Override
    default List<Plan> findAllActivos() {
        return findAll().stream().filter(Plan::isActivo).toList();
    }
}
