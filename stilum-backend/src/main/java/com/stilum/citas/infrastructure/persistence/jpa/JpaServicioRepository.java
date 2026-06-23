package com.stilum.citas.infrastructure.persistence.jpa;

import com.stilum.citas.domain.servicio.Servicio;
import com.stilum.citas.domain.servicio.ServicioRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaServicioRepository extends JpaRepository<Servicio, UUID>, ServicioRepository {

    @Override
    @Query("SELECT s FROM Servicio s WHERE s.tenant.id = :tenantId ORDER BY s.nombre")
    List<Servicio> findAllByTenantId(@Param("tenantId") UUID tenantId);

    @Override
    @Query("SELECT s FROM Servicio s WHERE s.tenant.id = :tenantId AND s.activo = true ORDER BY s.nombre")
    List<Servicio> findActivosByTenantId(@Param("tenantId") UUID tenantId);
}
