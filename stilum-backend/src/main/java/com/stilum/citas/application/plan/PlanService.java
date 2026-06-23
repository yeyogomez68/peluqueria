package com.stilum.citas.application.plan;

import com.stilum.citas.application.plan.dto.PlanResponse;
import com.stilum.citas.domain.plan.PlanRepository;
import com.stilum.citas.domain.shared.RecursoNoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Caso de uso: consulta de planes de suscripción.
 * SK-B-01: Application layer.
 */
@Service
@Transactional(readOnly = true)
public class PlanService {

    private final PlanRepository planRepository;

    public PlanService(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    public List<PlanResponse> listarActivos() {
        return planRepository.findAllActivos().stream()
                .map(p -> new PlanResponse(
                        p.getId(), p.getNombre(), p.getPrecioMensual(),
                        p.getMaxProfesionales(), p.getMaxCitasMes(),
                        p.isWhatsappHabilitado(), p.isRecordatoriosHabilitados(),
                        p.isReportesAvanzados(), p.isActivo()))
                .toList();
    }

    public PlanResponse obtener(UUID id) {
        return planRepository.findById(id)
                .map(p -> new PlanResponse(
                        p.getId(), p.getNombre(), p.getPrecioMensual(),
                        p.getMaxProfesionales(), p.getMaxCitasMes(),
                        p.isWhatsappHabilitado(), p.isRecordatoriosHabilitados(),
                        p.isReportesAvanzados(), p.isActivo()))
                .orElseThrow(() -> new RecursoNoEncontradoException("Plan", id));
    }
}
