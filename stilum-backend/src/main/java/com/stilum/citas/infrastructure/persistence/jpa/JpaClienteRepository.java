package com.stilum.citas.infrastructure.persistence.jpa;

import com.stilum.citas.domain.cita.Cliente;
import com.stilum.citas.domain.cita.ClienteRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaClienteRepository extends JpaRepository<Cliente, UUID>, ClienteRepository {

    @Override
    @Query("SELECT c FROM Cliente c WHERE c.tenant.id = :tenantId AND c.telefono = :telefono")
    Optional<Cliente> findByTenantIdAndTelefono(
            @Param("tenantId") UUID tenantId,
            @Param("telefono") String telefono);
}
