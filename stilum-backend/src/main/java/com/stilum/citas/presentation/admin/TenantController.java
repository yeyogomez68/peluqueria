package com.stilum.citas.presentation.admin;

import com.stilum.citas.application.tenant.TenantService;
import com.stilum.citas.application.tenant.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Endpoints de gestión de tenants — solo SUPER_ADMIN.
 * SK-B-11: Controladores delgados; toda la lógica en TenantService.
 */
@RestController
@RequestMapping("/api/admin/tenants")
@PreAuthorize("hasRole('SUPER_ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin — Tenants", description = "Gestión de negocios suscritos a la plataforma")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @GetMapping
    @Operation(summary = "Listar todos los tenants")
    public ResponseEntity<List<TenantSummaryResponse>> listar() {
        return ResponseEntity.ok(tenantService.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de un tenant")
    public ResponseEntity<TenantResponse> obtener(@PathVariable UUID id) {
        return ResponseEntity.ok(tenantService.obtener(id));
    }

    @PostMapping
    @Operation(summary = "Crear nuevo tenant", description = "Crea el tenant, suscripción de prueba y usuario admin inicial")
    public ResponseEntity<TenantResponse> crear(@Valid @RequestBody CreateTenantRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tenantService.crear(req));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar datos del tenant")
    public ResponseEntity<TenantResponse> actualizar(@PathVariable UUID id,
                                                      @Valid @RequestBody UpdateTenantRequest req) {
        return ResponseEntity.ok(tenantService.actualizar(id, req));
    }

    @PatchMapping("/{id}/activar")
    @Operation(summary = "Activar acceso del tenant")
    public ResponseEntity<TenantResponse> activar(@PathVariable UUID id) {
        return ResponseEntity.ok(tenantService.activar(id));
    }

    @PatchMapping("/{id}/inactivar")
    @Operation(summary = "Bloquear acceso del tenant sin borrar datos")
    public ResponseEntity<TenantResponse> inactivar(@PathVariable UUID id) {
        return ResponseEntity.ok(tenantService.inactivar(id));
    }
}
