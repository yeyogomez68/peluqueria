package com.stilum.citas.presentation.whatsapp;

import com.stilum.citas.application.whatsapp.WhatsAppBotService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/webhook/whatsapp")
@Tag(name = "WhatsApp Webhook", description = "Webhook para mensajes de WhatsApp Business API")
public class WhatsAppWebhookController {

    private final WhatsAppBotService botService;

    @Value("${stilum.whatsapp.verify-token:stilum-verify-2024}")
    private String verifyToken;

    public WhatsAppWebhookController(WhatsAppBotService botService) {
        this.botService = botService;
    }

    @GetMapping("/{tenantId}")
    public ResponseEntity<String> verificar(
            @PathVariable UUID tenantId,
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String token,
            @RequestParam("hub.challenge") String challenge) {
        if ("subscribe".equals(mode) && verifyToken.equals(token)) {
            return ResponseEntity.ok(challenge);
        }
        return ResponseEntity.status(403).body("Forbidden");
    }

    @PostMapping("/{tenantId}")
    public ResponseEntity<Void> recibirMensaje(
            @PathVariable UUID tenantId,
            @RequestBody Map<String, Object> payload) {
        try {
            String telefono = extraerCampo(payload, "from");
            String texto = extraerTextoBody(payload);
            if (telefono != null && texto != null) {
                botService.procesarMensaje(tenantId, telefono, texto);
            }
        } catch (Exception ignored) {}
        return ResponseEntity.ok().build();
    }

    @SuppressWarnings("unchecked")
    private String extraerCampo(Map<String, Object> payload, String campo) {
        try {
            List<Map<String, Object>> entries = (List<Map<String, Object>>) payload.get("entry");
            List<Map<String, Object>> changes = (List<Map<String, Object>>) entries.get(0).get("changes");
            Map<String, Object> value = (Map<String, Object>) changes.get(0).get("value");
            List<Map<String, Object>> messages = (List<Map<String, Object>>) value.get("messages");
            Object v = messages.get(0).get(campo);
            return v != null ? v.toString() : null;
        } catch (Exception ignored) { return null; }
    }

    @SuppressWarnings("unchecked")
    private String extraerTextoBody(Map<String, Object> payload) {
        try {
            List<Map<String, Object>> entries = (List<Map<String, Object>>) payload.get("entry");
            List<Map<String, Object>> changes = (List<Map<String, Object>>) entries.get(0).get("changes");
            Map<String, Object> value = (Map<String, Object>) changes.get(0).get("value");
            List<Map<String, Object>> messages = (List<Map<String, Object>>) value.get("messages");
            Map<String, Object> textObj = (Map<String, Object>) messages.get(0).get("text");
            Object body = textObj.get("body");
            return body != null ? body.toString() : null;
        } catch (Exception ignored) { return null; }
    }
}
