package com.stilum.citas.domain.user;

public enum UserRol {
    SUPER_ADMIN,
    ADMIN_TENANT,
    PROFESIONAL;

    public boolean esTenantUser() {
        return this == ADMIN_TENANT || this == PROFESIONAL;
    }
}
