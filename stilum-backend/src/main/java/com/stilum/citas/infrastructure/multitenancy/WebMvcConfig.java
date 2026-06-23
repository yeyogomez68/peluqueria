package com.stilum.citas.infrastructure.multitenancy;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registra el TenantHibernateInterceptor en el ciclo de vida de Spring MVC.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final TenantHibernateInterceptor tenantInterceptor;

    public WebMvcConfig(TenantHibernateInterceptor tenantInterceptor) {
        this.tenantInterceptor = tenantInterceptor;
    }

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(tenantInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/**", "/api/webhook/**");
    }
}
