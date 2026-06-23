package com.stilum.citas.infrastructure.persistence.jpa;

import com.stilum.citas.domain.subscription.Subscription;
import com.stilum.citas.domain.subscription.SubscriptionEstado;
import com.stilum.citas.domain.subscription.SubscriptionRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaSubscriptionRepository extends JpaRepository<Subscription, UUID>, SubscriptionRepository {

    @Query("""
            SELECT s FROM Subscription s
            WHERE s.tenant.id = :tenantId
              AND s.estado IN ('ACTIVA', 'PRUEBA')
              AND s.fechaFin >= :hoy
            ORDER BY s.fechaFin DESC
            LIMIT 1
            """)
    Optional<Subscription> findVigenteByTenantId(
            @Param("tenantId") UUID tenantId,
            @Param("hoy") LocalDate hoy);

    @Override
    default Optional<Subscription> findVigenteByTenantId(UUID tenantId) {
        return findVigenteByTenantId(tenantId, LocalDate.now());
    }

    @Override
    @Query("SELECT s FROM Subscription s WHERE s.tenant.id = :tenantId ORDER BY s.createdAt DESC")
    List<Subscription> findAllByTenantId(@Param("tenantId") UUID tenantId);
}
