# Reglas de Negocio — Sistema de Citas SaaS

**Versión:** 1.1  
**Fecha:** 2026-06-07  
**Principio:** Cada regla se define UNA sola vez en el dominio y se reutiliza en toda la aplicación.

---

## Cómo se implementan las reglas (sin repetición)

Las reglas de negocio viven en **una sola capa: el dominio**. Nunca se repite un `if` de negocio en un controller, service o componente Angular. Hay tres mecanismos según la complejidad de la regla:

| Mecanismo | Cuándo usarlo | Dónde vive |
|-----------|--------------|-----------|
| **Método de dominio** en la entidad | Regla sobre el estado de un objeto | `domain/model/` |
| **Specification** | Regla de elegibilidad compleja (múltiples condiciones) | `domain/specification/` |
| **Anotación de validación** | Regla de formato/valor en DTOs de entrada | `domain/validation/` |
| **Constante de dominio** | Valor numérico con significado de negocio | `domain/constant/` |
| **Guard Angular** (frontend) | Regla de acceso/navegación en UI | `core/guards/` |
| **Design Token** (frontend) | Color, tipografía, espaciado, animación | `styles/tokens/` |

---

## 1. Reglas de Citas

### RN-CITA-001 — Solapamiento de horarios
> Una cita no puede ocupar un horario que ya está ocupado por otro del mismo profesional.

```java
// domain/specification/CitaDisponibleSpec.java
public class CitaDisponibleSpec implements Specification<Cita> {

    private final UUID profesionalId;
    private final LocalDate fecha;
    private final LocalTime horaInicio;
    private final LocalTime horaFin;

    @Override
    public boolean esSatisfecha(List<Cita> citasExistentes) {
        return citasExistentes.stream()
            .filter(c -> c.getProfesionalId().equals(profesionalId))
            .filter(c -> c.getFecha().equals(fecha))
            .filter(c -> c.getEstado().esActiva())
            .noneMatch(c -> seSolapan(c.getHoraInicio(), c.getHoraFin(), horaInicio, horaFin));
    }

    private boolean seSolapan(LocalTime a1, LocalTime a2, LocalTime b1, LocalTime b2) {
        return a1.isBefore(b2) && b1.isBefore(a2);
    }
}

// Uso en AgendarCitaUseCase — una sola línea, sin repetición:
citaDisponibleSpec.verificar(citasDelDia); // lanza CitaNoDisponibleException si falla
```

### RN-CITA-002 — Estados válidos para transición
> Una cita solo puede cambiar de estado según la siguiente tabla. Cualquier otra transición es inválida.

```
PENDIENTE   → CONFIRMADA, CANCELADA, NO_ASISTIO
CONFIRMADA  → EN_ATENCION, CANCELADA, REPROGRAMADA, NO_ASISTIO
EN_ATENCION → COMPLETADA
COMPLETADA  → (sin transición — estado terminal)
CANCELADA   → (sin transición — estado terminal)
REPROGRAMADA→ (sin transición — estado terminal)
NO_ASISTIO  → (sin transición — estado terminal)
```

```java
// domain/model/EstadoCita.java
public enum EstadoCita {
    PENDIENTE, CONFIRMADA, EN_ATENCION, COMPLETADA, CANCELADA, REPROGRAMADA, NO_ASISTIO;

    private static final Map<EstadoCita, Set<EstadoCita>> TRANSICIONES_VALIDAS = Map.of(
        PENDIENTE,    Set.of(CONFIRMADA, CANCELADA, NO_ASISTIO),
        CONFIRMADA,   Set.of(EN_ATENCION, CANCELADA, REPROGRAMADA, NO_ASISTIO),
        EN_ATENCION,  Set.of(COMPLETADA)
    );

    public void validarTransicionA(EstadoCita destino) {
        Set<EstadoCita> validos = TRANSICIONES_VALIDAS.getOrDefault(this, Set.of());
        if (!validos.contains(destino)) {
            throw new TransicionEstadoInvalidaException(
                "No se puede pasar de %s a %s".formatted(this, destino)
            );
        }
    }

    public boolean esActiva() {
        return this == PENDIENTE || this == CONFIRMADA || this == EN_ATENCION;
    }

    public boolean esTerminal() {
        return this == COMPLETADA || this == CANCELADA || this == REPROGRAMADA || this == NO_ASISTIO;
    }
}

// Uso en la entidad Cita — método que encapsula la regla:
public void cambiarEstado(EstadoCita nuevo) {
    this.estado.validarTransicionA(nuevo);  // ← regla aplicada aquí, una sola vez
    this.estado = nuevo;
}
```

### RN-CITA-003 — No agendar en el pasado
> La fecha de una cita debe ser hoy o en el futuro.

```java
// domain/validation/FechaFuturaValidator.java
@Constraint(validatedBy = FechaFuturaValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FechaFutura {
    String message() default "La fecha de la cita debe ser hoy o en el futuro";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

public class FechaFuturaValidator implements ConstraintValidator<FechaFutura, LocalDate> {
    @Override
    public boolean isValid(LocalDate fecha, ConstraintValidatorContext ctx) {
        return fecha != null && !fecha.isBefore(LocalDate.now());
    }
}

// Uso en el DTO de entrada — se valida automáticamente con @Valid:
public record AgendarCitaRequest(
    @NotNull UUID profesionalId,
    @NotNull UUID servicioId,
    @NotNull @FechaFutura LocalDate fecha,    // ← regla aplicada al DTO
    @NotNull LocalTime hora
) {}
```

### RN-CITA-004 — QR válido solo el día de la cita
> El token QR expira a medianoche del día de la cita.

```java
// domain/model/QRToken.java  (Value Object)
public record QRToken(String valor, LocalDate fechaCita) {

    public void validar() {
        if (LocalDate.now().isAfter(fechaCita)) {
            throw new QRExpiradoException("El código QR expiró. La cita era el " + fechaCita);
        }
    }
}

// Uso en ConfirmarCheckInUseCase:
cita.getQrToken().validar();  // ← una línea, regla en el Value Object
```

### RN-CITA-005 — Límite de citas por plan
> Un tenant no puede superar el máximo de citas mensuales de su plan.

```java
// domain/specification/LimiteCitasPlanSpec.java
public class LimiteCitasPlanSpec implements Specification<Void> {

    private final Plan plan;
    private final long citasDelMes;

    @Override
    public boolean esSatisfecha(Void v) {
        return plan.getMaxCitasMes() == -1   // -1 = ilimitado
            || citasDelMes < plan.getMaxCitasMes();
    }
}
```

---

## 2. Reglas de Profesionales

### RN-PROF-001 — Profesional inactivo no recibe citas
> Si `activo = false`, no puede aparecer en recomendaciones del agente ni recibir nuevas citas.

```java
// domain/model/Profesional.java
public void validarDisponibleParaCitas() {
    if (!this.activo) {
        throw new ProfesionalInactivoException(
            "El profesional %s no está disponible para nuevas citas".formatted(nombre)
        );
    }
}
```

### RN-PROF-002 — Debe tener al menos un servicio asignado para aparecer en el agente

```java
// domain/specification/ProfesionalConServicioSpec.java
public class ProfesionalConServicioSpec implements Specification<Profesional> {
    private final UUID servicioId;

    @Override
    public boolean esSatisfecha(Profesional profesional) {
        return profesional.isActivo()
            && profesional.getServicios().stream()
                .anyMatch(s -> s.getId().equals(servicioId) && s.isActivo());
    }
}
```

### RN-PROF-003 — Límite de profesionales por plan

```java
// domain/specification/LimiteProfesionalesPlanSpec.java
public class LimiteProfesionalesPlanSpec implements Specification<Void> {

    private final Plan plan;
    private final long profesionalesActivos;

    @Override
    public boolean esSatisfecha(Void v) {
        return plan.getMaxProfesionales() == -1
            || profesionalesActivos < plan.getMaxProfesionales();
    }
}
```

---

## 3. Reglas de Servicios

### RN-SERV-001 — Duración mínima de un servicio

```java
// domain/constant/ReglasServicio.java
public final class ReglasServicio {
    public static final int DURACION_MINIMA_MINUTOS = 15;
    public static final int DURACION_MAXIMA_MINUTOS = 480; // 8 horas
    public static final int PRECIO_MINIMO = 0;

    private ReglasServicio() {} // no instanciable
}

// Uso en el DTO:
public record CrearServicioRequest(
    @NotBlank String nombre,
    @Min(ReglasServicio.DURACION_MINIMA_MINUTOS)
    @Max(ReglasServicio.DURACION_MAXIMA_MINUTOS) int duracionMinutos,
    @PositiveOrZero BigDecimal precio
) {}
```

### RN-SERV-002 — Servicio con citas activas no se puede eliminar, solo inactivar

```java
// domain/model/Servicio.java
public void validarEliminable(long citasActivas) {
    if (citasActivas > 0) {
        throw new ServicioConCitasActivasException(
            "El servicio tiene %d citas activas. Inactívelo en lugar de eliminarlo."
                .formatted(citasActivas)
        );
    }
}
```

### RN-SERV-003 — Servicio inactivo no aparece en el agente WhatsApp

```java
// En el Use Case del agente, al listar servicios:
// Solo se expone a Claude los servicios activos del tenant — filtro en repositorio:
List<Servicio> servicios = servicioRepository.findByTenantIdAndActivoTrue(tenantId);
```

---

## 4. Reglas de Tenants y Suscripciones

### RN-TENANT-001 — Tenant inactivo bloquea todo acceso

```java
// domain/model/Tenant.java
public void validarAccesoActivo() {
    if (!this.activo) {
        throw new TenantInactivoException(
            "La cuenta de %s está inactiva. Contacte al administrador.".formatted(nombreNegocio)
        );
    }
}
```

```java
// infrastructure/config/TenantFilter.java  — se aplica en CADA request HTTP
// Un solo punto de control para toda la aplicación:
@Component
public class TenantFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest req, ...) {
        String tenantId = jwtService.extractTenantId(req);
        if (tenantId != null) {
            Tenant tenant = tenantRepository.findById(UUID.fromString(tenantId))
                .orElseThrow(TenantNoEncontradoException::new);
            tenant.validarAccesoActivo();          // ← RN-TENANT-001 aplicada aquí
            suscripcionService.validarVigente(tenant); // ← RN-TENANT-002 aplicada aquí
            TenantContextHolder.set(tenantId);
        }
        filterChain.doFilter(req, res);
    }
}
```

### RN-TENANT-002 — Suscripción vencida: solo lectura por 7 días de gracia

```java
// domain/model/Subscription.java
public void validarVigente() {
    if (estado == EstadoSuscripcion.VENCIDA) {
        long diasGracia = ChronoUnit.DAYS.between(fechaFin, LocalDate.now());
        if (diasGracia > ReglasSubscripcion.DIAS_GRACIA) {
            throw new SuscripcionVencidaException(
                "La suscripción venció hace %d días. Renueve para continuar.".formatted(diasGracia)
            );
        }
        // dentro del período de gracia: acceso de solo lectura (sin lanzar excepción)
        SuscripcionContext.setModoSoloLectura(true);
    }
}

// domain/constant/ReglasSubscripcion.java
public final class ReglasSubscripcion {
    public static final int DIAS_GRACIA = 7;
    private ReglasSubscripcion() {}
}
```

---

## 5. Reglas de Disponibilidad

### RN-DISP-001 — La hora de cita debe estar dentro del horario del profesional

```java
// domain/specification/HorarioProfesionalSpec.java
public class HorarioProfesionalSpec implements Specification<Void> {

    private final Disponibilidad horario;
    private final LocalTime horaInicio;
    private final LocalTime horaFin;

    @Override
    public boolean esSatisfecha(Void v) {
        return !horaInicio.isBefore(horario.getHoraInicio())
            && !horaFin.isAfter(horario.getHoraFin());
    }
}
```

### RN-DISP-002 — Fecha bloqueada impide agendar

```java
// domain/model/Profesional.java
public boolean estaDisponibleEn(LocalDate fecha) {
    // Primero busca excepción específica para esa fecha
    return disponibilidades.stream()
        .filter(d -> d.esExcepcion() && d.getFecha().equals(fecha))
        .findFirst()
        .map(Disponibilidad::isDisponible)
        // si no hay excepción, usa el horario regular del día de la semana
        .orElseGet(() -> tieneHorarioRegularEn(fecha.getDayOfWeek()));
}
```

---

## 6. Reglas de Recordatorios y Notificaciones

### RN-NOTIF-001 — No reenviar recordatorio ya enviado

```java
// domain/model/Cita.java
public boolean requiereRecordatorio24h() {
    return !recordatorio24hEnviado && estado.esActiva();
}

public boolean requiereRecordatorio2h() {
    return !recordatorio2hEnviado && estado.esActiva();
}
// El scheduler llama estos métodos — sin if repetidos en el job
```

### RN-NOTIF-002 — No enviar resumen diario sin citas

```java
// domain/specification/ProfesionalConCitasHoySpec.java
public class ProfesionalConCitasHoySpec implements Specification<List<Cita>> {
    @Override
    public boolean esSatisfecha(List<Cita> citasHoy) {
        return !citasHoy.isEmpty();
    }
}
```

---

## 7. Patrón Specification — Contrato Base

Todas las specifications implementan este contrato:

```java
// domain/specification/Specification.java
@FunctionalInterface
public interface Specification<T> {

    boolean esSatisfecha(T t);

    default void verificar(T t) {
        if (!esSatisfecha(t)) {
            throw reglaViolada();
        }
    }

    default ReglaNegocioException reglaViolada() {
        return new ReglaNegocioException("Regla de negocio no satisfecha");
    }

    // Composición: AND, OR, NOT
    default Specification<T> y(Specification<T> otra) {
        return t -> this.esSatisfecha(t) && otra.esSatisfecha(t);
    }

    default Specification<T> o(Specification<T> otra) {
        return t -> this.esSatisfecha(t) || otra.esSatisfecha(t);
    }

    default Specification<T> no() {
        return t -> !this.esSatisfecha(t);
    }
}

// Uso combinado — legible como lenguaje natural:
var reglaAgendamiento = citaDisponibleSpec
    .y(horarioProfesionalSpec)
    .y(limiteCitasPlanSpec)
    .y(new ProfesionalConServicioSpec(servicioId));

reglaAgendamiento.verificar(contexto); // un solo punto, todas las reglas
```

---

## 8. Jerarquía de Excepciones de Dominio

```
ReglaNegocioException (base — HTTP 422)
├── CitaNoDisponibleException          (HTTP 409)
├── TransicionEstadoInvalidaException  (HTTP 409)
├── QRExpiradoException                (HTTP 410)
├── ProfesionalInactivoException       (HTTP 409)
├── ServicioConCitasActivasException   (HTTP 409)
├── TenantInactivoException            (HTTP 403)
├── SuscripcionVencidaException        (HTTP 402)
└── LimitePlanExcedidoException        (HTTP 402)
```

```java
// domain/exception/ReglaNegocioException.java
public class ReglaNegocioException extends RuntimeException {
    private final String codigo;  // ej: "CITA_NO_DISPONIBLE"

    public ReglaNegocioException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }
}

// presentation/advice/GlobalExceptionHandler.java
// Un solo lugar que las captura todas:
@ExceptionHandler(ReglaNegocioException.class)
public ResponseEntity<ErrorResponse> handleReglaNegocio(ReglaNegocioException ex) {
    return ResponseEntity
        .status(HttpStatus.UNPROCESSABLE_ENTITY)
        .body(new ErrorResponse(ex.getCodigo(), ex.getMessage()));
}
```

---

## 9. Reglas en el Frontend Angular

Las reglas de presentación (qué mostrar/habilitar según el estado) viven en **pipes y utilidades compartidas**, nunca inline en los templates.

```typescript
// shared/pipes/estado-cita.pipe.ts — una sola definición
@Pipe({ name: 'estadoCita', standalone: true, pure: true })
export class EstadoCitaPipe implements PipeTransform {
  transform(estado: EstadoCita): { label: string; severity: string; icon: string } {
    const mapa: Record<EstadoCita, { label: string; severity: string; icon: string }> = {
      PENDIENTE:    { label: 'Pendiente',    severity: 'warning', icon: 'pi-clock' },
      CONFIRMADA:   { label: 'Confirmada',   severity: 'info',    icon: 'pi-check' },
      EN_ATENCION:  { label: 'En atención',  severity: 'success', icon: 'pi-user' },
      COMPLETADA:   { label: 'Completada',   severity: 'success', icon: 'pi-check-circle' },
      CANCELADA:    { label: 'Cancelada',    severity: 'danger',  icon: 'pi-times' },
      REPROGRAMADA: { label: 'Reprogramada', severity: 'secondary', icon: 'pi-calendar' },
      NO_ASISTIO:   { label: 'No asistió',   severity: 'danger',  icon: 'pi-user-minus' },
    };
    return mapa[estado];
  }
}

// Uso en template — sin ifs:
@let info = cita.estado | estadoCita;
<p-tag [value]="info.label" [severity]="info.severity" [icon]="info.icon"/>
```

```typescript
// shared/utils/cita-rules.utils.ts — reglas de UI centralizadas
export const CitaRules = {
  puedeCancelar: (estado: EstadoCita) =>
    ['PENDIENTE', 'CONFIRMADA'].includes(estado),

  puedeReprogramar: (estado: EstadoCita) =>
    ['PENDIENTE', 'CONFIRMADA'].includes(estado),

  puedeVerQR: (estado: EstadoCita) =>
    ['PENDIENTE', 'CONFIRMADA'].includes(estado),

  colorCalendario: (estado: EstadoCita): string => ({
    PENDIENTE:    '#F59E0B',
    CONFIRMADA:   '#3B82F6',
    EN_ATENCION:  '#10B981',
    COMPLETADA:   '#6B7280',
    CANCELADA:    '#EF4444',
    REPROGRAMADA: '#8B5CF6',
    NO_ASISTIO:   '#EF4444',
  }[estado] ?? '#6B7280'),
} as const;

// Uso en template — sin ifs repetidos:
<p-button label="Cancelar"
  [disabled]="!CitaRules.puedeCancelar(cita.estado)"
  (onClick)="cancelar(cita)"/>

// Uso en el calendario:
events = this.citas().map(c => ({
  id: c.id,
  title: c.clienteNombre,
  backgroundColor: CitaRules.colorCalendario(c.estado),
  ...
}));
```

---

## 10. Reglas de Diseño — Tokens Obligatorios

### RN-DESIGN-001 — Ningún color hardcodeado en SCSS ni en TypeScript

> Prohibido escribir valores hexadecimales (`#C9A84C`), `rgb()`, `rgba()` o `hsl()` directamente en archivos `.scss` o `.ts`. Todo color debe ir por un token CSS o una constante TypeScript.

**Incorrecto:**
```scss
// ❌ — el día que cambies el dorado, hay que buscar en 40 archivos
.btn-primary { background: #C9A84C; }
.metric-value { color: #1C1C1E; }
```

```typescript
// ❌ — valor mágico sin nombre, sin contexto
backgroundColor: '#3B82F6'
```

**Correcto:**
```scss
// ✅ — cambiar el token cambia toda la app de una vez
.btn-primary { background: var(--color-brand-gold); }
.metric-value { color: var(--color-text-1); }
```

```typescript
// ✅ — legible, centralizado, cambiable en un solo lugar
import { DesignTokens } from '@shared/tokens/design-tokens';
backgroundColor: DesignTokens.cita.pendiente.border
```

### RN-DESIGN-002 — Ninguna fuente hardcodeada

> Prohibido escribir `font-family: 'Inter', sans-serif` o `font-family: 'Playfair Display'` en archivos de componentes. Siempre a través de variables SCSS o tokens CSS.

```scss
// ❌
.title { font-family: 'Playfair Display', serif; font-size: 22px; }

// ✅
.title { font-family: var(--font-display); font-size: var(--text-2xl); }
```

### RN-DESIGN-003 — Ningún tamaño de fuente fuera de la escala tipográfica

> Solo se usan los tamaños definidos en `_typography.scss`. No se inventa un `font-size: 17px` porque "encaja mejor".

```scss
// ❌
.label { font-size: 17px; }

// ✅ — usa el paso más cercano de la escala
.label { font-size: var(--text-sm); }   // 13px definido en la escala
```

### RN-DESIGN-004 — Ningún espaciado fuera de la escala de espaciado

> Los valores de `padding`, `margin` y `gap` usan la escala de 4px. No se inventa un `padding: 7px`.

```scss
// ❌
.card { padding: 13px 17px; }

// ✅
.card { padding: var(--space-3) var(--space-4); }  // 12px 16px
```

### RN-DESIGN-005 — Tiempos de animación siempre por token

```scss
// ❌
.nav-item { transition: all 0.15s ease; }

// ✅
.nav-item { transition: all var(--duration-fast) var(--easing-default); }
```

### RN-DESIGN-006 — Los colores de citas en el calendario nunca son strings literales

El `colorCalendario` del archivo `cita-rules.utils.ts` **debe** leer de `DesignTokens`, no de strings hardcodeados. Cuando se crea un nuevo tema, solo se actualiza `DesignTokens` y el calendario cambia solo.

```typescript
// ❌
colorCalendario: (estado: EstadoCita): string => ({
  PENDIENTE: '#3B82F6',
  ...
}[estado])

// ✅
import { DesignTokens } from '@shared/tokens/design-tokens';
colorCalendario: (estado: EstadoCita): string =>
  DesignTokens.cita[estado]?.border ?? DesignTokens.cita.default.border
```

### Cómo se hace cumplir (automatizado)

Estas reglas no dependen de la disciplina del desarrollador — el linter las hace cumplir en cada commit:

- **Stylelint** con la regla `color-no-invalid-hex` + plugin `stylelint-no-hardcoded-colors` bloquea hexadecimales en SCSS
- **ESLint** con una regla personalizada (`no-hardcoded-design-values`) alerta colores en TypeScript
- **Pre-commit hook (Husky)** ejecuta ambos linters antes de aceptar el commit

Ver configuración completa en `METODOLOGIA.md` → sección "Calidad de Código".

---

## 10. Resumen: dónde vive cada regla

| Regla | Archivo | Mecanismo |
|-------|---------|-----------|
| Solapamiento de citas | `CitaDisponibleSpec.java` | Specification |
| Transiciones de estado | `EstadoCita.java` | Enum con lógica |
| Fecha no en el pasado | `FechaFuturaValidator.java` | Custom annotation |
| QR expirado | `QRToken.java` | Value Object |
| Profesional inactivo | `Profesional.java` | Método de dominio |
| Límites del plan | `LimiteCitasPlanSpec.java` | Specification |
| Tenant inactivo | `TenantFilter.java` | Filtro HTTP global |
| Suscripción vencida | `Subscription.java` | Método de dominio |
| Duración mínima servicio | `ReglasServicio.java` | Constante de dominio |
| Recordatorio ya enviado | `Cita.java` | Método de dominio |
| Errores HTTP | `GlobalExceptionHandler.java` | Exception handler global |
| Colores de estado (UI) | `cita-rules.utils.ts` | Objeto constante compartido |
| Labels de estado (UI) | `estado-cita.pipe.ts` | Angular Pipe |
| Botones habilitados (UI) | `cita-rules.utils.ts` | Utilidad compartida |

---

*Documento de Reglas de Negocio — Sistema de Citas SaaS v1.0*
