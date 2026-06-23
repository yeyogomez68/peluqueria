package com.stilum.citas.infrastructure.persistence.jpa;

import com.stilum.citas.domain.cita.Cita;
import com.stilum.citas.domain.cita.CitaRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaCitaRepository extends JpaRepository<Cita, UUID>, CitaRepository {

    // Spring Data auto-derives this from the `qrToken` field
    @Override
    Optional<Cita> findByQrToken(UUID qrToken);

    @Override
    @Query("""
        SELECT c FROM Cita c
        WHERE c.tenant.id = :tenantId
          AND CAST(c.fechaHoraInicio AS date) = :fecha
        ORDER BY c.fechaHoraInicio
        """)
    List<Cita> findByTenantIdAndFecha(
            @Param("tenantId") UUID tenantId,
            @Param("fecha") LocalDate fecha);

    @Override
    @Query("""
        SELECT c FROM Cita c
        WHERE c.profesional.id = :profesionalId
          AND CAST(c.fechaHoraInicio AS date) = :fecha
        ORDER BY c.fechaHoraInicio
        """)
    List<Cita> findByProfesionalIdAndFecha(
            @Param("profesionalId") UUID profesionalId,
            @Param("fecha") LocalDate fecha);

    @Override
    @Query("""
        SELECT COUNT(c) > 0 FROM Cita c
        WHERE c.profesional.id = :profesionalId
          AND c.estado NOT IN ('CANCELADA', 'NO_SHOW')
          AND c.fechaHoraInicio < :fin
          AND c.fechaHoraFin > :inicio
          AND (:excludeId IS NULL OR c.id <> :excludeId)
        """)
    boolean existeSolapamiento(
            @Param("profesionalId") UUID profesionalId,
            @Param("inicio") ZonedDateTime inicio,
            @Param("fin") ZonedDateTime fin,
            @Param("excludeId") UUID excludeCitaId);

    /** Delegado interno — YearMonth se descompone a int antes de pasarle al JPQL. */
    @Query("""
        SELECT COUNT(c) FROM Cita c
        WHERE c.tenant.id = :tenantId
          AND c.estado NOT IN ('CANCELADA', 'NO_SHOW')
          AND YEAR(c.fechaHoraInicio) = :year
          AND MONTH(c.fechaHoraInicio) = :month
        """)
    long contarCitasEnMesByYearMonth(
            @Param("tenantId") UUID tenantId,
            @Param("year") int year,
            @Param("month") int month);

    @Override
    default long contarCitasEnMes(UUID tenantId, YearMonth mes) {
        return contarCitasEnMesByYearMonth(tenantId, mes.getYear(), mes.getMonthValue());
    }

    @Override
    @Query("""
        SELECT c FROM Cita c
        JOIN FETCH c.profesional
        JOIN FETCH c.servicio
        WHERE c.tenant.id = :tenantId
          AND c.estado = 'COMPLETADA'
          AND CAST(c.fechaHoraInicio AS date) = :fecha
        ORDER BY c.fechaHoraInicio
        """)
    List<Cita> findCompletadasPorTenantYFecha(
            @Param("tenantId") UUID tenantId,
            @Param("fecha") java.time.LocalDate fecha);
}
