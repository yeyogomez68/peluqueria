package com.stilum.citas.infrastructure.persistence.jpa;

import com.stilum.citas.domain.profesional.Profesional;
import com.stilum.citas.domain.profesional.ProfesionalRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaProfesionalRepository extends JpaRepository<Profesional, UUID>, ProfesionalRepository {

    @Override
    @Query("SELECT p FROM Profesional p WHERE p.tenant.id = :tenantId ORDER BY p.nombre")
    List<Profesional> findAllByTenantId(@Param("tenantId") UUID tenantId);

    @Override
    @Query("SELECT p FROM Profesional p WHERE p.tenant.id = :tenantId AND p.activo = true ORDER BY p.nombre")
    List<Profesional> findActivosByTenantId(@Param("tenantId") UUID tenantId);

    @Override
    @Query("SELECT COUNT(p) FROM Profesional p WHERE p.tenant.id = :tenantId AND p.activo = true")
    int countActivosByTenantId(@Param("tenantId") UUID tenantId);
}
