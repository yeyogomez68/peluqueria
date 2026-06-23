package com.stilum.citas.application.contabilidad;

import com.stilum.citas.application.contabilidad.dto.ResumenDiaResponse;
import com.stilum.citas.domain.cita.Cita;
import com.stilum.citas.domain.cita.CitaRepository;
import com.stilum.citas.infrastructure.security.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ContabilidadService {

    private final CitaRepository citaRepository;

    public ContabilidadService(CitaRepository citaRepository) {
        this.citaRepository = citaRepository;
    }

    public ResumenDiaResponse resumenDia(LocalDate fecha) {
        UUID tenantId = TenantContext.get();
        List<Cita> citas = citaRepository.findCompletadasPorTenantYFecha(tenantId, fecha);

        BigDecimal totalVendido = citas.stream()
                .map(c -> c.getPrecioCobrado() != null ? c.getPrecioCobrado() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalComisiones = citas.stream()
                .map(c -> c.getComisionCalculada() != null ? c.getComisionCalculada() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalNegocio = totalVendido.subtract(totalComisiones);

        List<ResumenDiaResponse.ResumenProfesional> porProfesional = citas.stream()
                .collect(Collectors.groupingBy(c -> c.getProfesional().getId()))
                .values().stream()
                .map(grupo -> {
                    Cita primera = grupo.get(0);
                    BigDecimal venta = grupo.stream()
                            .map(c -> c.getPrecioCobrado() != null ? c.getPrecioCobrado() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal comision = grupo.stream()
                            .map(c -> c.getComisionCalculada() != null ? c.getComisionCalculada() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new ResumenDiaResponse.ResumenProfesional(
                            primera.getProfesional().getNombre(),
                            grupo.size(), venta, comision,
                            primera.getProfesional().getComisionPorcentaje()
                    );
                })
                .sorted(Comparator.comparing(ResumenDiaResponse.ResumenProfesional::totalVendido).reversed())
                .toList();

        List<ResumenDiaResponse.ResumenServicio> porServicio = citas.stream()
                .collect(Collectors.groupingBy(c -> c.getServicio().getId()))
                .values().stream()
                .map(grupo -> {
                    Cita primera = grupo.get(0);
                    BigDecimal venta = grupo.stream()
                            .map(c -> c.getPrecioCobrado() != null ? c.getPrecioCobrado() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new ResumenDiaResponse.ResumenServicio(
                            primera.getServicio().getNombre(), grupo.size(), venta);
                })
                .sorted(Comparator.comparing(ResumenDiaResponse.ResumenServicio::cantidad).reversed())
                .toList();

        List<ResumenDiaResponse.ResumenMetodoPago> porMetodoPago = citas.stream()
                .filter(c -> c.getMetodoPago() != null)
                .collect(Collectors.groupingBy(Cita::getMetodoPago))
                .entrySet().stream()
                .map(e -> {
                    BigDecimal total = e.getValue().stream()
                            .map(c -> c.getPrecioCobrado() != null ? c.getPrecioCobrado() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new ResumenDiaResponse.ResumenMetodoPago(e.getKey(), e.getValue().size(), total);
                })
                .toList();

        return new ResumenDiaResponse(fecha, citas.size(), totalVendido,
                totalComisiones, totalNegocio, porProfesional, porServicio, porMetodoPago);
    }
}
