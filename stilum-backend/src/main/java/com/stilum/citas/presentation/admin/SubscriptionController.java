package com.stilum.citas.presentation.admin;

import com.stilum.citas.application.subscription.SubscriptionService;
import com.stilum.citas.application.subscription.dto.ActivarSubscriptionRequest;
import com.stilum.citas.application.subscription.dto.SubscriptionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Endpoints de suscripciones — solo SUPER_ADMIN.
 */
@RestController
@RequestMapping("/api/admin/tenants/{tenantId}/subscriptions")
@PreAuthorize("hasRole('SUPER_ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin — Suscripciones", description = "Gestión de suscripciones por tenant")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @GetMapping
    @Operation(summary = "Historial de suscripciones del tenant")
    public ResponseEntity<List<SubscriptionResponse>> listar(@PathVariable UUID tenantId) {
        return ResponseEntity.ok(subscriptionService.listarPorTenant(tenantId));
    }

    @GetMapping("/vigente")
    @Operation(summary = "Suscripción actualmente vigente del tenant")
    public ResponseEntity<SubscriptionResponse> vigente(@PathVariable UUID tenantId) {
        return subscriptionService.obtenerVigente(tenantId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{subscriptionId}/activar")
    @Operation(summary = "Activar suscripción tras pago recibido")
    public ResponseEntity<SubscriptionResponse> activar(
            @PathVariable UUID tenantId,
            @PathVariable UUID subscriptionId,
            @Valid @RequestBody ActivarSubscriptionRequest req) {
        return ResponseEntity.ok(subscriptionService.activar(subscriptionId, req));
    }

    @PatchMapping("/{subscriptionId}/renovar")
    @Operation(summary = "Renovar suscripción con nueva fecha de vencimiento")
    public ResponseEntity<SubscriptionResponse> renovar(
            @PathVariable UUID tenantId,
            @PathVariable UUID subscriptionId,
            @Valid @RequestBody ActivarSubscriptionRequest req) {
        return ResponseEntity.ok(subscriptionService.renovar(subscriptionId, req));
    }

    @PatchMapping("/{subscriptionId}/cancelar")
    @Operation(summary = "Cancelar suscripción")
    public ResponseEntity<SubscriptionResponse> cancelar(
            @PathVariable UUID tenantId,
            @PathVariable UUID subscriptionId,
            @RequestBody(required = false) Map<String, String> body) {
        String motivo = body != null ? body.get("motivo") : null;
        return ResponseEntity.ok(subscriptionService.cancelar(subscriptionId, motivo));
    }
}
