package com.stilum.citas.presentation.contabilidad;

import com.stilum.citas.application.contabilidad.ContabilidadService;
import com.stilum.citas.application.contabilidad.dto.ResumenDiaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/contabilidad")
@Tag(name = "Contabilidad", description = "Cierre de caja y resúmenes financieros")
public class ContabilidadController {

    private final ContabilidadService contabilidadService;

    public ContabilidadController(ContabilidadService contabilidadService) {
        this.contabilidadService = contabilidadService;
    }

    @GetMapping("/resumen-dia")
    @PreAuthorize("hasAnyRole('ADMIN_TENANT', 'SUPER_ADMIN')")
    @Operation(summary = "Resumen financiero del día",
               description = "Total vendido, comisiones, desglose por profesional, servicio y método de pago")
    public ResumenDiaResponse resumenDia(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return contabilidadService.resumenDia(fecha != null ? fecha : LocalDate.now());
    }
}
