package com.stilum.citas.domain.cita.spec;

import com.stilum.citas.domain.cita.Cita;
import com.stilum.citas.domain.cita.CitaRepository;
import com.stilum.citas.domain.shared.Specification;
import com.stilum.citas.domain.subscription.Subscription;

import java.time.YearMonth;

/**
 * RN-CITA-004: El tenant no puede superar el límite mensual de citas de su plan.
 */
public class LimitePlanSpec implements Specification<Cita> {

    private final CitaRepository citaRepository;
    private final Subscription subscripcionVigente;

    public LimitePlanSpec(CitaRepository citaRepository, Subscription subscripcionVigente) {
        this.citaRepository = citaRepository;
        this.subscripcionVigente = subscripcionVigente;
    }

    @Override
    public boolean esCumplida(Cita cita) {
        int limite = subscripcionVigente.getPlan().getMaxCitasMes();
        YearMonth mes = YearMonth.from(cita.getFechaHoraInicio());
        long citasEnMes = citaRepository.contarCitasEnMes(cita.getTenantId(), mes);
        return citasEnMes < limite;
    }
}
