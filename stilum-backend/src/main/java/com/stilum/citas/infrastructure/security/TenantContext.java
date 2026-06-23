package com.stilum.citas.infrastructure.security;

import java.util.UUID;

/**
 * Almacena el tenant_id del usuario autenticado en un ThreadLocal.
 * Hibernate @Filter lee este valor para aislar queries por tenant.
 * SK-B-01 / RN-TENANT-001.
 */
public final class TenantContext {

    private static final ThreadLocal<UUID> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {}

    public static void set(UUID tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static UUID get() {
        return CURRENT_TENANT.get();
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }

    public static boolean hasTenant() {
        return CURRENT_TENANT.get() != null;
    }
}
