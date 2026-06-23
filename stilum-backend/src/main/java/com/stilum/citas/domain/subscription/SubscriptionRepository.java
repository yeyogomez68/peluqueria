package com.stilum.citas.domain.subscription;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository {

    Optional<Subscription> findById(UUID id);

    /** Suscripción vigente actual de un tenant (ACTIVA o PRUEBA con fecha_fin >= hoy). */
    Optional<Subscription> findVigenteByTenantId(UUID tenantId);

    List<Subscription> findAllByTenantId(UUID tenantId);

    Subscription save(Subscription subscription);
}
