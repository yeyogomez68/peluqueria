package com.stilum.citas.infrastructure.multitenancy;

import com.stilum.citas.infrastructure.security.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Interceptor que activa el @Filter de Hibernate "tenantFilter" en cada request.
 * Inyecta el tenant_id del TenantContext → todas las queries filtrán por tenant.
 *
 * SK-B-01 / RN-TENANT-001: aislamiento automático de datos por tenant.
 *
 * Nota: las entidades que requieren aislamiento deben declarar @FilterDef y @Filter
 * en la clase de entidad correspondiente.
 */
@Component
public class TenantHibernateInterceptor implements HandlerInterceptor {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public boolean preHandle(@SuppressWarnings("NullableProblems") HttpServletRequest request,
                             @SuppressWarnings("NullableProblems") HttpServletResponse response,
                             @SuppressWarnings("NullableProblems") Object handler) {

        if (TenantContext.hasTenant()) {
            Session session = entityManager.unwrap(Session.class);
            Filter filter = session.enableFilter("tenantFilter");
            filter.setParameter("tenantId", TenantContext.get());
        }

        return true;
    }
}
