package com.stilum.citas.domain.profesional;

import com.stilum.citas.domain.tenant.Tenant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;

import java.time.LocalTime;
import java.util.UUID;

/**
 * Horario de disponibilidad de un profesional por día de semana.
 * RN-PROF-002: Validación de horario laboral al crear citas.
 */
@Entity
@Table(name = "horarios_profesional")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class HorarioProfesional {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profesional_id", nullable = false)
    private Profesional profesional;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    /** Día de semana ISO: 1=Lunes … 7=Domingo */
    @Column(name = "dia_semana", nullable = false)
    @JdbcTypeCode(Types.SMALLINT)
    private int diaSemana;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Column(nullable = false)
    private boolean activo = true;

    public static HorarioProfesional crear(Profesional profesional, Tenant tenant,
                                           int diaSemana, LocalTime horaInicio, LocalTime horaFin) {
        HorarioProfesional h = new HorarioProfesional();
        h.profesional = profesional;
        h.tenant = tenant;
        h.diaSemana = diaSemana;
        h.horaInicio = horaInicio;
        h.horaFin = horaFin;
        return h;
    }

    public void actualizar(LocalTime horaInicio, LocalTime horaFin) {
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }

    public boolean cubrea(LocalTime hora) {
        return !hora.isBefore(horaInicio) && hora.isBefore(horaFin);
    }
}
