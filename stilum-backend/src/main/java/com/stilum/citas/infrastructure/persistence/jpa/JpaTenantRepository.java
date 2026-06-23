package com.stilum.citas.infrastructure.persistence.jpa;

import com.stilum.citas.domain.tenant.Tenant;
import com.stilum.citas.domain.tenant.TenantRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaTenantRepository extends JpaRepository<Tenant, UUID>, TenantRepository {

    @Override
    Optional<Tenant> findByEmailContacto(String emailContacto);

    @Override
    boolean existsByEmailContacto(String emailContacto);

    @Override
    @Query("SELECT t FROM Tenant t WHERE t.activo = true")
    List<Tenant> findAllActivos();
}
