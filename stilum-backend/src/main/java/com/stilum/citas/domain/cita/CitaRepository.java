package com.stilum.citas.domain.cita;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CitaRepository {

    Optional<Cita> findById(UUID id);

    Optional<Cita> findByQrToken(UUID qrToken);

    /** Citas de un tenant en un rango de fechas (para el calendario). */
    List<Cita> findByTenantIdAndFecha(UUID tenantId, LocalDate fecha);

    /** Citas de un profesional específico en una fecha. */
    List<Cita> findByProfesionalIdAndFecha(UUID profesionalId, LocalDate fecha);

    /** Verifica solapamiento de horario para un profesional. */
    boolean existeSolapamiento(UUID profesionalId, ZonedDateTime inicio,
                               ZonedDateTime fin, UUID excludeCitaId);

    /** Cuenta citas activas (no canceladas ni no-show) en un mes. */
    long contarCitasEnMes(UUID tenantId, YearMonth mes);

    /** Citas completadas de un tenant en una fecha (para contabilidad). */
    List<Cita> findCompletadasPorTenantYFecha(UUID tenantId, java.time.LocalDate fecha);

    /** Citas completadas de un profesional en un rango de fechas (para mi portal). */
    List<Cita> findCompletadasPorProfesionalYRango(UUID profesionalId, java.time.LocalDate inicio, java.time.LocalDate fin);

    Cita save(Cita cita);
}
