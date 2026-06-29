# Final Code Review Report — Feature Branch Stilum (V12 + V13)

**Reviewer:** Claude Code (automated)  
**Date:** 2026-06-22  
**Branch scope:** 7 commits, 24 files, 784 insertions  
**Modules reviewed:** Cobro + Comisiones, Contabilidad Diaria, Portal del Profesional, WhatsApp Bot + IA

---

## Spec Compliance

| Module | Endpoint / Artefact | Status | Notes |
|--------|---------------------|--------|-------|
| Cobro + Comisiones | `comision_porcentaje` on `profesionales` | ✅ | V12 + `Profesional.comisionPorcentaje` aligned |
| Cobro + Comisiones | `metodo_pago` / `comision_calculada` on `citas` | ✅ | V12 + `Cita` fields aligned |
| Cobro + Comisiones | `PATCH /api/citas/{id}/pagar` | ✅ | `CitaController.registrarPago` present, wired to `CitaService.registrarPago` |
| Cobro + Comisiones | `PATCH /api/profesionales/{id}/comision` | ✅ | `ProfesionalController.actualizarComision` present |
| Contabilidad Diaria | `GET /api/contabilidad/resumen-dia` | ✅ | `ContabilidadController` + `ContabilidadService` present and complete |
| Contabilidad Diaria | Aggregation by professional, service, payment method | ✅ | All three breakdowns present in `ContabilidadService.resumenDia()` |
| Portal Profesional | `GET /api/mi-portal/mis-citas` | ✅ | `MiPortalController` present |
| Portal Profesional | `GET /api/mi-portal/ingresos` | ✅ | Delegates to `ProfesionalService.misIngresos` |
| WhatsApp Bot | V13 migration | ✅ | `whatsapp_conversaciones` table created |
| WhatsApp Bot | `ConversacionWhatsApp` entity | ✅ | Present, correct structure |
| WhatsApp Bot | `WhatsAppBotService` with Claude AI | ✅ | `consultarIA()` calls Anthropic API with graceful fallback |
| WhatsApp Bot | Webhook controller for Meta Business API | ✅ | GET verification + POST handler present |
| DTOs as Java 21 records | All new DTOs | ✅ | `RegistrarPagoRequest`, `ResumenDiaResponse`, `MisIngresosResponse`, `WhatsAppMensajeEntranteDto` are all records |
| Flyway | V12 and V13 created, no older migrations modified | ✅ | Only V12 and V13 exist as new files |

---

## Critical Issues (Must Fix)

### 1. `ConversacionWhatsApp` missing `@Filter` annotation — tenant filter NOT applied

**File:** `stilum-backend/src/main/java/com/stilum/citas/domain/whatsapp/ConversacionWhatsApp.java`

The entity has `tenant_id` correctly mapped and the `@FilterDef` is declared in `package-info.java`, but the entity itself lacks:

```java
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
```

All other entities (`Cita`, `Profesional`) have this annotation. Without it, Hibernate will not apply the `tenantFilter` to queries that hit `whatsapp_conversaciones`, meaning a compromised `tenantId` path variable in the webhook URL could — in theory — load conversations from another tenant if the JPA repository were queried without the explicit `tenantId` parameter. While the `ConversacionRepository.findByTenantIdAndTelefono` method does pass `tenantId` explicitly (mitigating direct data leak today), the absence of the filter is an architectural contract violation and a latent risk if future queries are added without the explicit parameter.

**Fix:** Add `@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")` to the class declaration, between `@Table` and `@Getter`.

---

### 2. `WhatsAppBotService.procesarMensaje` calls `TenantContext.set()` from a non-HTTP thread — leaks context

**File:** `stilum-backend/src/main/java/com/stilum/citas/application/whatsapp/WhatsAppBotService.java`, line 54

```java
public String procesarMensaje(UUID tenantId, String telefono, String texto) {
    TenantContext.set(tenantId);   // <-- problem
```

The webhook endpoint is already authenticated (or rather, unauthenticated — it is under `/api/webhook/**` which is `permitAll()`). The `TenantContext` is normally populated by `JwtAuthFilter`. Calling `TenantContext.set()` inside a service method is fragile:

- If `TenantContext` is backed by a `ThreadLocal`, this works, but the value is never cleared after the service call returns (there is no `TenantContext.clear()` call after the `procesarMensaje` logic). This is a **thread-local leak** when requests are handled by a thread pool. A subsequent request on the same thread could inherit the wrong `tenantId`.
- It also circumvents the security boundary: tenant isolation is the responsibility of the infrastructure layer (filter), not the application layer.

**Fix:** Either (a) clear the context with a `try/finally` block in `procesarMensaje`, or (b) remove the `TenantContext.set()` call from the service entirely and instead pass `tenantId` explicitly throughout all downstream queries (which the service already does via `tenantId` parameters — so the `TenantContext.set` call is redundant and only adds risk).

---

### 3. Webhook `/{tenantId}` is fully public with no HMAC signature validation

**File:** `stilum-backend/src/main/java/com/stilum/citas/presentation/whatsapp/WhatsAppWebhookController.java`

The `POST /{tenantId}` endpoint accepts any payload from any source and immediately processes it. Meta Business API sends an `X-Hub-Signature-256` HMAC-SHA256 header on every webhook delivery. Without verifying it, any actor who discovers the URL can:

1. Send arbitrary payloads that trigger AI calls (cost amplification attack against the Claude API key).
2. Inject crafted phone numbers and messages to manipulate conversation state for any tenant.

This is a known Meta webhook security requirement and is not present in the code.

**Fix:** Inject `stilum.whatsapp.app-secret`, compute `HMAC-SHA256(rawRequestBody, appSecret)`, and compare with the `X-Hub-Signature-256` header value before processing. Requires reading the raw body as `byte[]` or `String` before deserialization (use `@RequestBody String rawBody` + manual Jackson parsing, or a `HandlerInterceptor`).

---

## Important Issues (Should Fix)

### 4. `MiPortalController` accepts `profesionalId` as a free request parameter — no ownership check

**File:** `stilum-backend/src/main/java/com/stilum/citas/presentation/profesional/MiPortalController.java`, lines 34, 44

```java
public List<CitaResponse> misCitas(@RequestParam UUID profesionalId, ...)
public MisIngresosResponse misIngresos(@RequestParam UUID profesionalId, ...)
```

The controller is secured with `@PreAuthorize("hasAnyRole('PROFESIONAL', 'ADMIN_TENANT')")`, but a `PROFESIONAL` user can pass any `profesionalId` and retrieve another professional's schedule and income data. The controller should verify that the authenticated user's linked `profesionalId` matches the requested one (or that the user has `ADMIN_TENANT`).

**Fix:** Resolve the authenticated user's `profesionalId` from the `Principal`/JWT and compare, returning 403 if they differ (unless the caller has `ADMIN_TENANT`).

---

### 5. Naive JSON construction with string concatenation in `WhatsAppBotService` (injection risk)

**File:** `stilum-backend/src/main/java/com/stilum/citas/application/whatsapp/WhatsAppBotService.java`, line 85

```java
conv.avanzarEstado("ESPERANDO_FECHA",
    "{\"necesidad\":\"" + necesidad.replace("\"", "'") + "\"}");
```

Replacing `"` with `'` is not sufficient JSON escaping. A user input containing backslashes, newlines, or control characters can produce invalid JSON or corrupt the `datos_json` field. This data comes directly from an untrusted WhatsApp message.

**Fix:** Use `ObjectMapper` (Jackson) to serialize a proper `Map<String, String>` into the `datos_json` string.

---

### 6. `claudeModel` hardcodes a non-existent model ID

**File:** `stilum-backend/src/main/java/com/stilum/citas/application/whatsapp/WhatsAppBotService.java`, line 36

```java
@Value("${stilum.claude.model:claude-haiku-4-5-20251001}")
private String claudeModel;
```

As of the current Claude API, there is no model named `claude-haiku-4-5-20251001`. The correct model ID for the Haiku 4.5 generation is `claude-haiku-4-5`. The hardcoded default will cause every AI recommendation call to fail with a 404 from the Anthropic API, silently falling back to the static string (line 147). This means the AI feature ships broken unless the property is overridden in `application.properties`.

**Fix:** Change the default to `claude-haiku-4-5` (or confirm the actual model ID being deployed).

---

### 7. `PATCH /api/citas/{id}/pagar` allows `PROFESIONAL` role to register payment — spec said `ADMIN_TENANT` only

**File:** `stilum-backend/src/main/java/com/stilum/citas/presentation/cita/CitaController.java`, line 108

```java
@PreAuthorize("hasAnyRole('ADMIN_TENANT', 'PROFESIONAL', 'SUPER_ADMIN')")
public CitaResponse registrarPago(...)
```

The spec for Cobro + Comisiones describes payment registration as an admin-only operation (the admin closes the payment, calculates the commission). Allowing `PROFESIONAL` to call this endpoint means a professional could register a lower `precioCobrado` than actual (which also reduces their own commission — but could also understate revenue in reports). Verify the intended access policy with the product owner.

---

## Minor Notes

1. **V12 missing index on `citas.metodo_pago`**: The `ContabilidadService` groups by `metodo_pago` in Java after a full table scan filtered by date. If the `citas` table grows large, a partial index `CREATE INDEX idx_citas_metodo_pago ON citas (tenant_id, metodo_pago) WHERE metodo_pago IS NOT NULL` would help. Low priority for MVP.

2. **`ConversacionWhatsApp.createdAt` uses `Instant.now()` at field initialization, not `@CreationTimestamp`**: `Cita` correctly uses `@CreationTimestamp`, but `ConversacionWhatsApp` sets `createdAt = Instant.now()` inline (line 47). This is functionally equivalent but inconsistent with the pattern used across other entities. Consider replacing with `@CreationTimestamp` for consistency.

3. **`WhatsAppBotService` is `@Transactional` at class level**: The service makes a blocking HTTP call to the Claude API (`webClient...block()`) inside a transaction. This holds the DB connection open for the duration of the HTTP round-trip. Move the Claude API call outside the transaction boundary (e.g., fetch data in a read transaction, call Claude, then open a write transaction for `avanzarEstado`).

4. **`WhatsAppWebhookController` has an unused import dependency `WhatsAppMensajeEntranteDto`**: The DTO exists in `application/whatsapp/dto/` but the controller uses raw `Map<String, Object>`. This is not a bug but leaves dead code.

5. **`@PreAuthorize("hasRole('ADMIN_TENANT')")` on `PATCH /api/profesionales/{id}/comision`** uses `hasRole` (singular) while the class-level annotation uses `hasAnyRole('ADMIN_TENANT', 'SUPER_ADMIN')`. This inadvertently blocks `SUPER_ADMIN` from updating commissions. Should be `hasAnyRole('ADMIN_TENANT', 'SUPER_ADMIN')` for consistency.

6. **V13 has no `updated_at` trigger**: The column exists and `@UpdateTimestamp` in the entity keeps it current via Hibernate, but if rows are ever updated outside JPA (e.g., direct SQL migrations, admin scripts), the trigger would be missing. Low priority.

---

## DB Constraints Checklist

| Constraint | V12 | V13 |
|-----------|-----|-----|
| `tenant_id UUID NOT NULL REFERENCES tenants(id)` | N/A (ALTER on existing tables) | ✅ |
| Index on `tenant_id` | N/A | ✅ (`idx_conv_tenant_telefono`) |
| `UNIQUE (tenant_id, telefono)` | N/A | ✅ |
| `@Filter` on `@Entity` | ✅ `Cita`, ✅ `Profesional` | ❌ `ConversacionWhatsApp` missing |

---

## Overall Verdict

**NEEDS_FIXES**

Three critical issues block approval:

1. Missing `@Filter` on `ConversacionWhatsApp` — violates the mandatory multi-tenancy contract.
2. `TenantContext.set()` without cleanup in service layer — thread-local leak in production thread pool.
3. No HMAC signature validation on the public webhook — security vulnerability that enables cost amplification and state injection attacks.

The core booking, commission, and accounting logic is correct and well-structured. Issues 1 and 2 are quick fixes (a few lines each). Issue 3 requires adding HMAC verification middleware but does not require architectural changes. After resolving the three critical items, the branch can be approved.

---

## Fix Application Report

**Date:** 2026-06-22  
**Commit:** `65e0445`  
**Status:** DONE

### Fixes Applied

| # | Description | File | Result |
|---|-------------|------|--------|
| 1 | Added `@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")` annotation and `import org.hibernate.annotations.Filter` to `ConversacionWhatsApp` | `domain/whatsapp/ConversacionWhatsApp.java` | Applied |
| 2 | Removed `TenantContext.set(tenantId)` call and its import from `procesarMensaje()` in `WhatsAppBotService` | `application/whatsapp/WhatsAppBotService.java` | Applied |
| 3 | Replaced naive `necesidad.replace("\"", "'")` with `replaceAll("[\"\\\\\\n\\r\\t]", " ")` sanitizer for safe JSON construction | `application/whatsapp/WhatsAppBotService.java` | Applied |
| 4 | Added `verificarAcceso(UUID, Authentication)` helper method to `MiPortalController` and wired `Authentication auth` parameter + guard call into both `misCitas` and `misIngresos` endpoints | `presentation/profesional/MiPortalController.java` | Applied |

### Not Fixed in This Pass

- **Issue 3 (Critical):** HMAC signature validation on the public webhook — requires a larger change (raw body interception, new secret property, HMAC-SHA256 comparison). Tracked as a follow-up task.
- **Issue 6:** `claudeModel` default value `claude-haiku-4-5-20251001` — should be confirmed/corrected against actual deployed model ID.
- **Issue 7:** `PATCH /api/citas/{id}/pagar` role policy — needs product owner confirmation before changing.
- Minor notes 1–6 — deferred as low-priority.

