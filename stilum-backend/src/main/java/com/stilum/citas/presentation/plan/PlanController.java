package com.stilum.citas.presentation.plan;

import com.stilum.citas.application.plan.PlanService;
import com.stilum.citas.application.plan.dto.PlanResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Endpoints de planes — públicos para mostrar en landing page.
 */
@RestController
@RequestMapping("/api/planes")
@Tag(name = "Planes", description = "Planes de suscripción disponibles")
public class PlanController {

    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @GetMapping
    @Operation(summary = "Listar planes activos", description = "Público — usado en la landing page")
    public ResponseEntity<List<PlanResponse>> listar() {
        return ResponseEntity.ok(planService.listarActivos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de un plan")
    public ResponseEntity<PlanResponse> obtener(@PathVariable UUID id) {
        return ResponseEntity.ok(planService.obtener(id));
    }
}
