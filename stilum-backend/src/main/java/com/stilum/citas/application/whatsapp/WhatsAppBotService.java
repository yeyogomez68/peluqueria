package com.stilum.citas.application.whatsapp;

import com.stilum.citas.domain.profesional.Profesional;
import com.stilum.citas.domain.profesional.ProfesionalRepository;
import com.stilum.citas.domain.servicio.Servicio;
import com.stilum.citas.domain.servicio.ServicioRepository;
import com.stilum.citas.domain.tenant.Tenant;
import com.stilum.citas.domain.tenant.TenantRepository;
import com.stilum.citas.domain.whatsapp.ConversacionRepository;
import com.stilum.citas.domain.whatsapp.ConversacionWhatsApp;
import com.stilum.citas.infrastructure.security.TenantContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class WhatsAppBotService {

    private final ConversacionRepository conversacionRepository;
    private final ProfesionalRepository profesionalRepository;
    private final ServicioRepository servicioRepository;
    private final TenantRepository tenantRepository;
    private final WebClient webClient;

    @Value("${stilum.claude.api-key:}")
    private String claudeApiKey;

    @Value("${stilum.claude.model:claude-haiku-4-5-20251001}")
    private String claudeModel;

    @Value("${stilum.claude.api-url:https://api.anthropic.com/v1/messages}")
    private String claudeApiUrl;

    public WhatsAppBotService(ConversacionRepository conversacionRepository,
                               ProfesionalRepository profesionalRepository,
                               ServicioRepository servicioRepository,
                               TenantRepository tenantRepository,
                               WebClient.Builder webClientBuilder) {
        this.conversacionRepository = conversacionRepository;
        this.profesionalRepository = profesionalRepository;
        this.servicioRepository = servicioRepository;
        this.tenantRepository = tenantRepository;
        this.webClient = webClientBuilder.build();
    }

    public String procesarMensaje(UUID tenantId, String telefono, String texto) {
        TenantContext.set(tenantId);
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant no encontrado: " + tenantId));

        ConversacionWhatsApp conv = conversacionRepository
                .findByTenantIdAndTelefono(tenantId, telefono)
                .orElseGet(() -> conversacionRepository.save(
                        ConversacionWhatsApp.iniciar(tenant, telefono)));

        return switch (conv.getEstado()) {
            case "INICIO" -> manejarInicio(conv, tenant.getNombreNegocio());
            case "ESPERANDO_NECESIDAD" -> manejarNecesidad(conv, texto, tenantId);
            case "ESPERANDO_FECHA" -> manejarFecha(conv, texto);
            case "ESPERANDO_CONFIRMACION" -> manejarConfirmacion(conv, texto, tenant.getNombreNegocio());
            default -> reiniciar(conv, tenant.getNombreNegocio());
        };
    }

    private String manejarInicio(ConversacionWhatsApp conv, String nombreNegocio) {
        conv.avanzarEstado("ESPERANDO_NECESIDAD", "{}");
        conversacionRepository.save(conv);
        return "¡Hola! Bienvenido a *" + nombreNegocio + "*. ¿Qué servicio estás buscando hoy? "
                + "(Ej: corte de cabello, tinte, manicure...)";
    }

    private String manejarNecesidad(ConversacionWhatsApp conv, String necesidad, UUID tenantId) {
        List<Profesional> profesionales = profesionalRepository.findActivosByTenantId(tenantId);
        List<Servicio> servicios = servicioRepository.findActivosByTenantId(tenantId);

        String recomendacion = consultarIA(necesidad, profesionales, servicios);

        conv.avanzarEstado("ESPERANDO_FECHA", "{\"necesidad\":\"" + necesidad.replace("\"", "'") + "\"}");
        conversacionRepository.save(conv);
        return recomendacion + "\n\n¿Para qué fecha te gustaría la cita? (Ej: mañana, el jueves, 25/06/2026)";
    }

    private String manejarFecha(ConversacionWhatsApp conv, String fechaTexto) {
        conv.avanzarEstado("ESPERANDO_CONFIRMACION", conv.getDatosJson());
        conversacionRepository.save(conv);
        return "Perfecto, te agendaré para *" + fechaTexto + "*. "
                + "¿Confirmas? Responde *SÍ* para confirmar o *NO* para cancelar.";
    }

    private String manejarConfirmacion(ConversacionWhatsApp conv, String respuesta, String nombreNegocio) {
        String r = respuesta.trim().toUpperCase();
        if (r.equals("SÍ") || r.equals("SI") || r.equals("S") || r.startsWith("SI")) {
            conv.avanzarEstado("COMPLETADO", conv.getDatosJson());
            conversacionRepository.save(conv);
            return "✅ ¡Tu cita ha sido registrada! Te esperamos en *" + nombreNegocio
                    + "*. Gracias por preferirnos. 💇";
        } else {
            conv.avanzarEstado("INICIO", "{}");
            conversacionRepository.save(conv);
            return "Cita cancelada. Escríbenos cuando quieras agendar de nuevo. ¡Hasta pronto! 👋";
        }
    }

    private String reiniciar(ConversacionWhatsApp conv, String nombreNegocio) {
        conv.avanzarEstado("INICIO", "{}");
        conversacionRepository.save(conv);
        return "¡Hola de nuevo! Bienvenido a *" + nombreNegocio + "*. ¿En qué te podemos ayudar?";
    }

    private String consultarIA(String necesidad, List<Profesional> profesionales, List<Servicio> servicios) {
        if (claudeApiKey == null || claudeApiKey.isBlank()) {
            return "Tenemos excelentes profesionales disponibles para *" + necesidad + "*. "
                    + "Cualquiera de ellos podrá atenderte con la mejor calidad. 💫";
        }
        try {
            String prompt = buildPrompt(necesidad, profesionales, servicios);
            Map<String, Object> body = Map.of(
                    "model", claudeModel,
                    "max_tokens", 300,
                    "messages", List.of(Map.of("role", "user", "content", prompt))
            );
            Map<?, ?> response = webClient.post()
                    .uri(claudeApiUrl)
                    .header("x-api-key", claudeApiKey)
                    .header("anthropic-version", "2023-06-01")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            if (response != null && response.containsKey("content")) {
                List<?> content = (List<?>) response.get("content");
                if (!content.isEmpty()) {
                    Map<?, ?> first = (Map<?, ?>) content.get(0);
                    Object text = first.get("text");
                    return text != null ? text.toString() : "";
                }
            }
        } catch (Exception ignored) {}
        return "Tenemos profesionales listos para ayudarte con *" + necesidad + "*. 🌟";
    }

    private String buildPrompt(String necesidad, List<Profesional> profesionales, List<Servicio> servicios) {
        StringBuilder sb = new StringBuilder();
        sb.append("Eres el asistente virtual de una peluquería/salón de belleza colombiano. ");
        sb.append("El cliente busca: ").append(necesidad).append(".\n\n");
        sb.append("Profesionales disponibles:\n");
        profesionales.forEach(p ->
                sb.append("- ").append(p.getNombre())
                  .append(p.getEspecialidad() != null ? " (especialidad: " + p.getEspecialidad() + ")" : "")
                  .append("\n"));
        sb.append("\nServicios disponibles:\n");
        servicios.forEach(s ->
                sb.append("- ").append(s.getNombre())
                  .append(" $").append(s.getPrecio())
                  .append(" / ").append(s.getDuracionMin()).append("min\n"));
        sb.append("\nEn 2-3 líneas amables con emojis, recomienda el profesional más adecuado y el servicio. ")
          .append("Responde en español colombiano.");
        return sb.toString();
    }
}
