package com.stilum.citas.presentation.profesional;

import com.stilum.citas.application.profesional.ProfesionalService;
import com.stilum.citas.application.profesional.dto.CreateProfesionalRequest;
import com.stilum.citas.application.profesional.dto.ProfesionalResponse;
import com.stilum.citas.application.profesional.dto.UpdateProfesionalRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * API REST para gestión de profesionales del tenant.
 * Todos los endpoints requieren rol ADMIN_TENANT.
 */
@RestController
@RequestMapping("/api/profesionales")
@Tag(name = "Profesionales", description = "Gestión del equipo de profesionales")
@PreAuthorize("hasAnyRole('ADMIN_TENANT', 'SUPER_ADMIN')")
public class ProfesionalController {

    private final ProfesionalService profesionalService;

    public ProfesionalController(ProfesionalService profesionalService) {
        this.profesionalService = profesionalService;
    }

    @GetMapping
    @Operation(summary = "Lista todos los profesionales del tenant")
    public List<ProfesionalResponse> listar() {
        return profesionalService.listar();
    }

    @GetMapping("/activos")
    @PreAuthorize("hasAnyRole('ADMIN_TENANT', 'PROFESIONAL', 'SUPER_ADMIN')")
    @Operation(summary = "Lista profesionales activos — usado por el calendario y alta de citas")
    public List<ProfesionalResponse> listarActivos() {
        return profesionalService.listarActivos();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_TENANT', 'PROFESIONAL', 'SUPER_ADMIN')")
    @Operation(summary = "Obtiene un profesional por ID")
    public ProfesionalResponse obtener(@PathVariable UUID id) {
        return profesionalService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crea un nuevo profesional",
               description = "RN-PROF-001: Verifica límite de profesionales según el plan vigente")
    public ProfesionalResponse crear(@Valid @RequestBody CreateProfesionalRequest req) {
        return profesionalService.crear(req);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza datos y horarios de un profesional")
    public ProfesionalResponse actualizar(@PathVariable UUID id,
                                          @Valid @RequestBody UpdateProfesionalRequest req) {
        return profesionalService.actualizar(id, req);
    }

    @PatchMapping("/{id}/activar")
    @Operation(summary = "Activa un profesional",
               description = "RN-PROF-001: Verifica que no se supere el límite del plan")
    public ProfesionalResponse activar(@PathVariable UUID id) {
        return profesionalService.activar(id);
    }

    @PatchMapping("/{id}/desactivar")
    @Operation(summary = "Desactiva un profesional")
    public ProfesionalResponse desactivar(@PathVariable UUID id) {
        return profesionalService.desactivar(id);
    }

    @PatchMapping("/{id}/comision")
    @PreAuthorize("hasRole('ADMIN_TENANT')")
    @Operation(summary = "Actualiza porcentaje de comisión del profesional")
    public ProfesionalResponse actualizarComision(
            @PathVariable UUID id,
            @RequestParam @jakarta.validation.constraints.DecimalMin("0")
            @jakarta.validation.constraints.DecimalMax("100") java.math.BigDecimal porcentaje) {
        return profesionalService.actualizarComision(id, porcentaje);
    }
}
