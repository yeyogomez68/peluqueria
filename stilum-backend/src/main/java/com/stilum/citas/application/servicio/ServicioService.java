package com.stilum.citas.application.servicio;

import com.stilum.citas.application.servicio.dto.CreateServicioRequest;
import com.stilum.citas.application.servicio.dto.ServicioResponse;
import com.stilum.citas.application.servicio.dto.UpdateServicioRequest;
import com.stilum.citas.domain.servicio.Servicio;
import com.stilum.citas.domain.servicio.ServicioRepository;
import com.stilum.citas.domain.shared.AccesoNoAutorizadoException;
import com.stilum.citas.domain.shared.RecursoNoEncontradoException;
import com.stilum.citas.domain.tenant.Tenant;
import com.stilum.citas.domain.tenant.TenantRepository;
import com.stilum.citas.infrastructure.security.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Casos de uso del catálogo de servicios del tenant.
 * RN-SERV-001: duracionMin > 0 validado en el dominio.
 * RN-TENANT-001: Aislamiento por TenantContext + @Filter.
 */
@Service
@Transactional(readOnly = true)
public class ServicioService {

    private final ServicioRepository servicioRepository;
    private final TenantRepository tenantRepository;

    public ServicioService(ServicioRepository servicioRepository,
                           TenantRepository tenantRepository) {
        this.servicioRepository = servicioRepository;
        this.tenantRepository = tenantRepository;
    }

    public List<ServicioResponse> listar() {
        UUID tenantId = requireTenantContext();
        return servicioRepository.findAllByTenantId(tenantId).stream()
                .map(ServicioResponse::from)
                .toList();
    }

    public List<ServicioResponse> listarActivos() {
        UUID tenantId = requireTenantContext();
        return servicioRepository.findActivosByTenantId(tenantId).stream()
                .map(ServicioResponse::from)
                .toList();
    }

    public ServicioResponse obtener(UUID id) {
        return ServicioResponse.from(findAndVerify(id));
    }

    @Transactional
    public ServicioResponse crear(CreateServicioRequest req) {
        UUID tenantId = requireTenantContext();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Tenant", tenantId));

        Servicio s = Servicio.crear(tenant, req.nombre(), req.descripcion(),
                req.duracionMin(), req.precio());
        return ServicioResponse.from(servicioRepository.save(s));
    }

    @Transactional
    public ServicioResponse actualizar(UUID id, UpdateServicioRequest req) {
        Servicio s = findAndVerify(id);
        s.actualizar(req.nombre(), req.descripcion(), req.duracionMin(), req.precio());
        return ServicioResponse.from(servicioRepository.save(s));
    }

    @Transactional
    public ServicioResponse activar(UUID id) {
        Servicio s = findAndVerify(id);
        s.activar();
        return ServicioResponse.from(servicioRepository.save(s));
    }

    @Transactional
    public ServicioResponse desactivar(UUID id) {
        Servicio s = findAndVerify(id);
        s.desactivar();
        return ServicioResponse.from(servicioRepository.save(s));
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private UUID requireTenantContext() {
        UUID tenantId = TenantContext.get();
        if (tenantId == null) {
            throw new AccesoNoAutorizadoException("Operación requiere contexto de tenant");
        }
        return tenantId;
    }

    private Servicio findAndVerify(UUID id) {
        Servicio s = servicioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Servicio", id));
        UUID tenantId = TenantContext.get();
        if (tenantId != null && !tenantId.equals(s.getTenantId())) {
            throw new AccesoNoAutorizadoException("No tienes acceso a este servicio");
        }
        return s;
    }
}
