# Skills de Desarrollo — Mapa de Uso

**Versión:** 1.0  
**Fecha:** 2026-06-07  
**Propósito:** Cada skill tiene una regla que hace cumplir y una fase donde se introduce. No es opcional usarlas — son parte del contrato de calidad del proyecto.

---

## Cómo leer este documento

Cada skill tiene tres datos clave:

| Campo | Qué significa |
|-------|--------------|
| **Regla** | El código de regla en `REGLAS_NEGOCIO.md` o `METODOLOGIA.md` que esta skill hace cumplir |
| **Se aplica desde** | La fase del roadmap donde se introduce por primera vez |
| **Verificación** | Cómo se comprueba que se está usando correctamente |

---

## 1. Skills de Backend — Java 21 + Spring Boot 3

### SK-B-01 — Clean Architecture (capas)

**Regla:** `METODOLOGIA.md § 2` — dependencias solo apuntan hacia adentro  
**Se aplica desde:** Fase 1, Semana 1 (setup del proyecto)  
**Verificación:** ningún `import` de `infrastructure` dentro de `domain`; ningún `import` de `presentation` dentro de `application`

```
Checklist al crear cualquier clase nueva:
[ ] ¿En qué capa vive? (domain / application / infrastructure / presentation)
[ ] ¿Sus imports respetan la dirección de dependencia?
[ ] ¿Tiene lógica de negocio en el controller? → moverla al Use Case
[ ] ¿El Use Case llama a la DB directamente? → hacerlo a través del Repository interface
```

---

### SK-B-02 — Records de Java 21 para DTOs

**Regla:** `METODOLOGIA.md § 3.3` — DTOs inmutables sin boilerplate  
**Se aplica desde:** Fase 1, Semana 2 (primeros Use Cases)  
**Verificación:** ningún DTO usa clase con getters/setters; todos son `record`

```java
// Checklist al crear un DTO:
[ ] ¿Es un record? (no una clase con @Getter @Setter)
[ ] ¿Los campos de entrada tienen anotaciones @NotNull / @Valid?
[ ] ¿Tiene un método estático from(Entity) si es de salida?
[ ] ¿Está en el subpaquete dto/ de su use case?
```

---

### SK-B-03 — Lombok solo en entidades JPA

**Regla:** `METODOLOGIA.md § 3.1` — reducir boilerplate solo donde los records no aplican  
**Se aplica desde:** Fase 1, Semana 2 (primeras entidades)  
**Verificación:** Lombok no aparece en DTOs, Use Cases ni interfaces

```java
// Uso correcto de Lombok:
@Entity @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Cita { ... }

// Incorrecto — un DTO no necesita Lombok:
@Data  // ❌ — usar record
public class AgendarCitaRequest { ... }
```

---

### SK-B-04 — Specification Pattern para reglas de negocio

**Regla:** `RN-CITA-001`, `RN-PROF-002`, `RN-CITA-005`, `RN-PROF-003`  
**Se aplica desde:** Fase 1, Semana 2 (primeros Use Cases de citas)  
**Verificación:** ningún `if` de negocio repetido en dos Use Cases distintos

```
Checklist al escribir una condición de negocio:
[ ] ¿Esta condición ya existe en otra clase? → extraerla a una Specification
[ ] ¿La Specification se puede componer con .y() / .o()?
[ ] ¿Lanza una excepción de dominio tipada al fallar?
[ ] ¿Tiene test unitario propio?
```

---

### SK-B-05 — Métodos de dominio en entidades

**Regla:** `RN-CITA-002` (transiciones de estado), `RN-PROF-001`, `RN-SERV-002`  
**Se aplica desde:** Fase 1, Semana 2  
**Verificación:** los cambios de estado de `Cita` solo ocurren a través de `cita.cambiarEstado()`, nunca con `cita.setEstado()`

```
Checklist al cambiar el estado de una entidad:
[ ] ¿El cambio pasa por un método de dominio de la entidad?
[ ] ¿El método valida la transición antes de aplicarla?
[ ] ¿Lanza excepción tipada si la transición es inválida?
[ ] ¿El setter está en private o protegido?
```

---

### SK-B-06 — Custom Annotations para validación de DTOs

**Regla:** `RN-CITA-003` (fecha futura), `RN-SERV-001` (duración mínima)  
**Se aplica desde:** Fase 1, Semana 2  
**Verificación:** no hay validaciones de formato duplicadas en múltiples Use Cases

```
Checklist al validar un campo de entrada:
[ ] ¿La validación es reutilizable? → crear @CustomAnnotation
[ ] ¿Está anotado en el record DTO, no en el Use Case?
[ ] ¿El controller tiene @Valid en el @RequestBody?
[ ] ¿GlobalExceptionHandler captura MethodArgumentNotValidException?
```

---

### SK-B-07 — Constantes de dominio (sin magic numbers)

**Regla:** `RN-SERV-001`, `RN-TENANT-002`, `METODOLOGIA.md § 3.7`  
**Se aplica desde:** Fase 1, Semana 2  
**Verificación:** ningún número literal con significado de negocio suelto en el código

```java
// Checklist al escribir un número en el código:
[ ] ¿Tiene significado de negocio? → a ReglasServicio / ReglasSubscripcion / etc.
[ ] ¿La clase de constantes es final con constructor private?
[ ] ¿El nombre de la constante describe QUÉ es, no el valor?
//  DURACION_MINIMA_MINUTOS = 15  ✅
//  MIN = 15                       ❌
```

---

### SK-B-08 — MapStruct para mapeo Entity ↔ DTO

**Regla:** `METODOLOGIA.md § 3.4` — sin código de mapeo manual repetido  
**Se aplica desde:** Fase 1, Semana 2  
**Verificación:** ningún método de conversión escrito a mano cuando existe un mapper equivalente

```
Checklist al mapear entre capas:
[ ] ¿Existe el mapper en infrastructure/persistence/mapper/?
[ ] ¿El mapper es una @Mapper interface de MapStruct (no una clase manual)?
[ ] ¿El Use Case usa el mapper, no el controller?
```

---

### SK-B-09 — Flyway para migraciones de base de datos

**Regla:** `METODOLOGIA.md § 3.5` — schema versionado, nunca ddl-auto=update  
**Se aplica desde:** Fase 1, Semana 1 (primer día del proyecto)  
**Verificación:** `spring.jpa.hibernate.ddl-auto=validate` en todos los entornos; el schema solo cambia con nuevos archivos Flyway

```
Checklist al modificar el schema:
[ ] ¿Se creó un nuevo archivo V{N}__descripcion.sql?
[ ] ¿El nombre describe el cambio, no la fecha?
[ ] ¿El archivo existente NO fue editado?
[ ] ¿Se probó la migración en local antes de commitear?
```

---

### SK-B-10 — GlobalExceptionHandler (un solo punto de errores HTTP)

**Regla:** `REGLAS_NEGOCIO.md § 8` — jerarquía de excepciones de dominio  
**Se aplica desde:** Fase 1, Semana 1 (con el setup inicial)  
**Verificación:** ningún controller tiene try/catch propio; todos los errores HTTP pasan por `GlobalExceptionHandler`

```
Checklist al manejar un error:
[ ] ¿La excepción extiende ReglaNegocioException?
[ ] ¿Tiene un código string (ej: "CITA_NO_DISPONIBLE")?
[ ] ¿GlobalExceptionHandler la captura con el HTTP status correcto?
[ ] ¿El controller no tiene ningún try/catch?
```

---

### SK-B-11 — OpenAPI / Swagger (API-First)

**Regla:** `METODOLOGIA.md § 9` — spec antes de implementar  
**Se aplica desde:** Fase 1, Semana 1  
**Verificación:** ningún endpoint existe sin su entrada en `API_SPEC.yaml`

```
Checklist antes de implementar un endpoint:
[ ] ¿Está documentado en API_SPEC.yaml con request y responses?
[ ] ¿El controller tiene @Operation y @ApiResponse de SpringDoc?
[ ] ¿El Swagger UI lo muestra correctamente en local?
```

---

### SK-B-12 — Testcontainers para tests de integración

**Regla:** `METODOLOGIA.md § 6` — tests de repositorio contra PostgreSQL real  
**Se aplica desde:** Fase 1, Semana 2 (primeros repositorios)  
**Verificación:** los tests de repositorio no usan H2; levantan un contenedor PostgreSQL real

```
Checklist al escribir un test de repositorio:
[ ] ¿Usa @Testcontainers con @Container PostgreSQLContainer?
[ ] ¿No hay @Profile("test") con H2 en las propiedades?
[ ] ¿El test verifica el comportamiento del @Filter de tenant?
```

---

## 2. Skills de Frontend — Angular 21 + PrimeNG 21

### SK-F-01 — Signals para estado reactivo

**Regla:** `METODOLOGIA.md § 5.1` — reemplaza BehaviorSubject en estado local  
**Se aplica desde:** Fase 2, Semana 4 (primer componente Angular)  
**Verificación:** ningún `BehaviorSubject` o `Subject` en componentes; solo en servicios que manejan estado global compartido

```typescript
// Checklist al manejar estado en un componente:
[ ] ¿Usa signal() para estado mutable local?
[ ] ¿Usa computed() para valores derivados?
[ ] ¿Usa effect() solo para side effects (no para derivar estado)?
[ ] ¿El template usa la sintaxis @let o () para leer signals?
```

---

### SK-F-02 — Smart / Dumb components + OnPush

**Regla:** `METODOLOGIA.md § 5.2` — separación de responsabilidades en UI  
**Se aplica desde:** Fase 2, Semana 4  
**Verificación:** los componentes dumb no inyectan servicios; tienen `changeDetection: ChangeDetectionStrategy.OnPush`

```
Checklist al crear un componente nuevo:
[ ] ¿Es Smart (contiene lógica, inyecta servicios) o Dumb (solo presenta)?
[ ] Si es Dumb: ¿tiene ChangeDetectionStrategy.OnPush?
[ ] Si es Dumb: ¿recibe datos solo por input() y emite por output()?
[ ] Si es Dumb: ¿NO inyecta ningún servicio?
[ ] Si es Smart: ¿delega la presentación a componentes Dumb hijos?
```

---

### SK-F-03 — Lazy loading por feature

**Regla:** `METODOLOGIA.md § 5.6` — carga diferida de módulos  
**Se aplica desde:** Fase 2, Semana 4 (definición de rutas)  
**Verificación:** ninguna feature importada directamente en `app.module` o `app.routes`; todas van por `loadChildren` o `loadComponent`

```typescript
// Checklist al agregar una ruta nueva:
[ ] ¿Usa loadChildren() o loadComponent() (no import directo)?
[ ] ¿La feature tiene su propio archivo .routes.ts?
[ ] ¿El bundle del build muestra un chunk separado para esa feature?
```

---

### SK-F-04 — Interceptores funcionales (sin clases)

**Regla:** `METODOLOGIA.md § 5.4`  
**Se aplica desde:** Fase 2, Semana 4  
**Verificación:** no hay clases que implementen `HttpInterceptor`; todos son funciones `HttpInterceptorFn`

```typescript
// ✅ Correcto — función interceptora
export const jwtInterceptor: HttpInterceptorFn = (req, next) => { ... }

// ❌ Incorrecto — clase (patrón Angular anterior a v15)
@Injectable()
export class JwtInterceptor implements HttpInterceptor { ... }
```

---

### SK-F-05 — Design Tokens — sin valores hardcodeados

**Regla:** `RN-DESIGN-001` a `RN-DESIGN-006`  
**Se aplica desde:** Fase 2, Semana 4 (primer archivo SCSS del proyecto)  
**Verificación:** Stylelint + ESLint bloquean el commit si hay hex o font-family directo

```
Checklist al escribir SCSS:
[ ] ¿Cada color usa var(--color-...)?
[ ] ¿Cada font-family usa var(--font-...)?
[ ] ¿Cada font-size usa var(--text-...)?
[ ] ¿Cada padding/margin/gap usa var(--space-...)?
[ ] ¿Cada transition usa var(--duration-...) y var(--easing-...)?
[ ] ¿Pasó Stylelint sin errores?
```

---

### SK-F-06 — ThemeService + data-theme

**Regla:** `DISENO.md § 7` — temas claro/oscuro sin duplicar CSS  
**Se aplica desde:** Fase 2, Semana 4 (setup inicial de estilos)  
**Verificación:** cambiar el tema no rompe ningún componente; todos leen de tokens semánticos

```
Checklist al crear el proyecto Angular:
[ ] ¿_colors.scss tiene solo los valores base?
[ ] ¿_themes.scss define [data-theme="light"] y [data-theme="dark"]?
[ ] ¿ThemeService aplica el atributo en document.documentElement?
[ ] ¿Persiste en localStorage y detecta prefers-color-scheme?
[ ] ¿El link de PrimeNG tiene id="primeng-theme" para swapear en runtime?
```

---

### SK-F-07 — FullCalendar con ResourceTimeGrid

**Regla:** `ARQUITECTURA.md § 6` — calendario tipo salón por columnas de profesional  
**Se aplica desde:** Fase 2, Semana 7 (dashboard)  
**Verificación:** los bloques de cita reflejan la duración real del servicio; el color viene de `DesignTokens`, no de strings hardcodeados

```typescript
// Checklist al configurar el calendario:
[ ] ¿Los recursos (resources) son los profesionales activos del día?
[ ] ¿Los eventos (events) calculan end a partir de horaInicio + duracionMinutos?
[ ] ¿backgroundColor y borderColor leen de DesignTokens.cita[estado]?
[ ] ¿eventDrop llama al Use Case de reprogramar (no modifica estado local)?
[ ] ¿nowIndicator está en true?
```

---

### SK-F-08 — WebSocket con reconexión automática

**Regla:** `ARQUITECTURA.md § 15` — actualizaciones en tiempo real  
**Se aplica desde:** Fase 2, Semana 9 (pantalla de turnos y dashboard en tiempo real)  
**Verificación:** si el backend se reinicia, el frontend reconecta automáticamente sin intervención del usuario

```typescript
// Checklist al implementar WebSocket:
[ ] ¿Usa retryWhen + delay(3000) para reconexión automática?
[ ] ¿El Subject se destruye en ngOnDestroy?
[ ] ¿El token del display va en la URL, no en headers?
[ ] ¿El dashboard y el display comparten el mismo canal de eventos?
```

---

### SK-F-09 — Pipes para lógica de presentación

**Regla:** `REGLAS_NEGOCIO.md § 9` — sin lógica de estado en templates  
**Se aplica desde:** Fase 2, Semana 5 (primeras tablas de citas)  
**Verificación:** ningún `*ngIf="estado === 'PENDIENTE'"` en templates; todo pasa por pipes o `CitaRules`

```
Checklist al mostrar datos de estado en un template:
[ ] ¿El label/color/ícono del estado usa el pipe estadoCita?
[ ] ¿Los botones deshabilitados usan CitaRules.puedeCancelar() etc.?
[ ] ¿El pipe tiene pure: true?
[ ] ¿El pipe está en shared/pipes/ y no dentro de una feature?
```

---

### SK-F-10 — Guards de ruta tipados

**Regla:** `METODOLOGIA.md § 5.5` — protección de rutas por rol  
**Se aplica desde:** Fase 2, Semana 4 (definición de rutas)  
**Verificación:** ninguna ruta de tenant es accesible sin JWT válido; ninguna ruta de super admin sin rol SUPER_ADMIN

```
Checklist al definir una ruta protegida:
[ ] ¿Tiene canActivate: [authGuard]?
[ ] Si es de Super Admin: ¿tiene además canActivate: [superAdminGuard]?
[ ] ¿El guard redirige a /login si no hay token, no lanza error?
[ ] ¿El guard es una función (CanActivateFn), no una clase?
```

---

## 3. Skills de Calidad y Proceso

### SK-Q-01 — Conventional Commits

**Regla:** `METODOLOGIA.md § 7`  
**Se aplica desde:** Fase 1, Semana 1 — primer commit  
**Verificación:** el historial de git no tiene mensajes como "fix", "cambios", "wip"

```
Checklist antes de hacer commit:
[ ] ¿El mensaje sigue el formato tipo(scope): descripción?
[ ] ¿El tipo es uno de: feat, fix, refactor, test, docs, chore, perf?
[ ] ¿El scope nombra el módulo o feature afectado?
[ ] ¿La descripción está en minúsculas y no termina en punto?
```

---

### SK-Q-02 — Husky + lint-staged (pre-commit)

**Regla:** `RN-DESIGN-001` a `RN-DESIGN-006`, `METODOLOGIA.md § 8`  
**Se aplica desde:** Fase 1, Semana 1 — antes de cualquier commit  
**Verificación:** un commit con un hex hardcodeado es rechazado automáticamente

```bash
# Verificar que Husky está activo:
cat .husky/pre-commit
# Debe mostrar: npx lint-staged

# Verificar lint-staged:
cat package.json | grep lint-staged -A 6
```

---

### SK-Q-03 — OpenAPI-First (spec antes de código)

**Regla:** `METODOLOGIA.md § 9`  
**Se aplica desde:** Fase 1, Semana 1  
**Verificación:** cada endpoint tiene su entrada en `API_SPEC.yaml` antes de que exista el controller

```
Checklist antes de implementar un endpoint:
[ ] ¿Está en API_SPEC.yaml con request body y todas las responses?
[ ] ¿Los schemas de request/response tienen ejemplos?
[ ] ¿Se generó el cliente TypeScript desde la spec?
[ ] ¿El frontend usa el cliente generado, no fetch directo?
```

---

### SK-Q-04 — Tests antes de marcar una tarea como completada

**Regla:** `METODOLOGIA.md § 6` — cobertura mínima 70% en use cases  
**Se aplica desde:** Fase 1, Semana 2 — con el primer Use Case  
**Verificación:** JaCoCo reporta ≥ 70% en el paquete `application/`

```
Checklist al terminar un Use Case:
[ ] ¿Tiene test de caso exitoso?
[ ] ¿Tiene test de cada excepción que puede lanzar?
[ ] ¿El test usa Mockito para aislar dependencias?
[ ] ¿JaCoCo no baja del 70% tras el commit?
```

---

## 4. Mapa Skills → Fases del Roadmap

### Fase 1 — Cimientos Backend (semanas 1-3)

| Semana | Tarea | Skills obligatorias |
|--------|-------|-------------------|
| 1 | Setup Spring Boot | SK-B-01, SK-B-09, SK-B-10, SK-B-11, SK-Q-01, SK-Q-02, SK-Q-03 |
| 2 | Dominio + Use Cases | SK-B-02, SK-B-03, SK-B-04, SK-B-05, SK-B-06, SK-B-07, SK-B-08, SK-B-12 |
| 3 | APIs REST | SK-B-10, SK-B-11, SK-Q-04 |

### Fase 2 — Frontend Web (semanas 4-9)

| Semana | Tarea | Skills obligatorias |
|--------|-------|-------------------|
| 4 | Setup Angular | SK-F-01, SK-F-03, SK-F-04, SK-F-05, SK-F-06, SK-F-10, SK-Q-01, SK-Q-02 |
| 5 | Super Admin Portal | SK-F-02, SK-F-09 |
| 6 | Profesionales y Servicios | SK-F-02, SK-F-05, SK-F-09 |
| 7 | Dashboard Calendario | SK-F-07, SK-F-05 |
| 8 | Citas y Clientes | SK-F-02, SK-F-09 |
| 9 | Turnos + QR | SK-F-08, SK-F-05 |

### Fase 3 — Reportes y Notificaciones (semanas 10-11)

| Semana | Tarea | Skills obligatorias |
|--------|-------|-------------------|
| 10 | Reportes | SK-F-05, SK-F-09 |
| 11 | Scheduler + Email | SK-B-04 (Specification), SK-B-05 (métodos dominio) |

### Fase 4 — Agente WhatsApp (semanas 12-15)

| Semana | Tarea | Skills obligatorias |
|--------|-------|-------------------|
| 12-15 | Agente | SK-B-01 (Clean Arch), SK-B-04 (Specification), SK-B-05, SK-Q-03, SK-Q-04 |

---

## 5. Resumen: todas las skills del proyecto

| Código | Skill | Tecnología | Regla principal | Fase |
|--------|-------|-----------|----------------|------|
| SK-B-01 | Clean Architecture | Java | METODOLOGIA § 2 | F1-S1 |
| SK-B-02 | Records para DTOs | Java 21 | METODOLOGIA § 3.3 | F1-S2 |
| SK-B-03 | Lombok solo en entidades | Java | METODOLOGIA § 3.1 | F1-S2 |
| SK-B-04 | Specification Pattern | Java | RN-CITA-001, RN-PROF-002 | F1-S2 |
| SK-B-05 | Métodos de dominio | Java | RN-CITA-002 | F1-S2 |
| SK-B-06 | Custom Annotations | Java | RN-CITA-003 | F1-S2 |
| SK-B-07 | Constantes de dominio | Java | RN-SERV-001 | F1-S2 |
| SK-B-08 | MapStruct | Java | METODOLOGIA § 3.4 | F1-S2 |
| SK-B-09 | Flyway | SQL | METODOLOGIA § 3.5 | F1-S1 |
| SK-B-10 | GlobalExceptionHandler | Java | RN § 8 | F1-S1 |
| SK-B-11 | OpenAPI / Swagger | Java | METODOLOGIA § 9 | F1-S1 |
| SK-B-12 | Testcontainers | Java | METODOLOGIA § 6 | F1-S2 |
| SK-F-01 | Signals | Angular 21 | METODOLOGIA § 5.1 | F2-S4 |
| SK-F-02 | Smart/Dumb + OnPush | Angular | METODOLOGIA § 5.2 | F2-S4 |
| SK-F-03 | Lazy loading | Angular | METODOLOGIA § 5.6 | F2-S4 |
| SK-F-04 | Interceptores funcionales | Angular | METODOLOGIA § 5.4 | F2-S4 |
| SK-F-05 | Design Tokens (sin hardcode) | SCSS | RN-DESIGN-001..006 | F2-S4 |
| SK-F-06 | ThemeService | Angular + SCSS | DISENO § 7 | F2-S4 |
| SK-F-07 | FullCalendar ResourceGrid | Angular + PrimeNG | ARQUITECTURA § 6 | F2-S7 |
| SK-F-08 | WebSocket + reconexión | Angular | ARQUITECTURA § 15 | F2-S9 |
| SK-F-09 | Pipes de presentación | Angular | RN § 9 | F2-S5 |
| SK-F-10 | Guards funcionales | Angular | METODOLOGIA § 5.5 | F2-S4 |
| SK-Q-01 | Conventional Commits | Git | METODOLOGIA § 7 | F1-S1 |
| SK-Q-02 | Husky + lint-staged | Node | RN-DESIGN-001 | F1-S1 |
| SK-Q-03 | OpenAPI-First | Proceso | METODOLOGIA § 9 | F1-S1 |
| SK-Q-04 | Tests por Use Case | JUnit5 | METODOLOGIA § 6 | F1-S2 |

---

*Documento de Skills — Sistema de Citas SaaS v1.0*
