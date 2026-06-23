package com.stilum.citas.presentation.cita;

import com.stilum.citas.application.cita.CitaService;
import com.stilum.citas.application.cita.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * API REST para el módulo de Citas.
 *
 * Endpoints de lectura: ADMIN_TENANT + PROFESIONAL.
 * Endpoints de escritura: ADMIN_TENANT (crear/cancelar/completar).
 * Check-in QR: sin autenticación (token en URL actúa como credencial).
 */
@RestController
@RequestMapping("/api/citas")
@Tag(name = "Citas", description = "Gestión del agendamiento de citas")
public class CitaController {

    private final CitaService citaService;

    public CitaController(CitaService citaService) {
        this.citaService = citaService;
    }

    // ── Consultas ──────────────────────────────────────────────────────────

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN_TENANT', 'PROFESIONAL', 'SUPER_ADMIN')")
    @Operation(summary = "Lista citas del tenant en una fecha",
               description = "Devuelve todas las citas del día para el calendario principal")
    public List<CitaResponse> listarPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return citaService.listarPorFecha(fecha);
    }

    @GetMapping("/profesional/{profesionalId}")
    @PreAuthorize("hasAnyRole('ADMIN_TENANT', 'PROFESIONAL', 'SUPER_ADMIN')")
    @Operation(summary = "Lista citas de un profesional en una fecha")
    public List<CitaResponse> listarPorProfesionalYFecha(
            @PathVariable UUID profesionalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return citaService.listarPorProfesionalYFecha(profesionalId, fecha);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_TENANT', 'PROFESIONAL', 'SUPER_ADMIN')")
    @Operation(summary = "Obtiene detalle de una cita")
    public CitaResponse obtener(@PathVariable UUID id) {
        return citaService.obtener(id);
    }

    @GetMapping("/disponibilidad")
    @PreAuthorize("hasAnyRole('ADMIN_TENANT', 'PROFESIONAL', 'SUPER_ADMIN')")
    @Operation(summary = "Consulta slots disponibles para un profesional en una fecha",
               description = "Genera slots de 30 min dentro del horario laboral y filtra los ocupados")
    public List<SlotDisponibleResponse> disponibilidad(
            @RequestParam UUID profesionalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(defaultValue = "30") int duracionMin) {
        return citaService.disponibilidad(profesionalId, fecha, duracionMin);
    }

    // ── Comandos ───────────────────────────────────────────────────────────

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN_TENANT', 'SUPER_ADMIN')")
    @Operation(summary = "Crea una nueva cita",
               description = "Aplica RN-CITA-001 (no solapamiento), RN-CITA-003 (horario laboral), RN-CITA-004 (límite plan)")
    public CitaResponse crear(@Valid @RequestBody CreateCitaRequest req) {
        return citaService.crear(req);
    }

    @PatchMapping("/{id}/confirmar")
    @PreAuthorize("hasAnyRole('ADMIN_TENANT', 'SUPER_ADMIN')")
    @Operation(summary = "Confirma una cita PENDIENTE")
    public CitaResponse confirmar(@PathVariable UUID id) {
        return citaService.confirmar(id);
    }

    @PatchMapping("/{id}/iniciar")
    @PreAuthorize("hasAnyRole('ADMIN_TENANT', 'PROFESIONAL', 'SUPER_ADMIN')")
    @Operation(summary = "Inicia una cita (pasa a EN_CURSO)")
    public CitaResponse iniciar(@PathVariable UUID id) {
        return citaService.iniciar(id);
    }

    @PatchMapping("/{id}/completar")
    @PreAuthorize("hasAnyRole('ADMIN_TENANT', 'PROFESIONAL', 'SUPER_ADMIN')")
    @Operation(summary = "Completa una cita registrando precio cobrado")
    public CitaResponse completar(@PathVariable UUID id,
                                   @RequestBody(required = false) CompletarCitaRequest req) {
        return citaService.completar(id, req != null ? req : new CompletarCitaRequest(null));
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('ADMIN_TENANT', 'PROFESIONAL', 'SUPER_ADMIN')")
    @Operation(summary = "Cancela una cita")
    public CitaResponse cancelar(@PathVariable UUID id,
                                  @RequestBody(required = false) CancelarCitaRequest req) {
        return citaService.cancelar(id, req != null ? req : new CancelarCitaRequest(null, null));
    }

    @PatchMapping("/{id}/no-show")
    @PreAuthorize("hasAnyRole('ADMIN_TENANT', 'SUPER_ADMIN')")
    @Operation(summary = "Marca una cita como NO_SHOW")
    public CitaResponse marcarNoShow(@PathVariable UUID id) {
        return citaService.marcarNoShow(id);
    }

    /**
     * Check-in QR — no requiere autenticación.
     * El qrToken actúa como credencial de un solo uso.
     */
    @PostMapping("/checkin/{qrToken}")
    @Operation(summary = "Registra check-in del cliente vía QR",
               description = "Endpoint público — el QR token identifica unívocamente la cita")
    public CitaResponse checkIn(@PathVariable UUID qrToken) {
        return citaService.checkIn(qrToken);
    }
}
