package com.stilum.citas.presentation.profesional;

import com.stilum.citas.application.cita.CitaService;
import com.stilum.citas.application.cita.dto.CitaResponse;
import com.stilum.citas.application.profesional.ProfesionalService;
import com.stilum.citas.application.profesional.dto.MisIngresosResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/mi-portal")
@PreAuthorize("hasAnyRole('PROFESIONAL', 'ADMIN_TENANT')")
@Tag(name = "Mi Portal", description = "Vista del profesional: sus citas y sus ingresos")
public class MiPortalController {

    private final CitaService citaService;
    private final ProfesionalService profesionalService;

    public MiPortalController(CitaService citaService, ProfesionalService profesionalService) {
        this.citaService = citaService;
        this.profesionalService = profesionalService;
    }

    @GetMapping("/mis-citas")
    @Operation(summary = "Citas del profesional en una fecha")
    public List<CitaResponse> misCitas(
            @RequestParam UUID profesionalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return citaService.listarPorProfesionalYFecha(profesionalId, fecha);
    }

    @GetMapping("/ingresos")
    @Operation(summary = "Ingresos del profesional en un rango de fechas")
    public MisIngresosResponse misIngresos(
            @RequestParam UUID profesionalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        LocalDate finFecha = fin != null ? fin : LocalDate.now();
        return profesionalService.misIngresos(profesionalId, inicio, finFecha);
    }
}
