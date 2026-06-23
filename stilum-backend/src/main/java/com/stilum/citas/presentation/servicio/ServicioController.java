package com.stilum.citas.presentation.servicio;

import com.stilum.citas.application.servicio.ServicioService;
import com.stilum.citas.application.servicio.dto.CreateServicioRequest;
import com.stilum.citas.application.servicio.dto.ServicioResponse;
import com.stilum.citas.application.servicio.dto.UpdateServicioRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * API REST para el catálogo de servicios del tenant.
 */
@RestController
@RequestMapping("/api/servicios")
@Tag(name = "Servicios", description = "Catálogo de servicios ofrecidos por el tenant")
public class ServicioController {

    private final ServicioService servicioService;

    public ServicioController(ServicioService servicioService) {
        this.servicioService = servicioService;
    }

    /** Listado completo — solo ADMIN_TENANT. */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN_TENANT', 'SUPER_ADMIN')")
    @Operation(summary = "Lista todos los servicios (activos e inactivos)")
    public List<ServicioResponse> listar() {
        return servicioService.listar();
    }

    /** Servicios activos — usados al crear una cita o mostrarlos en booking público. */
    @GetMapping("/activos")
    @PreAuthorize("hasAnyRole('ADMIN_TENANT', 'PROFESIONAL', 'SUPER_ADMIN')")
    @Operation(summary = "Lista servicios activos")
    public List<ServicioResponse> listarActivos() {
        return servicioService.listarActivos();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_TENANT', 'PROFESIONAL', 'SUPER_ADMIN')")
    @Operation(summary = "Obtiene un servicio por ID")
    public ServicioResponse obtener(@PathVariable UUID id) {
        return servicioService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN_TENANT', 'SUPER_ADMIN')")
    @Operation(summary = "Crea un nuevo servicio",
               description = "RN-SERV-001: duracionMin debe ser > 0")
    public ServicioResponse crear(@Valid @RequestBody CreateServicioRequest req) {
        return servicioService.crear(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_TENANT', 'SUPER_ADMIN')")
    @Operation(summary = "Actualiza un servicio")
    public ServicioResponse actualizar(@PathVariable UUID id,
                                       @Valid @RequestBody UpdateServicioRequest req) {
        return servicioService.actualizar(id, req);
    }

    @PatchMapping("/{id}/activar")
    @PreAuthorize("hasAnyRole('ADMIN_TENANT', 'SUPER_ADMIN')")
    @Operation(summary = "Activa un servicio")
    public ServicioResponse activar(@PathVariable UUID id) {
        return servicioService.activar(id);
    }

    @PatchMapping("/{id}/desactivar")
    @PreAuthorize("hasAnyRole('ADMIN_TENANT', 'SUPER_ADMIN')")
    @Operation(summary = "Desactiva un servicio")
    public ServicioResponse desactivar(@PathVariable UUID id) {
        return servicioService.desactivar(id);
    }
}
