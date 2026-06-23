# Metodología de Desarrollo — Sistema de Citas SaaS

**Versión:** 1.0  
**Fecha:** 2026-06-07  
**Aplica a:** Backend (Java/Spring Boot) · Frontend (Angular 21 + PrimeNG 21)

---

## 1. Metodología SDD — Specification-Driven Development

### ¿Qué es SDD?

SDD (Specification-Driven Development) es un enfoque donde **todo lo que se construye parte de una especificación escrita y revisada antes de escribir código**. El código es la implementación de la especificación, no al revés.

### Ciclo SDD aplicado a este proyecto

```
1. ESPECIFICAR
   Escribir la spec del módulo/feature:
   - Qué problema resuelve
   - Entidades involucradas
   - Contratos de API (OpenAPI)
   - Reglas de negocio
   - Casos de error
        │
        ▼
2. REVISAR
   Validar la spec antes de codificar:
   - ¿Los endpoints son coherentes con el dominio?
   - ¿Las reglas de negocio cubren todos los casos?
   - ¿El modelo de datos es correcto?
        │
        ▼
3. IMPLEMENTAR
   Codificar siguiendo la spec aprobada:
   - Primero el contrato (interfaces, DTOs)
   - Luego la lógica de dominio
   - Luego la infraestructura (DB, API externa)
   - Finalmente el controlador/vista
        │
        ▼
4. VERIFICAR
   Confirmar que lo implementado cumple la spec:
   - Tests unitarios sobre las reglas de negocio
   - Tests de integración sobre los endpoints
   - Revisión manual del comportamiento
        │
        ▼
5. DOCUMENTAR
   Actualizar los documentos si la spec cambió durante la implementación
```

### Artefactos SDD del proyecto

| Documento | Propósito |
|-----------|-----------|
| `ARQUITECTURA.md` | Visión del sistema, módulos, flujos, modelo de datos |
| `METODOLOGIA.md` (este archivo) | Cómo se construye, estructura, patrones, calidad |
| `API_SPEC.yaml` | Contrato OpenAPI completo (generado antes de implementar) |
| `REGLAS_NEGOCIO.md` | Casos de negocio, validaciones, restricciones |
| `MODELO_DATOS.sql` | Schema DDL versionado |

---

## 2. Clean Architecture — Backend (Spring Boot)

### Principio

El código se organiza en **capas concéntricas** donde las capas internas no conocen las externas. La lógica de negocio no sabe si está siendo consumida por un REST controller, un job, o un test.

```
┌─────────────────────────────────────────────────┐
│           INFRASTRUCTURE (capa externa)          │
│  DB (JPA/Hibernate), APIs externas, Email, QR,  │
│  WhatsApp, Spring @Scheduled, WebSocket          │
│  ┌───────────────────────────────────────────┐  │
│  │         APPLICATION (casos de uso)         │  │
│  │  Services, Use Cases, Orchestration        │  │
│  │  ┌─────────────────────────────────────┐  │  │
│  │  │     DOMAIN (núcleo del negocio)      │  │  │
│  │  │  Entities, Value Objects,            │  │  │
│  │  │  Repository Interfaces, Domain Rules  │  │  │
│  │  └─────────────────────────────────────┘  │  │
│  └───────────────────────────────────────────┘  │
└─────────────────────────────────────────────────┘
         ↑ Las dependencias apuntan hacia adentro
```

### Estructura de paquetes

```
com.peluqueria/
│
├── domain/                          # Núcleo — sin dependencias externas
│   ├── model/
│   │   ├── Cita.java               # Entidad JPA limpia
│   │   ├── Profesional.java
│   │   ├── Servicio.java
│   │   └── enums/
│   │       ├── EstadoCita.java
│   │       └── RolUsuario.java
│   ├── repository/                  # Solo interfaces (contratos)
│   │   ├── CitaRepository.java
│   │   └── ProfesionalRepository.java
│   └── exception/                   # Excepciones de dominio
│       ├── CitaNoDisponibleException.java
│       └── TenantInactivoException.java
│
├── application/                     # Casos de uso — orquesta el dominio
│   ├── cita/
│   │   ├── AgendarCitaUseCase.java
│   │   ├── CancelarCitaUseCase.java
│   │   ├── ConfirmarCheckInUseCase.java
│   │   └── dto/
│   │       ├── AgendarCitaCommand.java   # entrada
│   │       └── CitaResponse.java          # salida
│   ├── profesional/
│   │   ├── GestionarProfesionalUseCase.java
│   │   └── dto/
│   ├── notificacion/
│   │   └── EnviarRecordatorioUseCase.java
│   └── agente/
│       └── ProcesarMensajeWhatsAppUseCase.java
│
├── infrastructure/                  # Implementaciones concretas
│   ├── persistence/
│   │   ├── CitaRepositoryImpl.java  # Implementa domain.repository
│   │   ├── entity/                  # Entidades JPA (pueden diferir del dominio)
│   │   └── mapper/                  # MapStruct: entity ↔ domain model
│   ├── whatsapp/
│   │   ├── MetaWhatsAppClient.java  # Llamadas a Meta API
│   │   └── ClaudeAgentClient.java   # Llamadas a Anthropic API
│   ├── email/
│   │   └── JavaMailEmailSender.java
│   ├── qr/
│   │   └── ZXingQRGenerator.java
│   ├── scheduler/
│   │   └── ReminderSchedulerJob.java
│   └── websocket/
│       └── TurnosWebSocketHandler.java
│
├── presentation/                    # Capa HTTP — solo orquesta, sin lógica
│   ├── controller/
│   │   ├── superadmin/
│   │   │   └── TenantController.java
│   │   └── tenant/
│   │       ├── CitaController.java
│   │       ├── ProfesionalController.java
│   │       └── DashboardController.java
│   ├── webhook/
│   │   └── WhatsAppWebhookController.java
│   ├── dto/                         # DTOs de entrada/salida HTTP
│   └── advice/
│       └── GlobalExceptionHandler.java
│
└── config/
    ├── SecurityConfig.java
    ├── TenantFilter.java
    ├── SwaggerConfig.java
    └── WebSocketConfig.java
```

### Regla de dependencia (obligatoria)

```
✅ presentation  → application → domain
✅ infrastructure → domain (implementa interfaces)
❌ domain         → application  (nunca)
❌ domain         → infrastructure (nunca)
❌ application    → presentation (nunca)
```

---

## 3. Skills de Desarrollo — Backend Java/Spring Boot

### 3.1 Entidades y Modelo de Dominio

**Usar Lombok para reducir boilerplate:**

```java
@Entity
@Table(name = "citas")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @Enumerated(EnumType.STRING)
    private EstadoCita estado;

    // sin setters en campos de identidad del negocio
    // los cambios de estado van por métodos de dominio:
    public void confirmarAsistencia() {
        if (this.estado != EstadoCita.PENDIENTE) {
            throw new EstadoCitaInvalidoException("Solo se puede confirmar una cita pendiente");
        }
        this.estado = EstadoCita.CONFIRMADA;
    }
}
```

### 3.2 Use Cases (Application Layer)

Cada caso de uso es una clase con **un solo método público** y una sola responsabilidad (SRP):

```java
@Service
@RequiredArgsConstructor
@Transactional
public class AgendarCitaUseCase {

    private final CitaRepository citaRepository;
    private final ProfesionalRepository profesionalRepository;
    private final DisponibilidadService disponibilidadService;
    private final QRGenerator qrGenerator;
    private final EmailService emailService;

    public CitaResponse ejecutar(AgendarCitaCommand command) {
        // 1. Validar disponibilidad
        disponibilidadService.validar(command.profesionalId(), command.fecha(), command.hora());

        // 2. Crear la cita
        Cita cita = Cita.builder()
            .tenantId(command.tenantId())
            .profesionalId(command.profesionalId())
            .clienteId(command.clienteId())
            .servicioId(command.servicioId())
            .fecha(command.fecha())
            .horaInicio(command.hora())
            .estado(EstadoCita.PENDIENTE)
            .qrToken(qrGenerator.generarToken(command.tenantId(), command.profesionalId()))
            .build();

        citaRepository.save(cita);

        // 3. Notificar
        emailService.enviarConfirmacion(cita);

        return CitaResponse.from(cita);
    }
}
```

### 3.3 DTOs con Records (Java 21)

Usar `record` para DTOs inmutables — sin Lombok, sin boilerplate:

```java
// Command (entrada)
public record AgendarCitaCommand(
    UUID tenantId,
    UUID profesionalId,
    UUID clienteId,
    UUID servicioId,
    LocalDate fecha,
    LocalTime hora
) {}

// Response (salida)
public record CitaResponse(
    UUID id,
    String clienteNombre,
    String servicio,
    String profesional,
    LocalDate fecha,
    LocalTime horaInicio,
    EstadoCita estado
) {
    public static CitaResponse from(Cita cita) {
        return new CitaResponse(
            cita.getId(),
            cita.getCliente().getNombre(),
            cita.getServicio().getNombre(),
            cita.getProfesional().getNombre(),
            cita.getFecha(),
            cita.getHoraInicio(),
            cita.getEstado()
        );
    }
}
```

### 3.4 Mapeo con MapStruct

Para mapear entre entidades JPA y objetos de dominio sin código manual:

```java
@Mapper(componentModel = "spring")
public interface CitaMapper {
    CitaResponse toResponse(Cita cita);
    Cita toEntity(AgendarCitaCommand command);
}
```

### 3.5 Migraciones con Flyway

Toda modificación al schema es un archivo SQL versionado. **Nunca** se usa `spring.jpa.hibernate.ddl-auto=update` en producción:

```
src/main/resources/db/migration/
├── V1__crear_tablas_base.sql
├── V2__crear_tabla_citas.sql
├── V3__agregar_qr_token_a_citas.sql
├── V4__crear_tabla_conversation_state.sql
└── V5__agregar_hora_inicio_real_a_citas.sql
```

Regla: **un archivo por cambio, nunca se edita un archivo ya aplicado**.

### 3.6 Manejo Global de Excepciones

Un solo lugar para manejar todos los errores HTTP:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CitaNoDisponibleException.class)
    public ResponseEntity<ErrorResponse> handleCitaNoDisponible(CitaNoDisponibleException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new ErrorResponse("CITA_NO_DISPONIBLE", ex.getMessage()));
    }

    @ExceptionHandler(TenantInactivoException.class)
    public ResponseEntity<ErrorResponse> handleTenantInactivo(TenantInactivoException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(new ErrorResponse("TENANT_INACTIVO", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidacion(MethodArgumentNotValidException ex) {
        // retorna todos los campos con error
    }
}
```

### 3.7 Validación en Controllers

```java
@PostMapping("/citas")
public ResponseEntity<CitaResponse> agendar(@Valid @RequestBody AgendarCitaRequest request) {
    // @Valid dispara las anotaciones del DTO antes de llegar al use case
}

public record AgendarCitaRequest(
    @NotNull UUID profesionalId,
    @NotNull UUID servicioId,
    @NotNull @FutureOrPresent LocalDate fecha,
    @NotNull LocalTime hora
) {}
```

### 3.8 Documentación API con OpenAPI (API-First)

```java
@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Sistema de Citas Peluquería API")
                .version("1.0"))
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
            .components(new Components()
                .addSecuritySchemes("bearerAuth",
                    new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer")));
    }
}
```

Disponible en: `http://localhost:8080/swagger-ui.html`

### 3.9 Librerías Backend (pom.xml)

```xml
<!-- Spring Boot Starters -->
spring-boot-starter-web
spring-boot-starter-data-jpa
spring-boot-starter-security
spring-boot-starter-mail
spring-boot-starter-websocket
spring-boot-starter-validation

<!-- Base de datos -->
postgresql
flyway-core

<!-- Utilidades -->
lombok
mapstruct
mapstruct-processor

<!-- Seguridad -->
jjwt-api / jjwt-impl / jjwt-jackson   (JWT)

<!-- QR -->
com.google.zxing:core
com.google.zxing:javase

<!-- HTTP Client (para Meta API y Claude API) -->
spring-boot-starter-webflux    (WebClient no bloqueante)

<!-- Documentación -->
springdoc-openapi-starter-webmvc-ui

<!-- Testing -->
spring-boot-starter-test
testcontainers (PostgreSQL en tests de integración)
```

---

## 4. Clean Architecture — Frontend Angular 21

### Principio

El frontend se organiza por **features** (funcionalidades), no por tipo de archivo. Cada feature es un módulo lazy-loaded independiente que contiene todo lo que necesita.

### Estructura de carpetas

```
src/
├── app/
│   ├── core/                          # Singleton — se carga una vez
│   │   ├── auth/
│   │   │   ├── auth.service.ts
│   │   │   ├── auth.guard.ts
│   │   │   └── jwt.interceptor.ts     # Inyecta token en cada request
│   │   ├── tenant/
│   │   │   └── tenant.interceptor.ts  # Inyecta tenant context si aplica
│   │   └── websocket/
│   │       └── websocket.service.ts   # Conexión WebSocket compartida
│   │
│   ├── shared/                        # Reutilizable entre features
│   │   ├── components/
│   │   │   ├── page-header/
│   │   │   ├── estado-badge/          # Badge de estado de cita
│   │   │   └── confirm-dialog/        # Diálogo de confirmación genérico
│   │   ├── pipes/
│   │   │   ├── estado-cita.pipe.ts
│   │   │   └── duracion.pipe.ts
│   │   ├── models/                    # Interfaces TypeScript del dominio
│   │   │   ├── cita.model.ts
│   │   │   ├── profesional.model.ts
│   │   │   └── tenant.model.ts
│   │   └── utils/
│   │       └── date.utils.ts
│   │
│   ├── features/                      # Lazy-loaded — se carga al navegar
│   │   ├── dashboard/
│   │   │   ├── dashboard.routes.ts
│   │   │   ├── dashboard.component.ts         # Smart — tiene lógica
│   │   │   ├── dashboard.service.ts
│   │   │   └── components/
│   │   │       ├── calendario-citas/           # Dumb — solo presenta
│   │   │       ├── resumen-dia/                # Dumb
│   │   │       └── tarjeta-profesional/        # Dumb
│   │   │
│   │   ├── profesionales/
│   │   │   ├── profesionales.routes.ts
│   │   │   ├── lista-profesionales/
│   │   │   ├── form-profesional/
│   │   │   └── disponibilidad/
│   │   │
│   │   ├── citas/
│   │   │   ├── citas.routes.ts
│   │   │   ├── calendario-citas/
│   │   │   └── detalle-cita/
│   │   │
│   │   ├── servicios/
│   │   ├── clientes/
│   │   ├── reportes/
│   │   └── configuracion/
│   │
│   ├── superadmin/                    # Portal separado, lazy-loaded
│   │   ├── tenants/
│   │   ├── suscripciones/
│   │   └── planes/
│   │
│   └── public/                        # Sin auth
│       ├── display/                   # Pantalla de turnos
│       ├── checkin/                   # Página de check-in QR
│       └── login/
│
├── environments/
│   ├── environment.ts                 # desarrollo
│   └── environment.prod.ts            # producción
└── styles/
    ├── _variables.scss               # Tokens de diseño (colores, tipografía)
    └── _primeng-theme.scss           # Override de tema PrimeNG
```

---

## 5. Skills de Desarrollo — Frontend Angular 21

### 5.1 Signals (Angular 16+) — Estado Reactivo Moderno

Usar **Signals** en lugar de BehaviorSubject para estado local de componentes. Son más simples y tienen mejor rendimiento:

```typescript
// ✅ Con Signals (preferido)
@Component({ ... })
export class DashboardComponent {
  private dashboardService = inject(DashboardService);

  citas = signal<Cita[]>([]);
  cargando = signal(false);
  resumen = computed(() => ({
    total: this.citas().length,
    completadas: this.citas().filter(c => c.estado === 'COMPLETADA').length
  }));

  ngOnInit() {
    this.cargarCitas();
  }

  private async cargarCitas() {
    this.cargando.set(true);
    const data = await this.dashboardService.getCitasHoy();
    this.citas.set(data);
    this.cargando.set(false);
  }
}
```

### 5.2 Smart / Dumb Components

- **Smart (Container):** se conecta a servicios, maneja estado, coordina
- **Dumb (Presentational):** recibe datos por `@Input`, emite eventos por `@Output`, sin lógica

```typescript
// DUMB — TarjetaProfesionalComponent
@Component({
  selector: 'app-tarjeta-profesional',
  changeDetection: ChangeDetectionStrategy.OnPush,  // ← siempre OnPush en dumb
  template: `
    <p-card>
      <ng-template pTemplate="header">
        <p-avatar [image]="profesional().foto" size="large" shape="circle"/>
      </ng-template>
      <h3>{{ profesional().nombre }}</h3>
      <app-estado-badge [estado]="profesional().estadoHoy"/>
    </p-card>
  `
})
export class TarjetaProfesionalComponent {
  profesional = input.required<ProfesionalDashboard>();  // Signal input (Angular 17+)
  verDetalle = output<UUID>();                           // Signal output
}
```

### 5.3 Servicios con HttpClient tipado

```typescript
@Injectable({ providedIn: 'root' })
export class CitaService {
  private http = inject(HttpClient);
  private baseUrl = inject(API_URL);

  getCitasHoy(fecha?: string): Observable<CitaDashboard[]> {
    const params = new HttpParams().set('fecha', fecha ?? today());
    return this.http.get<CitaDashboard[]>(`${this.baseUrl}/dashboard/citas`, { params });
  }

  agendarCita(command: AgendarCitaRequest): Observable<CitaResponse> {
    return this.http.post<CitaResponse>(`${this.baseUrl}/citas`, command);
  }
}
```

### 5.4 Interceptores

**JWT Interceptor** — agrega el token automáticamente:

```typescript
export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const token = inject(AuthService).getToken();
  if (token) {
    req = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
  }
  return next(req);
};
```

### 5.5 Guards de Rutas

```typescript
export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (auth.isAuthenticated()) return true;
  return router.createUrlTree(['/login']);
};

export const superAdminGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  return auth.hasRole('SUPER_ADMIN') || inject(Router).createUrlTree(['/dashboard']);
};
```

### 5.6 Lazy Loading de Features

```typescript
// app.routes.ts
export const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  { path: 'login', loadComponent: () => import('./public/login/login.component') },
  {
    path: 'dashboard',
    canActivate: [authGuard],
    loadChildren: () => import('./features/dashboard/dashboard.routes')
  },
  {
    path: 'profesionales',
    canActivate: [authGuard],
    loadChildren: () => import('./features/profesionales/profesionales.routes')
  },
  {
    path: 'display/:token',
    loadComponent: () => import('./public/display/display.component')
  },
  {
    path: 'superadmin',
    canActivate: [authGuard, superAdminGuard],
    loadChildren: () => import('./superadmin/superadmin.routes')
  }
];
```

### 5.7 Calendario con PrimeNG + FullCalendar

```typescript
@Component({ ... })
export class CalendarioCitasComponent implements OnInit {
  calendarOptions = signal<CalendarOptions>({
    plugins: [resourceTimeGridPlugin, interactionPlugin],
    initialView: 'resourceTimeGridDay',
    resources: [],          // se llena con los profesionales
    events: [],             // se llena con las citas
    editable: true,         // drag & drop
    eventDrop: (info) => this.reprogramarCita(info),
    eventClick: (info) => this.verDetalleCita(info),
    select: (info) => this.nuevaCita(info),
    nowIndicator: true,     // línea de hora actual
    slotMinTime: '07:00:00',
    slotMaxTime: '21:00:00',
    locale: esLocale,
    headerToolbar: {
      left: 'prev,next today',
      center: 'title',
      right: 'resourceTimeGridDay,timeGridWeek'
    }
  });
}
```

### 5.8 WebSocket en Angular

```typescript
@Injectable({ providedIn: 'root' })
export class TurnosWebSocketService {
  private socket$ = new WebSocketSubject<TurnoEvent | null>(null);

  conectar(displayToken: string): Observable<TurnoEvent> {
    this.socket$ = webSocket(`wss://api.tuplataforma.com/ws/display/${displayToken}`);
    return this.socket$.pipe(
      retryWhen(errors => errors.pipe(delay(3000))), // reconexión automática
      filter((msg): msg is TurnoEvent => msg !== null)
    );
  }
}
```

### 5.9 Librerías Frontend

```json
{
  "dependencies": {
    "@angular/core": "^21.0.0",
    "primeng": "^21.1.8",
    "primeicons": "^7.0.0",
    "@fullcalendar/core": "^6.1.0",
    "@fullcalendar/resource-timegrid": "^6.1.0",
    "@fullcalendar/interaction": "^6.1.0",
    "rxjs": "^7.8.0",
    "chart.js": "^4.4.0"
  },
  "devDependencies": {
    "@angular/cli": "^21.0.0",
    "@angular-eslint/eslint-plugin": "^18.0.0",
    "typescript": "^5.5.0",
    "jest": "^29.0.0",
    "@testing-library/angular": "^17.0.0",
    "stylelint": "^16.0.0",
    "stylelint-config-standard-scss": "^13.0.0",
    "stylelint-no-hardcoded-colors": "^2.0.0",
    "husky": "^9.0.0",
    "lint-staged": "^15.0.0"
  }
}
```

### 5.10 Regla de Tokens — Sin valores hardcodeados

Ver reglas RN-DESIGN-001 a RN-DESIGN-006 en `REGLAS_NEGOCIO.md`. La estructura completa de tokens vive en `src/styles/tokens/`. Los componentes **nunca** usan hex, `rgb()` ni nombres de fuente directamente.

```scss
// ✅ Componente correcto — sin ningún valor hardcodeado
.appointment-block {
  background:   var(--cita-pendiente-bg);
  border-color: var(--cita-pendiente-border);
  border-radius: var(--radius-sm);
  transition:   filter var(--duration-fast) var(--easing-default);

  .client-name {
    font-family: var(--font-body);
    font-size:   var(--text-sm);
    font-weight: var(--font-medium);
    color:       var(--cita-pendiente-text);
  }
}
```

---

## 6. Estrategia de Testing

### Backend

| Tipo | Qué testea | Herramienta |
|------|-----------|-------------|
| Unitario | Use Cases y reglas de dominio | JUnit 5 + Mockito |
| Integración | Repositorios contra DB real | Testcontainers (PostgreSQL) |
| API | Endpoints completos | Spring MockMvc |
| Contrato | Que la API no rompe el contrato | Spring Cloud Contract |

```java
// Ejemplo test unitario de un Use Case
@ExtendWith(MockitoExtension.class)
class AgendarCitaUseCaseTest {

    @Mock CitaRepository citaRepository;
    @Mock DisponibilidadService disponibilidadService;
    @InjectMocks AgendarCitaUseCase useCase;

    @Test
    void deberiaAgendarCitaExitosamente() {
        // given
        var command = new AgendarCitaCommand(...);
        when(disponibilidadService.estaDisponible(...)).thenReturn(true);

        // when
        var result = useCase.ejecutar(command);

        // then
        assertThat(result.estado()).isEqualTo(EstadoCita.PENDIENTE);
        verify(citaRepository).save(any(Cita.class));
    }

    @Test
    void deberiaLanzarExcepcionSiHorarioNoDisponible() {
        when(disponibilidadService.estaDisponible(...)).thenReturn(false);
        assertThrows(CitaNoDisponibleException.class, () -> useCase.ejecutar(command));
    }
}
```

### Frontend

| Tipo | Qué testea | Herramienta |
|------|-----------|-------------|
| Unitario | Servicios, pipes, guards | Jest |
| Componente | Render y comportamiento | Angular Testing Library |
| E2E | Flujos completos | Playwright |

---

## 7. Flujo de Trabajo Git

### Estrategia de Ramas

```
main            ← producción, siempre estable
  └── develop   ← integración continua
        └── feature/agendar-cita-agente    ← una rama por feature
        └── feature/dashboard-calendario
        └── fix/error-timezone-disponibilidad
        └── hotfix/token-whatsapp-expirado  ← correcciones urgentes a main
```

### Conventional Commits

Formato obligatorio para todos los commits:

```
tipo(scope): descripción corta en minúsculas

feat(citas): agregar endpoint de disponibilidad por profesional
fix(agente): corregir manejo de estado cuando cliente cancela
refactor(dashboard): migrar BehaviorSubject a Signals
test(citas): agregar tests de integración para AgendarCitaUseCase
docs(api): actualizar spec OpenAPI de /api/tenant/citas
chore(deps): actualizar PrimeNG a 21.1.8
```

### Tipos válidos

| Tipo | Cuándo usarlo |
|------|--------------|
| `feat` | Nueva funcionalidad |
| `fix` | Corrección de bug |
| `refactor` | Cambio sin nueva funcionalidad ni bug |
| `test` | Agregar o corregir tests |
| `docs` | Solo documentación |
| `chore` | Dependencias, config, scripts |
| `perf` | Mejora de rendimiento |

### Pull Request (PR)

Todo cambio va por PR a `develop`. El PR debe:
1. Pasar todos los tests (CI automático)
2. Pasar el análisis de SonarCloud
3. Tener al menos 1 revisión aprobada

---

## 8. Calidad de Código

### Backend — Herramientas

| Herramienta | Propósito | Config |
|-------------|-----------|--------|
| Checkstyle | Estilo de código Java | `checkstyle.xml` |
| SpotBugs | Detecta bugs comunes | `spotbugs-exclude.xml` |
| SonarCloud | Cobertura, deuda técnica, vulnerabilidades | CI/CD |
| JaCoCo | Reporte de cobertura | Umbral mínimo: 70% |

### Frontend — Herramientas

| Herramienta | Propósito |
|-------------|-----------|
| ESLint + Angular ESLint | Reglas de código TypeScript/Angular |
| Prettier | Formato consistente |
| Stylelint | Detecta colores, fuentes y valores hardcodeados en SCSS |
| SonarCloud | Cobertura y calidad |
| Husky + lint-staged | Pre-commit: ejecuta linters antes de aceptar el commit |

### Stylelint — configuración completa

```json
// .stylelintrc.json
{
  "extends": ["stylelint-config-standard-scss"],
  "plugins": ["stylelint-no-hardcoded-colors"],
  "rules": {
    "no-hardcoded-colors": [true, {
      "severity": "error",
      "message": "Usa un token CSS (var(--...)) en lugar de un valor de color directo"
    }],
    "color-no-invalid-hex": true,
    "declaration-property-value-disallowed-list": {
      "font-family": ["/^['\"]?(Inter|Playfair Display|JetBrains Mono)/"],
      "message": "Usa var(--font-body), var(--font-display) o var(--font-mono)"
    },
    "unit-disallowed-list": {
      "px": { "severity": "warning", "message": "Usa un token de espaciado (var(--space-N)) o tipografía (var(--text-N))" }
    }
  },
  "ignoreFiles": [
    "src/styles/tokens/_colors.scss"
  ]
}
```

> `_colors.scss` es el **único archivo excluido** de la regla — es el único lugar donde se permiten hex.

### ESLint — regla de tokens en TypeScript

```json
// .eslintrc.json (fragmento)
{
  "rules": {
    "no-restricted-syntax": [
      "error",
      {
        "selector": "Literal[value=/^#[0-9A-Fa-f]{3,8}$/]",
        "message": "RN-DESIGN-001: usa DesignTokens.* en lugar de colores hex en TypeScript"
      },
      {
        "selector": "Literal[value=/rgb\\(|rgba\\(|hsl\\(/]",
        "message": "RN-DESIGN-001: usa DesignTokens.* en lugar de valores de color directos"
      }
    ]
  }
}
```

### Husky — pre-commit hook

```bash
# Instalar
npx husky init

# .husky/pre-commit
npx lint-staged
```

```json
// package.json — lint-staged
{
  "lint-staged": {
    "*.{ts,html}": ["eslint --fix", "prettier --write"],
    "*.scss":      ["stylelint --fix", "prettier --write"]
  }
}
```

**Flujo de protección:** el desarrollador hace `git commit` → Husky dispara lint-staged → ESLint valida TypeScript (sin colores hardcodeados) + Stylelint valida SCSS (sin hex fuera de `_colors.scss`) → si alguno falla, el commit **se rechaza** con el mensaje de la regla violada.

### Reglas de Calidad Mínimas

- Cobertura de tests: **≥ 70%** en Use Cases (backend) y servicios (frontend)
- Sin `any` en TypeScript (habilitado en `tsconfig.json`)
- Sin `console.log` en producción
- Sin colores, fuentes ni espaciados hardcodeados (Stylelint + ESLint lo bloquean en pre-commit)
- Todos los endpoints documentados en OpenAPI antes de implementar
- Ningún secret en código fuente (usar variables de entorno)

---

## 9. API-First con OpenAPI

El contrato de la API se escribe **antes** de implementar el endpoint. Esto permite:
- Frontend y backend trabajar en paralelo
- Generar mocks automáticos para tests
- Detectar inconsistencias antes de codificar

### Flujo

```
1. Escribir el endpoint en API_SPEC.yaml
2. Revisar con el equipo
3. Backend: implementa el controlador anotado con @Operation (Swagger)
4. Frontend: genera el cliente TypeScript desde la spec (openapi-generator)
5. Ambos lados trabajan contra el mismo contrato
```

```yaml
# Ejemplo en API_SPEC.yaml
/api/tenant/citas:
  post:
    summary: Agendar una nueva cita
    operationId: agendarCita
    tags: [Citas]
    requestBody:
      required: true
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/AgendarCitaRequest'
    responses:
      '201':
        description: Cita agendada exitosamente
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CitaResponse'
      '409':
        description: Horario no disponible
      '403':
        description: Tenant inactivo o suscripción vencida
```

---

*Documento de Metodología — Sistema de Citas SaaS v1.0*
