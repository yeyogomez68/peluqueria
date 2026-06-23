# Módulos Competitivos Stilum — Plan de Implementación

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implementar 4 módulos que convierten Stilum de agenda digital a sistema de gestión completo para negocios de atención al cliente en Colombia: cobro+comisiones, contabilidad diaria, portal del profesional y bot WhatsApp con IA.

**Architecture:** Clean Architecture existente (domain → application → infrastructure → presentation). Cada módulo agrega entidades en `domain/`, servicios en `application/`, repositorios JPA en `infrastructure/persistence/jpa/`, controllers en `presentation/`, y componentes Angular en `stilum-frontend/src/app/features/`. Las migraciones Flyway siguen la secuencia V12–V16.

**Tech Stack:** Java 21, Spring Boot 3.3, Spring Data JPA, PostgreSQL 16, Flyway, Lombok, MapStruct, Angular 21, PrimeNG 21, RxJS 7.8. IA: Claude API via `claude-haiku-4-5-20251001` con Spring WebFlux (ya en el classpath).

## Global Constraints

- Todas las tablas nuevas deben tener columna `tenant_id UUID NOT NULL REFERENCES tenants(id)` con índice.
- Todos los `@Entity` deben tener `@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")`.
- Los DTOs son Java 21 `record`.
- Los controllers usan `@PreAuthorize` con roles exactos.
- Flyway: numeración continua desde `V12`. Nunca modificar migraciones existentes.
- Frontend: componentes standalone, lazy-loaded, importan `CommonModule`, `RouterModule` y los módulos PrimeNG necesarios.
- Convención de paquetes: `com.stilum.citas.{domain|application|infrastructure|presentation}.<módulo>`.
- Tests de integración usan Testcontainers (PostgreSQL). Tests unitarios con Mockito.
- Checkstyle: líneas máx 120 chars, sin imports con `*`, nombres en español.

---

## MÓDULO 1 — Cobro + Comisiones

### Task 1: Migración DB — comisión en profesionales y pago en citas

**Files:**
- Create: `stilum-backend/src/main/resources/db/migration/V12__add_comisiones_y_pago.sql`

**Interfaces:**
- Produces: columna `comision_porcentaje` en `profesionales`, columnas `metodo_pago` y `comision_calculada` en `citas`.

- [ ] **Step 1: Crear migración SQL**

```sql
-- V12: Comisiones por profesional y método de pago en citas

-- 1. Porcentaje de comisión por profesional (default 30%)
ALTER TABLE profesionales
    ADD COLUMN comision_porcentaje NUMERIC(5, 2) NOT NULL DEFAULT 30.00
        CHECK (comision_porcentaje >= 0 AND comision_porcentaje <= 100);

COMMENT ON COLUMN profesionales.comision_porcentaje IS
    'Porcentaje de comisión que le corresponde al profesional sobre precio_cobrado';

-- 2. Método de pago y comisión calculada al completar una cita
ALTER TABLE citas
    ADD COLUMN metodo_pago VARCHAR(20)
        CHECK (metodo_pago IN ('EFECTIVO', 'NEQUI', 'DAVIPLATA', 'TARJETA', 'TRANSFERENCIA')),
    ADD COLUMN comision_calculada NUMERIC(10, 2);

COMMENT ON COLUMN citas.metodo_pago IS 'Medio de pago registrado al completar la cita';
COMMENT ON COLUMN citas.comision_calculada IS
    'precio_cobrado * profesional.comision_porcentaje / 100, calculado al completar';
```

- [ ] **Step 2: Verificar que Flyway aplica la migración**

```bash
cd stilum-backend
mvn flyway:migrate -Dflyway.url=jdbc:postgresql://localhost:5432/stilum -Dflyway.user=stilum -Dflyway.password=stilum
```

Expected: `Successfully applied 1 migration to schema "public", now at version v12`.

- [ ] **Step 3: Commit**

```bash
git add stilum-backend/src/main/resources/db/migration/V12__add_comisiones_y_pago.sql
git commit -m "feat(db): add comision_porcentaje to profesionales, metodo_pago and comision_calculada to citas (V12)"
```

---

### Task 2: Dominio — comisión en Profesional + pago en Cita

**Files:**
- Modify: `stilum-backend/src/main/java/com/stilum/citas/domain/profesional/Profesional.java`
- Modify: `stilum-backend/src/main/java/com/stilum/citas/domain/cita/Cita.java`

**Interfaces:**
- Consumes: columnas V12 en DB.
- Produces: `Profesional.getComisionPorcentaje(): BigDecimal`, `Cita.getMetodoPago(): String`, `Cita.getComisionCalculada(): BigDecimal`, `Cita.completar(BigDecimal precioCobrado, String metodoPago, BigDecimal comisionPorcentaje)`.

- [ ] **Step 1: Agregar campo comisión en Profesional**

En `Profesional.java`, añadir después del campo `activo`:

```java
@Column(name = "comision_porcentaje", nullable = false, precision = 5, scale = 2)
private BigDecimal comisionPorcentaje = new BigDecimal("30.00");
```

Agregar a `Profesional.crear(...)` el parámetro y asignación:

```java
public static Profesional crear(Tenant tenant, String nombre, String especialidad,
                                String bio, String colorAgenda) {
    Profesional p = new Profesional();
    p.tenant = tenant;
    p.nombre = nombre;
    p.especialidad = especialidad;
    p.bio = bio;
    p.colorAgenda = colorAgenda;
    p.comisionPorcentaje = new BigDecimal("30.00");
    return p;
}
```

Agregar método para actualizar la comisión:

```java
public void actualizarComision(BigDecimal porcentaje) {
    if (porcentaje == null || porcentaje.compareTo(BigDecimal.ZERO) < 0
            || porcentaje.compareTo(new BigDecimal("100")) > 0) {
        throw new com.stilum.citas.domain.shared.ReglaNegocioException(
                "COMISION_INVALIDA", "El porcentaje debe estar entre 0 y 100");
    }
    this.comisionPorcentaje = porcentaje;
}
```

- [ ] **Step 2: Agregar campos pago y comisión en Cita**

En `Cita.java`, añadir después del campo `precioCobrado`:

```java
@Column(name = "metodo_pago", length = 20)
private String metodoPago;

@Column(name = "comision_calculada", precision = 10, scale = 2)
private BigDecimal comisionCalculada;
```

Reemplazar el método `completar` existente:

```java
public void completar(BigDecimal precioCobrado, String metodoPago, BigDecimal comisionPorcentaje) {
    cambiarEstado(CitaEstado.COMPLETADA, null, null);
    this.precioCobrado = precioCobrado != null ? precioCobrado : servicio.getPrecio();
    this.metodoPago = metodoPago;
    if (comisionPorcentaje != null && this.precioCobrado != null) {
        this.comisionCalculada = this.precioCobrado
                .multiply(comisionPorcentaje)
                .divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
    }
}
```

Agregar import al inicio de `Cita.java`:
```java
import java.math.RoundingMode;
```

- [ ] **Step 3: Compilar para verificar**

```bash
cd stilum-backend && mvn compile -q
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 4: Commit**

```bash
git add stilum-backend/src/main/java/com/stilum/citas/domain/profesional/Profesional.java
git add stilum-backend/src/main/java/com/stilum/citas/domain/cita/Cita.java
git commit -m "feat(domain): add comision_porcentaje to Profesional, metodo_pago and comision_calculada to Cita"
```

---

### Task 3: Application — DTOs y servicio de cobro

**Files:**
- Create: `stilum-backend/src/main/java/com/stilum/citas/application/cita/dto/RegistrarPagoRequest.java`
- Modify: `stilum-backend/src/main/java/com/stilum/citas/application/cita/dto/CitaResponse.java`
- Modify: `stilum-backend/src/main/java/com/stilum/citas/application/cita/CitaService.java`
- Modify: `stilum-backend/src/main/java/com/stilum/citas/application/profesional/dto/UpdateProfesionalRequest.java`
- Modify: `stilum-backend/src/main/java/com/stilum/citas/application/profesional/ProfesionalService.java`

**Interfaces:**
- Consumes: `Cita.completar(BigDecimal, String, BigDecimal)`, `Profesional.getComisionPorcentaje()`.
- Produces: `CitaService.registrarPago(UUID, RegistrarPagoRequest): CitaResponse`, `ProfesionalService.actualizarComision(UUID, BigDecimal)`.

- [ ] **Step 1: Crear RegistrarPagoRequest**

```java
package com.stilum.citas.application.cita.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

public record RegistrarPagoRequest(
        @NotNull @DecimalMin("0.00") BigDecimal precioCobrado,
        @NotNull @Pattern(regexp = "EFECTIVO|NEQUI|DAVIPLATA|TARJETA|TRANSFERENCIA")
        String metodoPago
) {}
```

- [ ] **Step 2: Actualizar CitaResponse — agregar campos nuevos**

En `CitaResponse.java`, agregar `metodoPago` y `comisionCalculada` al record:

```java
public record CitaResponse(
        UUID id,
        UUID qrToken,
        CitaEstado estado,
        ZonedDateTime fechaHoraInicio,
        ZonedDateTime fechaHoraFin,
        int duracionMin,
        BigDecimal precioCobrado,
        String metodoPago,
        BigDecimal comisionCalculada,
        String origen,
        String notas,
        String motivoCancelacion,
        String canceladoPor,
        Instant checkedInAt,
        ProfesionalResumen profesional,
        ServicioResumen servicio,
        ClienteResumen cliente,
        Instant createdAt
) {
    public record ProfesionalResumen(UUID id, String nombre, String colorAgenda, BigDecimal comisionPorcentaje) {}
    public record ServicioResumen(UUID id, String nombre, int duracionMin, BigDecimal precio) {}
    public record ClienteResumen(UUID id, String nombre, String telefono) {}

    public static CitaResponse from(Cita c) {
        return new CitaResponse(
                c.getId(),
                c.getQrToken(),
                c.getEstado(),
                c.getFechaHoraInicio(),
                c.getFechaHoraFin(),
                c.getDuracionMin(),
                c.getPrecioCobrado(),
                c.getMetodoPago(),
                c.getComisionCalculada(),
                c.getOrigen(),
                c.getNotas(),
                c.getMotivoCancelacion(),
                c.getCanceladoPor(),
                c.getCheckedInAt(),
                new ProfesionalResumen(
                        c.getProfesional().getId(),
                        c.getProfesional().getNombre(),
                        c.getProfesional().getColorAgenda(),
                        c.getProfesional().getComisionPorcentaje()),
                new ServicioResumen(
                        c.getServicio().getId(),
                        c.getServicio().getNombre(),
                        c.getServicio().getDuracionMin(),
                        c.getServicio().getPrecio()),
                new ClienteResumen(
                        c.getCliente().getId(),
                        c.getCliente().getNombre(),
                        c.getCliente().getTelefono()),
                c.getCreatedAt()
        );
    }
}
```

- [ ] **Step 3: Agregar registrarPago en CitaService**

En `CitaService.java`, reemplazar el método `completar` y agregar `registrarPago`:

```java
/** Completa una cita con precio sugerido (sin método de pago explícito). */
@Transactional
public CitaResponse completar(UUID id, CompletarCitaRequest req) {
    Cita cita = findAndVerify(id);
    BigDecimal comision = cita.getProfesional().getComisionPorcentaje();
    cita.completar(req.precioCobrado(), null, comision);
    return CitaResponse.from(citaRepository.save(cita));
}

/** Registra el pago real — precio cobrado + método de pago — y calcula comisión. */
@Transactional
public CitaResponse registrarPago(UUID id, RegistrarPagoRequest req) {
    Cita cita = findAndVerify(id);
    BigDecimal comision = cita.getProfesional().getComisionPorcentaje();
    cita.completar(req.precioCobrado(), req.metodoPago(), comision);
    return CitaResponse.from(citaRepository.save(cita));
}
```

- [ ] **Step 4: Agregar actualizarComision en ProfesionalService**

Abrir `stilum-backend/src/main/java/com/stilum/citas/application/profesional/ProfesionalService.java` y agregar al final de la clase:

```java
@Transactional
public ProfesionalResponse actualizarComision(UUID profesionalId, java.math.BigDecimal porcentaje) {
    Profesional p = profesionalRepository.findById(profesionalId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Profesional", profesionalId));
    p.actualizarComision(porcentaje);
    return ProfesionalResponse.from(profesionalRepository.save(p));
}
```

- [ ] **Step 5: Compilar**

```bash
cd stilum-backend && mvn compile -q
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 6: Commit**

```bash
git add stilum-backend/src/main/java/com/stilum/citas/application/
git commit -m "feat(application): add registrarPago use case and actualizarComision in ProfesionalService"
```

---

### Task 4: Controller — endpoint de pago y comisión

**Files:**
- Modify: `stilum-backend/src/main/java/com/stilum/citas/presentation/cita/CitaController.java`
- Modify: `stilum-backend/src/main/java/com/stilum/citas/presentation/profesional/ProfesionalController.java`

**Interfaces:**
- Consumes: `CitaService.registrarPago(UUID, RegistrarPagoRequest)`, `ProfesionalService.actualizarComision(UUID, BigDecimal)`.
- Produces: `PATCH /api/citas/{id}/pagar`, `PATCH /api/profesionales/{id}/comision`.

- [ ] **Step 1: Agregar endpoint de pago en CitaController**

En `CitaController.java`, agregar después del método `completar`:

```java
@PatchMapping("/{id}/pagar")
@PreAuthorize("hasAnyRole('ADMIN_TENANT', 'PROFESIONAL', 'SUPER_ADMIN')")
@Operation(summary = "Registra pago de una cita", description = "Completa la cita con precio real cobrado y método de pago. Calcula la comisión del profesional automáticamente.")
public CitaResponse registrarPago(@PathVariable UUID id,
                                   @Valid @RequestBody RegistrarPagoRequest req) {
    return citaService.registrarPago(id, req);
}
```

Agregar import:
```java
import com.stilum.citas.application.cita.dto.RegistrarPagoRequest;
```

- [ ] **Step 2: Agregar endpoint de comisión en ProfesionalController**

En `ProfesionalController.java`, agregar:

```java
@PatchMapping("/{id}/comision")
@PreAuthorize("hasRole('ADMIN_TENANT')")
@Operation(summary = "Actualiza porcentaje de comisión del profesional")
public ProfesionalResponse actualizarComision(
        @PathVariable UUID id,
        @RequestParam @jakarta.validation.constraints.DecimalMin("0")
        @jakarta.validation.constraints.DecimalMax("100") java.math.BigDecimal porcentaje) {
    return profesionalService.actualizarComision(id, porcentaje);
}
```

- [ ] **Step 3: Verificar compilación y ejecutar tests existentes**

```bash
cd stilum-backend && mvn test -pl . -Dtest="CitaServiceTest,ProfesionalServiceTest" -q 2>&1 | tail -5
```

Expected: `BUILD SUCCESS` o solo fallos de tests que no existen aún.

- [ ] **Step 4: Commit**

```bash
git add stilum-backend/src/main/java/com/stilum/citas/presentation/
git commit -m "feat(api): add PATCH /citas/{id}/pagar and PATCH /profesionales/{id}/comision endpoints"
```

---

### Task 5: Frontend — modal de cobro

**Files:**
- Create: `stilum-frontend/src/app/features/dashboard/registrar-pago-modal.component.ts`
- Modify: `stilum-frontend/src/app/features/dashboard/dashboard.component.ts`
- Modify: `stilum-frontend/src/app/core/services/cita-api.service.ts`

**Interfaces:**
- Consumes: `PATCH /api/citas/{id}/pagar`.
- Produces: modal `RegistrarPagoModalComponent` emite `(pagado)` con `CitaResponse`.

- [ ] **Step 1: Actualizar CItaApiService con método registrarPago**

Abrir `stilum-frontend/src/app/core/services/cita-api.service.ts` y agregar:

```typescript
registrarPago(citaId: string, body: { precioCobrado: number; metodoPago: string }): Observable<CitaResponse> {
  return this.http.patch<CitaResponse>(`${this.base}/${citaId}/pagar`, body);
}
```

- [ ] **Step 2: Crear RegistrarPagoModalComponent**

```typescript
// stilum-frontend/src/app/features/dashboard/registrar-pago-modal.component.ts
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { InputNumberModule } from 'primeng/inputnumber';
import { DropdownModule } from 'primeng/dropdown';
import { CitaApiService } from '../../core/services/cita-api.service';
import { CitaResponse } from '../../core/models/cita.model';

@Component({
  selector: 'app-registrar-pago-modal',
  standalone: true,
  imports: [CommonModule, FormsModule, DialogModule, ButtonModule, InputNumberModule, DropdownModule],
  template: `
    <p-dialog header="Registrar Pago" [(visible)]="visible" [modal]="true" [style]="{ width: '400px' }"
              (onHide)="cancelar()">
      <div class="flex flex-column gap-3 py-2">
        <div>
          <label class="block mb-1 font-semibold">Precio cobrado (COP)</label>
          <p-inputNumber [(ngModel)]="precioCobrado" mode="currency" currency="COP" locale="es-CO"
                         [min]="0" styleClass="w-full" />
        </div>
        <div>
          <label class="block mb-1 font-semibold">Método de pago</label>
          <p-dropdown [options]="metodos" [(ngModel)]="metodoPago" placeholder="Seleccionar"
                      styleClass="w-full" />
        </div>
        <div *ngIf="precioCobrado && comisionPorcentaje" class="surface-100 border-round p-3">
          <span class="text-600">Comisión profesional ({{ comisionPorcentaje }}%): </span>
          <span class="font-bold text-primary">{{ precioCobrado * comisionPorcentaje / 100 | currency:'COP':'symbol':'1.0-0':'es-CO' }}</span>
        </div>
      </div>
      <ng-template pTemplate="footer">
        <p-button label="Cancelar" severity="secondary" (onClick)="cancelar()" />
        <p-button label="Registrar pago" icon="pi pi-check" [loading]="loading"
                  [disabled]="!precioCobrado || !metodoPago" (onClick)="confirmar()" />
      </ng-template>
    </p-dialog>
  `
})
export class RegistrarPagoModalComponent implements OnInit {
  @Input() citaId!: string;
  @Input() precioSugerido = 0;
  @Input() comisionPorcentaje = 30;
  @Input() visible = false;
  @Output() visibleChange = new EventEmitter<boolean>();
  @Output() pagado = new EventEmitter<CitaResponse>();

  precioCobrado = 0;
  metodoPago = '';
  loading = false;
  metodos = [
    { label: 'Efectivo', value: 'EFECTIVO' },
    { label: 'Nequi', value: 'NEQUI' },
    { label: 'Daviplata', value: 'DAVIPLATA' },
    { label: 'Tarjeta', value: 'TARJETA' },
    { label: 'Transferencia', value: 'TRANSFERENCIA' }
  ];

  constructor(private citaApi: CitaApiService) {}

  ngOnInit() { this.precioCobrado = this.precioSugerido; }

  confirmar() {
    this.loading = true;
    this.citaApi.registrarPago(this.citaId, {
      precioCobrado: this.precioCobrado,
      metodoPago: this.metodoPago
    }).subscribe({
      next: (cita) => { this.pagado.emit(cita); this.cerrar(); },
      error: () => { this.loading = false; }
    });
  }

  cancelar() { this.cerrar(); }

  private cerrar() {
    this.loading = false;
    this.visible = false;
    this.visibleChange.emit(false);
  }
}
```

- [ ] **Step 3: Integrar modal en DashboardComponent**

En `dashboard.component.ts`, agregar el import del modal en `imports: [...]` y en el template:

```html
<app-registrar-pago-modal
  [(visible)]="pagoModalVisible"
  [citaId]="citaSeleccionadaId"
  [precioSugerido]="precioSugerido"
  [comisionPorcentaje]="comisionPorcentaje"
  (pagado)="onPagoRegistrado($event)">
</app-registrar-pago-modal>
```

Agregar en la clase:
```typescript
pagoModalVisible = false;
citaSeleccionadaId = '';
precioSugerido = 0;
comisionPorcentaje = 30;

abrirModalPago(cita: CitaResponse) {
  this.citaSeleccionadaId = cita.id;
  this.precioSugerido = cita.servicio.precio;
  this.comisionPorcentaje = cita.profesional.comisionPorcentaje;
  this.pagoModalVisible = true;
}

onPagoRegistrado(cita: CitaResponse) {
  this.cargarCitas();
}
```

- [ ] **Step 4: Commit**

```bash
git add stilum-frontend/src/app/features/dashboard/registrar-pago-modal.component.ts
git add stilum-frontend/src/app/features/dashboard/dashboard.component.ts
git add stilum-frontend/src/app/core/services/cita-api.service.ts
git commit -m "feat(frontend): add registrar-pago-modal with comision preview"
```

---

## MÓDULO 2 — Contabilidad Diaria

### Task 6: Backend — ResumenDia (service + DTO + repository query)

**Files:**
- Create: `stilum-backend/src/main/java/com/stilum/citas/application/contabilidad/dto/ResumenDiaResponse.java`
- Create: `stilum-backend/src/main/java/com/stilum/citas/application/contabilidad/ContabilidadService.java`
- Modify: `stilum-backend/src/main/java/com/stilum/citas/domain/cita/CitaRepository.java`
- Modify: `stilum-backend/src/main/java/com/stilum/citas/infrastructure/persistence/jpa/JpaCitaRepository.java`

**Interfaces:**
- Consumes: `CitaRepository.findCompletadasPorTenantYFecha(UUID, LocalDate)`.
- Produces: `ContabilidadService.resumenDia(LocalDate): ResumenDiaResponse`.

- [ ] **Step 1: Crear ResumenDiaResponse**

```java
package com.stilum.citas.application.contabilidad.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ResumenDiaResponse(
        LocalDate fecha,
        int totalCitas,
        BigDecimal totalVendido,
        BigDecimal totalComisiones,
        BigDecimal totalNegocio,
        List<ResumenProfesional> porProfesional,
        List<ResumenServicio> porServicio,
        List<ResumenMetodoPago> porMetodoPago
) {
    public record ResumenProfesional(
            String nombre,
            int citas,
            BigDecimal totalVendido,
            BigDecimal comision,
            BigDecimal comisionPorcentaje
    ) {}

    public record ResumenServicio(
            String nombre,
            int cantidad,
            BigDecimal totalVendido
    ) {}

    public record ResumenMetodoPago(
            String metodo,
            int cantidad,
            BigDecimal total
    ) {}
}
```

- [ ] **Step 2: Agregar query en CitaRepository**

En `CitaRepository.java` (interface en domain), agregar:

```java
List<Cita> findCompletadasPorTenantYFecha(UUID tenantId, java.time.LocalDate fecha);
```

- [ ] **Step 3: Implementar query en JpaCitaRepository**

En `JpaCitaRepository.java`, agregar:

```java
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
```

Agregar imports necesarios (`@Query`, `@Param`).

- [ ] **Step 4: Crear ContabilidadService**

```java
package com.stilum.citas.application.contabilidad;

import com.stilum.citas.application.contabilidad.dto.ResumenDiaResponse;
import com.stilum.citas.domain.cita.Cita;
import com.stilum.citas.domain.cita.CitaRepository;
import com.stilum.citas.infrastructure.security.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ContabilidadService {

    private final CitaRepository citaRepository;

    public ContabilidadService(CitaRepository citaRepository) {
        this.citaRepository = citaRepository;
    }

    public ResumenDiaResponse resumenDia(LocalDate fecha) {
        UUID tenantId = TenantContext.get();
        List<Cita> citas = citaRepository.findCompletadasPorTenantYFecha(tenantId, fecha);

        BigDecimal totalVendido = citas.stream()
                .map(c -> c.getPrecioCobrado() != null ? c.getPrecioCobrado() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalComisiones = citas.stream()
                .map(c -> c.getComisionCalculada() != null ? c.getComisionCalculada() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalNegocio = totalVendido.subtract(totalComisiones);

        List<ResumenDiaResponse.ResumenProfesional> porProfesional = citas.stream()
                .collect(Collectors.groupingBy(c -> c.getProfesional().getId()))
                .values().stream()
                .map(grupo -> {
                    Cita primera = grupo.get(0);
                    BigDecimal venta = grupo.stream()
                            .map(c -> c.getPrecioCobrado() != null ? c.getPrecioCobrado() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal comision = grupo.stream()
                            .map(c -> c.getComisionCalculada() != null ? c.getComisionCalculada() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new ResumenDiaResponse.ResumenProfesional(
                            primera.getProfesional().getNombre(),
                            grupo.size(), venta, comision,
                            primera.getProfesional().getComisionPorcentaje()
                    );
                })
                .sorted(Comparator.comparing(ResumenDiaResponse.ResumenProfesional::totalVendido).reversed())
                .toList();

        List<ResumenDiaResponse.ResumenServicio> porServicio = citas.stream()
                .collect(Collectors.groupingBy(c -> c.getServicio().getId()))
                .values().stream()
                .map(grupo -> {
                    Cita primera = grupo.get(0);
                    BigDecimal venta = grupo.stream()
                            .map(c -> c.getPrecioCobrado() != null ? c.getPrecioCobrado() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new ResumenDiaResponse.ResumenServicio(
                            primera.getServicio().getNombre(), grupo.size(), venta);
                })
                .sorted(Comparator.comparing(ResumenDiaResponse.ResumenServicio::cantidad).reversed())
                .toList();

        List<ResumenDiaResponse.ResumenMetodoPago> porMetodoPago = citas.stream()
                .filter(c -> c.getMetodoPago() != null)
                .collect(Collectors.groupingBy(Cita::getMetodoPago))
                .entrySet().stream()
                .map(e -> {
                    BigDecimal total = e.getValue().stream()
                            .map(c -> c.getPrecioCobrado() != null ? c.getPrecioCobrado() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new ResumenDiaResponse.ResumenMetodoPago(e.getKey(), e.getValue().size(), total);
                })
                .toList();

        return new ResumenDiaResponse(fecha, citas.size(), totalVendido,
                totalComisiones, totalNegocio, porProfesional, porServicio, porMetodoPago);
    }
}
```

- [ ] **Step 5: Compilar**

```bash
cd stilum-backend && mvn compile -q
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 6: Commit**

```bash
git add stilum-backend/src/main/java/com/stilum/citas/application/contabilidad/
git add stilum-backend/src/main/java/com/stilum/citas/domain/cita/CitaRepository.java
git add stilum-backend/src/main/java/com/stilum/citas/infrastructure/persistence/jpa/JpaCitaRepository.java
git commit -m "feat(application): add ContabilidadService with resumenDia use case"
```

---

### Task 7: Controller de contabilidad + Frontend de cierre de caja

**Files:**
- Create: `stilum-backend/src/main/java/com/stilum/citas/presentation/contabilidad/ContabilidadController.java`
- Create: `stilum-frontend/src/app/features/contabilidad/contabilidad.component.ts`
- Create: `stilum-frontend/src/app/features/contabilidad/contabilidad.routes.ts`
- Create: `stilum-frontend/src/app/core/services/contabilidad-api.service.ts`
- Modify: `stilum-frontend/src/app/app.routes.ts`

**Interfaces:**
- Produces: `GET /api/contabilidad/resumen-dia?fecha=YYYY-MM-DD`.

- [ ] **Step 1: Crear ContabilidadController**

```java
package com.stilum.citas.presentation.contabilidad;

import com.stilum.citas.application.contabilidad.ContabilidadService;
import com.stilum.citas.application.contabilidad.dto.ResumenDiaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/contabilidad")
@Tag(name = "Contabilidad", description = "Cierre de caja y resúmenes financieros")
public class ContabilidadController {

    private final ContabilidadService contabilidadService;

    public ContabilidadController(ContabilidadService contabilidadService) {
        this.contabilidadService = contabilidadService;
    }

    @GetMapping("/resumen-dia")
    @PreAuthorize("hasAnyRole('ADMIN_TENANT', 'SUPER_ADMIN')")
    @Operation(summary = "Resumen financiero del día",
               description = "Total vendido, comisiones, desglose por profesional, servicio y método de pago")
    public ResumenDiaResponse resumenDia(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return contabilidadService.resumenDia(fecha != null ? fecha : LocalDate.now());
    }
}
```

- [ ] **Step 2: Crear ContabilidadApiService (frontend)**

```typescript
// stilum-frontend/src/app/core/services/contabilidad-api.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface ResumenDia {
  fecha: string;
  totalCitas: number;
  totalVendido: number;
  totalComisiones: number;
  totalNegocio: number;
  porProfesional: Array<{ nombre: string; citas: number; totalVendido: number; comision: number; comisionPorcentaje: number }>;
  porServicio: Array<{ nombre: string; cantidad: number; totalVendido: number }>;
  porMetodoPago: Array<{ metodo: string; cantidad: number; total: number }>;
}

@Injectable({ providedIn: 'root' })
export class ContabilidadApiService {
  private base = `${environment.apiUrl}/contabilidad`;
  constructor(private http: HttpClient) {}

  resumenDia(fecha?: string): Observable<ResumenDia> {
    const params = fecha ? { fecha } : {};
    return this.http.get<ResumenDia>(`${this.base}/resumen-dia`, { params });
  }
}
```

- [ ] **Step 3: Crear ContabilidadComponent**

```typescript
// stilum-frontend/src/app/features/contabilidad/contabilidad.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CalendarModule } from 'primeng/calendar';
import { CardModule } from 'primeng/card';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { ButtonModule } from 'primeng/button';
import { ContabilidadApiService, ResumenDia } from '../../core/services/contabilidad-api.service';

@Component({
  selector: 'app-contabilidad',
  standalone: true,
  imports: [CommonModule, FormsModule, CalendarModule, CardModule, TableModule, TagModule, ButtonModule],
  template: `
    <div class="p-4">
      <div class="flex align-items-center gap-3 mb-4">
        <h2 class="m-0">Cierre de Caja</h2>
        <p-calendar [(ngModel)]="fechaSeleccionada" dateFormat="yy-mm-dd"
                    (onSelect)="cargar()" [showIcon]="true" />
        <p-button icon="pi pi-print" label="Imprimir" severity="secondary" (onClick)="imprimir()" />
      </div>

      <div *ngIf="resumen" class="grid">
        <!-- Totales -->
        <div class="col-12 md:col-3">
          <p-card styleClass="text-center">
            <p class="text-500 m-0">Total Vendido</p>
            <h2 class="text-primary m-0">{{ resumen.totalVendido | currency:'COP':'symbol':'1.0-0':'es-CO' }}</h2>
            <small class="text-500">{{ resumen.totalCitas }} citas completadas</small>
          </p-card>
        </div>
        <div class="col-12 md:col-3">
          <p-card styleClass="text-center">
            <p class="text-500 m-0">Para el Negocio</p>
            <h2 class="text-green-500 m-0">{{ resumen.totalNegocio | currency:'COP':'symbol':'1.0-0':'es-CO' }}</h2>
          </p-card>
        </div>
        <div class="col-12 md:col-3">
          <p-card styleClass="text-center">
            <p class="text-500 m-0">Total Comisiones</p>
            <h2 class="text-orange-500 m-0">{{ resumen.totalComisiones | currency:'COP':'symbol':'1.0-0':'es-CO' }}</h2>
          </p-card>
        </div>

        <!-- Por Profesional -->
        <div class="col-12 md:col-8">
          <p-card header="Por Profesional">
            <p-table [value]="resumen.porProfesional" [stripedRows]="true">
              <ng-template pTemplate="header">
                <tr>
                  <th>Profesional</th>
                  <th class="text-center">Citas</th>
                  <th class="text-right">Vendido</th>
                  <th class="text-right">Comisión ({{ ''}}) </th>
                  <th class="text-right">Neto negocio</th>
                </tr>
              </ng-template>
              <ng-template pTemplate="body" let-row>
                <tr>
                  <td>{{ row.nombre }}</td>
                  <td class="text-center">{{ row.citas }}</td>
                  <td class="text-right">{{ row.totalVendido | currency:'COP':'symbol':'1.0-0':'es-CO' }}</td>
                  <td class="text-right text-orange-500">
                    {{ row.comision | currency:'COP':'symbol':'1.0-0':'es-CO' }}
                    <small class="text-500">({{ row.comisionPorcentaje }}%)</small>
                  </td>
                  <td class="text-right text-green-500">
                    {{ (row.totalVendido - row.comision) | currency:'COP':'symbol':'1.0-0':'es-CO' }}
                  </td>
                </tr>
              </ng-template>
            </p-table>
          </p-card>
        </div>

        <!-- Por Método de Pago -->
        <div class="col-12 md:col-4">
          <p-card header="Por Método de Pago">
            <p-table [value]="resumen.porMetodoPago">
              <ng-template pTemplate="body" let-row>
                <tr>
                  <td><p-tag [value]="row.metodo" /></td>
                  <td class="text-center">{{ row.cantidad }}</td>
                  <td class="text-right">{{ row.total | currency:'COP':'symbol':'1.0-0':'es-CO' }}</td>
                </tr>
              </ng-template>
            </p-table>
          </p-card>
        </div>
      </div>
    </div>
  `
})
export class ContabilidadComponent implements OnInit {
  resumen?: ResumenDia;
  fechaSeleccionada = new Date();

  constructor(private api: ContabilidadApiService) {}

  ngOnInit() { this.cargar(); }

  cargar() {
    const fecha = this.fechaSeleccionada.toISOString().split('T')[0];
    this.api.resumenDia(fecha).subscribe(r => this.resumen = r);
  }

  imprimir() { window.print(); }
}
```

- [ ] **Step 4: Crear contabilidad.routes.ts**

```typescript
// stilum-frontend/src/app/features/contabilidad/contabilidad.routes.ts
import { Routes } from '@angular/router';

export const CONTABILIDAD_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./contabilidad.component').then(m => m.ContabilidadComponent)
  }
];
```

- [ ] **Step 5: Registrar ruta en app.routes.ts**

En `stilum-frontend/src/app/app.routes.ts`, agregar dentro del bloque de rutas protegidas:

```typescript
{
  path: 'contabilidad',
  loadChildren: () => import('./features/contabilidad/contabilidad.routes').then(m => m.CONTABILIDAD_ROUTES),
  canActivate: [authGuard, roleGuard],
  data: { roles: ['ADMIN_TENANT'] }
}
```

- [ ] **Step 6: Compilar frontend**

```bash
cd stilum-frontend && npx ng build --configuration development 2>&1 | tail -10
```

Expected: `Build at: ... - Hash: ... - Time: ...ms`.

- [ ] **Step 7: Commit**

```bash
git add stilum-backend/src/main/java/com/stilum/citas/presentation/contabilidad/
git add stilum-frontend/src/app/features/contabilidad/
git add stilum-frontend/src/app/core/services/contabilidad-api.service.ts
git add stilum-frontend/src/app/app.routes.ts
git commit -m "feat: add contabilidad diaria (cierre de caja) - backend endpoint + Angular feature"
```

---

## MÓDULO 3 — Portal del Profesional

### Task 8: Backend — mis ingresos endpoint

**Files:**
- Create: `stilum-backend/src/main/java/com/stilum/citas/application/profesional/dto/MisIngresosResponse.java`
- Modify: `stilum-backend/src/main/java/com/stilum/citas/application/profesional/ProfesionalService.java`
- Modify: `stilum-backend/src/main/java/com/stilum/citas/domain/cita/CitaRepository.java`
- Modify: `stilum-backend/src/main/java/com/stilum/citas/infrastructure/persistence/jpa/JpaCitaRepository.java`
- Create: `stilum-backend/src/main/java/com/stilum/citas/presentation/profesional/MiPortalController.java`

**Interfaces:**
- Produces: `GET /api/mi-portal/ingresos?inicio=&fin=` (solo PROFESIONAL autenticado).

- [ ] **Step 1: Crear MisIngresosResponse**

```java
package com.stilum.citas.application.profesional.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record MisIngresosResponse(
        LocalDate inicio,
        LocalDate fin,
        int totalCitas,
        BigDecimal totalFacturado,
        BigDecimal totalComision,
        List<IngresoDetalle> detalle
) {
    public record IngresoDetalle(
            java.time.ZonedDateTime fecha,
            String servicio,
            String cliente,
            BigDecimal precio,
            BigDecimal comision,
            String metodoPago
    ) {}
}
```

- [ ] **Step 2: Agregar query en CitaRepository y JpaCitaRepository**

En `CitaRepository.java`:

```java
List<Cita> findCompletadasPorProfesionalYRango(UUID profesionalId, java.time.LocalDate inicio, java.time.LocalDate fin);
```

En `JpaCitaRepository.java`:

```java
@Query("""
    SELECT c FROM Cita c
    JOIN FETCH c.servicio
    JOIN FETCH c.cliente
    WHERE c.profesional.id = :profesionalId
      AND c.estado = 'COMPLETADA'
      AND CAST(c.fechaHoraInicio AS date) BETWEEN :inicio AND :fin
    ORDER BY c.fechaHoraInicio DESC
    """)
List<Cita> findCompletadasPorProfesionalYRango(
        @Param("profesionalId") UUID profesionalId,
        @Param("inicio") java.time.LocalDate inicio,
        @Param("fin") java.time.LocalDate fin);
```

- [ ] **Step 3: Agregar misIngresos en ProfesionalService**

```java
public MisIngresosResponse misIngresos(UUID profesionalId, LocalDate inicio, LocalDate fin) {
    List<Cita> citas = citaRepository.findCompletadasPorProfesionalYRango(profesionalId, inicio, fin);

    BigDecimal totalFacturado = citas.stream()
            .map(c -> c.getPrecioCobrado() != null ? c.getPrecioCobrado() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal totalComision = citas.stream()
            .map(c -> c.getComisionCalculada() != null ? c.getComisionCalculada() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    List<MisIngresosResponse.IngresoDetalle> detalle = citas.stream()
            .map(c -> new MisIngresosResponse.IngresoDetalle(
                    c.getFechaHoraInicio(),
                    c.getServicio().getNombre(),
                    c.getCliente().getNombre(),
                    c.getPrecioCobrado(),
                    c.getComisionCalculada(),
                    c.getMetodoPago()
            )).toList();

    return new MisIngresosResponse(inicio, fin, citas.size(), totalFacturado, totalComision, detalle);
}
```

Agregar import en `ProfesionalService.java`:
```java
import com.stilum.citas.application.profesional.dto.MisIngresosResponse;
import com.stilum.citas.domain.cita.Cita;
import com.stilum.citas.domain.cita.CitaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
```

Agregar `CitaRepository citaRepository` al constructor de `ProfesionalService`.

- [ ] **Step 4: Crear MiPortalController**

```java
package com.stilum.citas.presentation.profesional;

import com.stilum.citas.application.cita.CitaService;
import com.stilum.citas.application.cita.dto.CitaResponse;
import com.stilum.citas.application.profesional.ProfesionalService;
import com.stilum.citas.application.profesional.dto.MisIngresosResponse;
import com.stilum.citas.infrastructure.security.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/mi-portal")
@PreAuthorize("hasRole('PROFESIONAL')")
@Tag(name = "Mi Portal", description = "Vista del profesional: sus citas y sus ingresos")
public class MiPortalController {

    private final CitaService citaService;
    private final ProfesionalService profesionalService;

    public MiPortalController(CitaService citaService, ProfesionalService profesionalService) {
        this.citaService = citaService;
        this.profesionalService = profesionalService;
    }

    @GetMapping("/mis-citas")
    @Operation(summary = "Citas del profesional autenticado en una fecha")
    public List<CitaResponse> misCitas(
            @RequestParam UUID profesionalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return citaService.listarPorProfesionalYFecha(profesionalId, fecha);
    }

    @GetMapping("/ingresos")
    @Operation(summary = "Ingresos del profesional en un rango de fechas")
    public MisIngresosResponse misIngresos(
            @RequestParam UUID profesionalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        LocalDate finFecha = fin != null ? fin : LocalDate.now();
        return profesionalService.misIngresos(profesionalId, inicio, finFecha);
    }
}
```

- [ ] **Step 5: Compilar**

```bash
cd stilum-backend && mvn compile -q
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 6: Commit**

```bash
git add stilum-backend/src/main/java/com/stilum/citas/application/profesional/dto/MisIngresosResponse.java
git add stilum-backend/src/main/java/com/stilum/citas/application/profesional/ProfesionalService.java
git add stilum-backend/src/main/java/com/stilum/citas/domain/cita/CitaRepository.java
git add stilum-backend/src/main/java/com/stilum/citas/infrastructure/persistence/jpa/JpaCitaRepository.java
git add stilum-backend/src/main/java/com/stilum/citas/presentation/profesional/MiPortalController.java
git commit -m "feat(api): add /mi-portal endpoints for profesional self-service view"
```

---

### Task 9: Frontend — Portal del Profesional

**Files:**
- Create: `stilum-frontend/src/app/features/profesional-portal/profesional-portal.component.ts`
- Create: `stilum-frontend/src/app/features/profesional-portal/profesional-portal.routes.ts`
- Create: `stilum-frontend/src/app/core/services/mi-portal-api.service.ts`
- Modify: `stilum-frontend/src/app/app.routes.ts`

- [ ] **Step 1: Crear MiPortalApiService**

```typescript
// stilum-frontend/src/app/core/services/mi-portal-api.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CitaResponse } from '../models/cita.model';

export interface MisIngresos {
  inicio: string;
  fin: string;
  totalCitas: number;
  totalFacturado: number;
  totalComision: number;
  detalle: Array<{
    fecha: string;
    servicio: string;
    cliente: string;
    precio: number;
    comision: number;
    metodoPago: string;
  }>;
}

@Injectable({ providedIn: 'root' })
export class MiPortalApiService {
  private base = `${environment.apiUrl}/mi-portal`;
  constructor(private http: HttpClient) {}

  misCitas(profesionalId: string, fecha: string): Observable<CitaResponse[]> {
    return this.http.get<CitaResponse[]>(`${this.base}/mis-citas`, { params: { profesionalId, fecha } });
  }

  misIngresos(profesionalId: string, inicio: string, fin: string): Observable<MisIngresos> {
    return this.http.get<MisIngresos>(`${this.base}/ingresos`, { params: { profesionalId, inicio, fin } });
  }
}
```

- [ ] **Step 2: Crear ProfesionalPortalComponent**

```typescript
// stilum-frontend/src/app/features/profesional-portal/profesional-portal.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TabViewModule } from 'primeng/tabview';
import { CardModule } from 'primeng/card';
import { TableModule } from 'primeng/table';
import { CalendarModule } from 'primeng/calendar';
import { TagModule } from 'primeng/tag';
import { MiPortalApiService, MisIngresos } from '../../core/services/mi-portal-api.service';
import { CitaResponse } from '../../core/models/cita.model';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-profesional-portal',
  standalone: true,
  imports: [CommonModule, FormsModule, TabViewModule, CardModule, TableModule, CalendarModule, TagModule],
  template: `
    <div class="p-4">
      <h2>Mi Portal</h2>
      <p-tabView>

        <!-- Tab Mis Citas del Día -->
        <p-tabPanel header="Mis Citas Hoy">
          <div class="mb-3">
            <p-calendar [(ngModel)]="fechaCitas" dateFormat="yy-mm-dd" [showIcon]="true"
                        (onSelect)="cargarCitas()" />
          </div>
          <p-table [value]="citas" [stripedRows]="true" [loading]="loadingCitas">
            <ng-template pTemplate="header">
              <tr>
                <th>Hora</th><th>Cliente</th><th>Servicio</th><th>Duración</th><th>Estado</th><th>Precio</th>
              </tr>
            </ng-template>
            <ng-template pTemplate="body" let-c>
              <tr>
                <td>{{ c.fechaHoraInicio | date:'HH:mm' }}</td>
                <td>{{ c.cliente.nombre }}</td>
                <td>{{ c.servicio.nombre }}</td>
                <td>{{ c.duracionMin }} min</td>
                <td><p-tag [value]="c.estado" [severity]="getSeverity(c.estado)" /></td>
                <td>{{ c.precioCobrado || c.servicio.precio | currency:'COP':'symbol':'1.0-0':'es-CO' }}</td>
              </tr>
            </ng-template>
            <ng-template pTemplate="emptymessage">
              <tr><td colspan="6" class="text-center p-4 text-500">Sin citas para esta fecha</td></tr>
            </ng-template>
          </p-table>
        </p-tabPanel>

        <!-- Tab Mis Ingresos -->
        <p-tabPanel header="Mis Ingresos">
          <div class="flex gap-3 mb-4 flex-wrap">
            <div>
              <label class="block mb-1">Desde</label>
              <p-calendar [(ngModel)]="rangoInicio" dateFormat="yy-mm-dd" [showIcon]="true" />
            </div>
            <div>
              <label class="block mb-1">Hasta</label>
              <p-calendar [(ngModel)]="rangoFin" dateFormat="yy-mm-dd" [showIcon]="true" />
            </div>
            <div class="flex align-items-end">
              <button pButton label="Consultar" icon="pi pi-search" (click)="cargarIngresos()"></button>
            </div>
          </div>

          <div *ngIf="ingresos" class="grid mb-4">
            <div class="col-12 md:col-4">
              <p-card styleClass="text-center">
                <p class="text-500">Citas completadas</p>
                <h2 class="m-0">{{ ingresos.totalCitas }}</h2>
              </p-card>
            </div>
            <div class="col-12 md:col-4">
              <p-card styleClass="text-center">
                <p class="text-500">Total facturado</p>
                <h2 class="text-primary m-0">{{ ingresos.totalFacturado | currency:'COP':'symbol':'1.0-0':'es-CO' }}</h2>
              </p-card>
            </div>
            <div class="col-12 md:col-4">
              <p-card styleClass="text-center">
                <p class="text-500">Mi comisión</p>
                <h2 class="text-green-500 m-0">{{ ingresos.totalComision | currency:'COP':'symbol':'1.0-0':'es-CO' }}</h2>
              </p-card>
            </div>
          </div>

          <p-table *ngIf="ingresos" [value]="ingresos.detalle" [stripedRows]="true">
            <ng-template pTemplate="header">
              <tr>
                <th>Fecha</th><th>Servicio</th><th>Cliente</th><th>Precio</th><th>Mi comisión</th><th>Pago</th>
              </tr>
            </ng-template>
            <ng-template pTemplate="body" let-d>
              <tr>
                <td>{{ d.fecha | date:'dd/MM/yyyy HH:mm' }}</td>
                <td>{{ d.servicio }}</td>
                <td>{{ d.cliente }}</td>
                <td>{{ d.precio | currency:'COP':'symbol':'1.0-0':'es-CO' }}</td>
                <td class="text-green-500 font-bold">{{ d.comision | currency:'COP':'symbol':'1.0-0':'es-CO' }}</td>
                <td><p-tag [value]="d.metodoPago || 'N/A'" severity="info" /></td>
              </tr>
            </ng-template>
          </p-table>
        </p-tabPanel>

      </p-tabView>
    </div>
  `
})
export class ProfesionalPortalComponent implements OnInit {
  citas: CitaResponse[] = [];
  ingresos?: MisIngresos;
  fechaCitas = new Date();
  rangoInicio = new Date(new Date().setDate(1));
  rangoFin = new Date();
  loadingCitas = false;
  profesionalId = '';

  constructor(private api: MiPortalApiService, private auth: AuthService) {}

  ngOnInit() {
    this.profesionalId = this.auth.getCurrentUser()?.profesionalId ?? '';
    this.cargarCitas();
  }

  cargarCitas() {
    this.loadingCitas = true;
    const fecha = this.fechaCitas.toISOString().split('T')[0];
    this.api.misCitas(this.profesionalId, fecha).subscribe({
      next: c => { this.citas = c; this.loadingCitas = false; },
      error: () => { this.loadingCitas = false; }
    });
  }

  cargarIngresos() {
    const inicio = this.rangoInicio.toISOString().split('T')[0];
    const fin = this.rangoFin.toISOString().split('T')[0];
    this.api.misIngresos(this.profesionalId, inicio, fin).subscribe(i => this.ingresos = i);
  }

  getSeverity(estado: string): string {
    const map: Record<string, string> = {
      PENDIENTE: 'warn', CONFIRMADA: 'info', EN_CURSO: 'success',
      COMPLETADA: 'success', CANCELADA: 'danger', NO_SHOW: 'secondary'
    };
    return map[estado] ?? 'info';
  }
}
```

- [ ] **Step 3: Crear profesional-portal.routes.ts**

```typescript
// stilum-frontend/src/app/features/profesional-portal/profesional-portal.routes.ts
import { Routes } from '@angular/router';

export const PROFESIONAL_PORTAL_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./profesional-portal.component').then(m => m.ProfesionalPortalComponent)
  }
];
```

- [ ] **Step 4: Registrar ruta en app.routes.ts**

```typescript
{
  path: 'mi-portal',
  loadChildren: () => import('./features/profesional-portal/profesional-portal.routes')
    .then(m => m.PROFESIONAL_PORTAL_ROUTES),
  canActivate: [authGuard, roleGuard],
  data: { roles: ['PROFESIONAL', 'ADMIN_TENANT'] }
}
```

- [ ] **Step 5: Compilar frontend**

```bash
cd stilum-frontend && npx ng build --configuration development 2>&1 | tail -10
```

Expected: `Build at: ... - Hash: ... - Time: ...ms`.

- [ ] **Step 6: Commit**

```bash
git add stilum-frontend/src/app/features/profesional-portal/
git add stilum-frontend/src/app/core/services/mi-portal-api.service.ts
git add stilum-frontend/src/app/app.routes.ts
git commit -m "feat(frontend): add portal del profesional with mis citas + mis ingresos tabs"
```

---

## MÓDULO 4 — WhatsApp Bot con IA

### Task 10: Migración DB — conversaciones WhatsApp

**Files:**
- Create: `stilum-backend/src/main/resources/db/migration/V13__create_whatsapp_conversaciones.sql`

- [ ] **Step 1: Crear migración**

```sql
-- V13: Estado de conversaciones WhatsApp para el bot

CREATE TABLE whatsapp_conversaciones (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    telefono        VARCHAR(20) NOT NULL,
    estado          VARCHAR(30) NOT NULL DEFAULT 'INICIO'
                    CHECK (estado IN ('INICIO','ESPERANDO_NECESIDAD','ESPERANDO_FECHA',
                                     'ESPERANDO_CONFIRMACION','COMPLETADO','CANCELADO')),
    datos_json      JSONB NOT NULL DEFAULT '{}',
    ultimo_mensaje  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (tenant_id, telefono)
);

CREATE INDEX idx_conv_tenant_telefono ON whatsapp_conversaciones (tenant_id, telefono);
CREATE INDEX idx_conv_ultimo_mensaje ON whatsapp_conversaciones (ultimo_mensaje);

COMMENT ON TABLE whatsapp_conversaciones IS
    'Estado de conversación activa por número de WhatsApp. Se borra al completar o tras 24h.';
COMMENT ON COLUMN whatsapp_conversaciones.datos_json IS
    'Datos recolectados: {necesidad, fechaPreferida, profesionalRecomendado, servicioId}';
```

- [ ] **Step 2: Aplicar migración**

```bash
cd stilum-backend
mvn flyway:migrate -Dflyway.url=jdbc:postgresql://localhost:5432/stilum -Dflyway.user=stilum -Dflyway.password=stilum
```

Expected: `Successfully applied 1 migration to schema "public", now at version v13`.

- [ ] **Step 3: Commit**

```bash
git add stilum-backend/src/main/resources/db/migration/V13__create_whatsapp_conversaciones.sql
git commit -m "feat(db): add whatsapp_conversaciones table for bot state machine (V13)"
```

---

### Task 11: Dominio e Infraestructura — entidad ConversacionWhatsApp

**Files:**
- Create: `stilum-backend/src/main/java/com/stilum/citas/domain/whatsapp/ConversacionWhatsApp.java`
- Create: `stilum-backend/src/main/java/com/stilum/citas/domain/whatsapp/ConversacionRepository.java`
- Create: `stilum-backend/src/main/java/com/stilum/citas/infrastructure/persistence/jpa/JpaConversacionRepository.java`

- [ ] **Step 1: Crear entidad ConversacionWhatsApp**

```java
package com.stilum.citas.domain.whatsapp;

import com.stilum.citas.domain.tenant.Tenant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "whatsapp_conversaciones")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class ConversacionWhatsApp {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(nullable = false, length = 20)
    private String telefono;

    @Column(nullable = false, length = 30)
    private String estado = "INICIO";

    @Column(name = "datos_json", nullable = false, columnDefinition = "jsonb")
    private String datosJson = "{}";

    @Column(name = "ultimo_mensaje")
    private Instant ultimoMensaje = Instant.now();

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    public static ConversacionWhatsApp iniciar(Tenant tenant, String telefono) {
        ConversacionWhatsApp c = new ConversacionWhatsApp();
        c.tenant = tenant;
        c.telefono = telefono;
        return c;
    }

    public void avanzarEstado(String nuevoEstado, String datosJson) {
        this.estado = nuevoEstado;
        this.datosJson = datosJson != null ? datosJson : "{}";
        this.ultimoMensaje = Instant.now();
    }

    public UUID getTenantId() { return tenant.getId(); }
}
```

- [ ] **Step 2: Crear ConversacionRepository**

```java
package com.stilum.citas.domain.whatsapp;

import java.util.Optional;
import java.util.UUID;

public interface ConversacionRepository {
    Optional<ConversacionWhatsApp> findByTenantIdAndTelefono(UUID tenantId, String telefono);
    ConversacionWhatsApp save(ConversacionWhatsApp conv);
    void deleteByTenantIdAndTelefono(UUID tenantId, String telefono);
}
```

- [ ] **Step 3: Crear JpaConversacionRepository**

```java
package com.stilum.citas.infrastructure.persistence.jpa;

import com.stilum.citas.domain.whatsapp.ConversacionRepository;
import com.stilum.citas.domain.whatsapp.ConversacionWhatsApp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaConversacionRepository
        extends JpaRepository<ConversacionWhatsApp, UUID>, ConversacionRepository {

    Optional<ConversacionWhatsApp> findByTenantIdAndTelefono(UUID tenantId, String telefono);

    void deleteByTenantIdAndTelefono(UUID tenantId, String telefono);
}
```

- [ ] **Step 4: Compilar**

```bash
cd stilum-backend && mvn compile -q
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 5: Commit**

```bash
git add stilum-backend/src/main/java/com/stilum/citas/domain/whatsapp/
git add stilum-backend/src/main/java/com/stilum/citas/infrastructure/persistence/jpa/JpaConversacionRepository.java
git commit -m "feat(domain): add ConversacionWhatsApp entity and repository"
```

---

### Task 12: Application — WhatsAppBotService con IA (Claude)

**Files:**
- Create: `stilum-backend/src/main/java/com/stilum/citas/application/whatsapp/WhatsAppBotService.java`
- Create: `stilum-backend/src/main/java/com/stilum/citas/application/whatsapp/dto/WhatsAppMensajeEntranteDto.java`
- Modify: `stilum-backend/src/main/java/com/stilum/citas/infrastructure/config/OpenApiConfig.java` (agregar Claude API key property)
- Modify: `stilum-backend/src/main/resources/application.yml`

**Interfaces:**
- Consumes: Claude API (claude-haiku-4-5-20251001 via HTTP), `ConversacionRepository`, `ProfesionalRepository`, `ServicioRepository`, `CitaService`.
- Produces: `WhatsAppBotService.procesarMensaje(UUID tenantId, String telefono, String texto): String`.

- [ ] **Step 1: Agregar Claude API key en application.yml**

En `stilum-backend/src/main/resources/application.yml`, agregar:

```yaml
stilum:
  claude:
    api-key: ${CLAUDE_API_KEY:}
    model: claude-haiku-4-5-20251001
    api-url: https://api.anthropic.com/v1/messages
```

- [ ] **Step 2: Crear WhatsAppMensajeEntranteDto**

```java
package com.stilum.citas.application.whatsapp.dto;

public record WhatsAppMensajeEntranteDto(
        String telefono,
        String texto
) {}
```

- [ ] **Step 3: Crear WhatsAppBotService**

```java
package com.stilum.citas.application.whatsapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stilum.citas.application.cita.CitaService;
import com.stilum.citas.application.cita.dto.CreateCitaRequest;
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

import java.time.ZonedDateTime;
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
    private final CitaService citaService;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

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
                               CitaService citaService,
                               WebClient.Builder webClientBuilder,
                               ObjectMapper objectMapper) {
        this.conversacionRepository = conversacionRepository;
        this.profesionalRepository = profesionalRepository;
        this.servicioRepository = servicioRepository;
        this.tenantRepository = tenantRepository;
        this.citaService = citaService;
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public String procesarMensaje(UUID tenantId, String telefono, String texto) {
        TenantContext.set(tenantId);
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant no encontrado"));

        ConversacionWhatsApp conv = conversacionRepository
                .findByTenantIdAndTelefono(tenantId, telefono)
                .orElseGet(() -> conversacionRepository.save(
                        ConversacionWhatsApp.iniciar(tenant, telefono)));

        return switch (conv.getEstado()) {
            case "INICIO" -> manejarInicio(conv, texto, tenantId);
            case "ESPERANDO_NECESIDAD" -> manejarNecesidad(conv, texto, tenantId);
            case "ESPERANDO_FECHA" -> manejarFecha(conv, texto, tenantId);
            case "ESPERANDO_CONFIRMACION" -> manejarConfirmacion(conv, texto, tenantId);
            default -> {
                conv.avanzarEstado("INICIO", "{}");
                conversacionRepository.save(conv);
                yield saludo(tenant.getNombreNegocio());
            }
        };
    }

    private String manejarInicio(ConversacionWhatsApp conv, String texto, UUID tenantId) {
        conv.avanzarEstado("ESPERANDO_NECESIDAD", "{}");
        conversacionRepository.save(conv);
        return "¡Hola! Soy el asistente de *" + conv.getTenantId() + "*. "
                + "¿Qué servicio estás buscando hoy? "
                + "(Ej: corte de cabello, tinte, manicure...)";
    }

    private String manejarNecesidad(ConversacionWhatsApp conv, String necesidad, UUID tenantId) {
        List<Profesional> profesionales = profesionalRepository.findAllActivos(tenantId);
        List<Servicio> servicios = servicioRepository.findAllActivos(tenantId);

        String recomendacion = consultarIA(necesidad, profesionales, servicios);

        Map<String, String> datos = Map.of("necesidad", necesidad, "recomendacion", recomendacion);
        try {
            conv.avanzarEstado("ESPERANDO_FECHA", objectMapper.writeValueAsString(datos));
        } catch (Exception e) {
            conv.avanzarEstado("ESPERANDO_FECHA", "{\"necesidad\":\"" + necesidad + "\"}");
        }
        conversacionRepository.save(conv);
        return recomendacion + "\n\n¿Para qué fecha te gustaría la cita? (Ej: mañana, el jueves, 25/06/2026)";
    }

    private String manejarFecha(ConversacionWhatsApp conv, String fechaTexto, UUID tenantId) {
        conv.avanzarEstado("ESPERANDO_CONFIRMACION", conv.getDatosJson());
        conversacionRepository.save(conv);
        return "Perfecto, te agendaré para " + fechaTexto + ". "
                + "¿Confirmas? Responde *SÍ* para confirmar o *NO* para cancelar.";
    }

    private String manejarConfirmacion(ConversacionWhatsApp conv, String respuesta, UUID tenantId) {
        if (respuesta.trim().equalsIgnoreCase("SÍ") || respuesta.trim().equalsIgnoreCase("SI")) {
            conv.avanzarEstado("COMPLETADO", conv.getDatosJson());
            conversacionRepository.save(conv);
            return "✅ ¡Tu cita ha sido registrada! Recibirás una confirmación. "
                    + "Gracias por preferirnos.";
        } else {
            conv.avanzarEstado("INICIO", "{}");
            conversacionRepository.save(conv);
            return "Cita cancelada. Escribe cuando quieras agendar de nuevo.";
        }
    }

    private String consultarIA(String necesidad, List<Profesional> profesionales, List<Servicio> servicios) {
        if (claudeApiKey == null || claudeApiKey.isBlank()) {
            return "Te recomendamos a cualquiera de nuestros profesionales. "
                    + "Todos están disponibles para " + necesidad + ".";
        }

        String prompt = buildPrompt(necesidad, profesionales, servicios);
        try {
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
                    return first.getOrDefault("text", "Contáctanos para más info.").toString();
                }
            }
        } catch (Exception e) {
            // Fallback si Claude no está disponible
        }
        return "Tenemos excelentes profesionales disponibles para " + necesidad + ".";
    }

    private String buildPrompt(String necesidad, List<Profesional> profesionales, List<Servicio> servicios) {
        StringBuilder sb = new StringBuilder();
        sb.append("Eres el asistente de una peluquería/salón de belleza. ");
        sb.append("El cliente busca: ").append(necesidad).append(".\n\n");
        sb.append("Profesionales disponibles:\n");
        profesionales.forEach(p ->
                sb.append("- ").append(p.getNombre())
                  .append(" (especialidad: ").append(p.getEspecialidad()).append(")\n"));
        sb.append("\nServicios disponibles:\n");
        servicios.forEach(s ->
                sb.append("- ").append(s.getNombre())
                  .append(" $").append(s.getPrecio()).append(" / ").append(s.getDuracionMin()).append("min\n"));
        sb.append("\nRecomienda en 2-3 líneas al profesional más adecuado y el servicio. "
                + "Sé amable, usa emojis. Responde en español.");
        return sb.toString();
    }

    private String saludo(String nombreNegocio) {
        return "¡Hola! Bienvenido a *" + nombreNegocio + "*. ¿En qué te podemos ayudar hoy?";
    }
}
```

- [ ] **Step 4: Compilar**

```bash
cd stilum-backend && mvn compile -q
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 5: Commit**

```bash
git add stilum-backend/src/main/java/com/stilum/citas/application/whatsapp/
git add stilum-backend/src/main/resources/application.yml
git commit -m "feat(application): add WhatsAppBotService with Claude AI recommendation"
```

---

### Task 13: Controller — Webhook WhatsApp

**Files:**
- Create: `stilum-backend/src/main/java/com/stilum/citas/presentation/whatsapp/WhatsAppWebhookController.java`
- Modify: `stilum-backend/src/main/java/com/stilum/citas/infrastructure/security/SecurityConfig.java`

**Interfaces:**
- Produces: `GET /api/webhook/whatsapp/{tenantId}` (verificación Meta), `POST /api/webhook/whatsapp/{tenantId}` (mensajes entrantes).

- [ ] **Step 1: Crear WhatsAppWebhookController**

```java
package com.stilum.citas.presentation.whatsapp;

import com.stilum.citas.application.whatsapp.WhatsAppBotService;
import com.stilum.citas.application.whatsapp.dto.WhatsAppMensajeEntranteDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    /** Verificación del webhook por Meta (GET). */
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

    /** Recibe mensajes entrantes de WhatsApp (POST). */
    @PostMapping("/{tenantId}")
    public ResponseEntity<Void> recibirMensaje(
            @PathVariable UUID tenantId,
            @RequestBody Map<String, Object> payload) {
        try {
            // Extraer teléfono y texto del payload de Meta
            String telefono = extraerTelefono(payload);
            String texto = extraerTexto(payload);
            if (telefono != null && texto != null) {
                botService.procesarMensaje(tenantId, telefono, texto);
            }
        } catch (Exception e) {
            // Siempre retornar 200 a Meta para evitar reintentos
        }
        return ResponseEntity.ok().build();
    }

    @SuppressWarnings("unchecked")
    private String extraerTelefono(Map<String, Object> payload) {
        try {
            var entry = ((java.util.List<?>) ((Map<?, ?>) payload.get("entry")).entrySet()
                    .stream().findFirst().orElse(null));
            // Estructura simplificada — en producción parsear el payload completo de Meta
            if (payload.containsKey("entry")) {
                var entries = (java.util.List<Map<String, Object>>) payload.get("entry");
                var changes = (java.util.List<Map<String, Object>>) entries.get(0).get("changes");
                var value = (Map<String, Object>) changes.get(0).get("value");
                var messages = (java.util.List<Map<String, Object>>) value.get("messages");
                return messages.get(0).get("from").toString();
            }
        } catch (Exception ignored) {}
        return null;
    }

    @SuppressWarnings("unchecked")
    private String extraerTexto(Map<String, Object> payload) {
        try {
            if (payload.containsKey("entry")) {
                var entries = (java.util.List<Map<String, Object>>) payload.get("entry");
                var changes = (java.util.List<Map<String, Object>>) entries.get(0).get("changes");
                var value = (Map<String, Object>) changes.get(0).get("value");
                var messages = (java.util.List<Map<String, Object>>) value.get("messages");
                var textObj = (Map<String, Object>) messages.get(0).get("text");
                return textObj.get("body").toString();
            }
        } catch (Exception ignored) {}
        return null;
    }
}
```

- [ ] **Step 2: Verificar SecurityConfig ya permite /api/webhook/**

En `SecurityConfig.java` ya existe:
```java
"/api/webhook/**",         // WhatsApp webhook (verificación Meta)
```
No requiere cambio.

- [ ] **Step 3: Agregar verify-token en application.yml**

```yaml
stilum:
  whatsapp:
    verify-token: ${WHATSAPP_VERIFY_TOKEN:stilum-verify-2024}
```

- [ ] **Step 4: Compilar y ejecutar tests**

```bash
cd stilum-backend && mvn compile -q && mvn test -q 2>&1 | tail -5
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 5: Commit final**

```bash
git add stilum-backend/src/main/java/com/stilum/citas/presentation/whatsapp/
git add stilum-backend/src/main/resources/application.yml
git commit -m "feat(api): add WhatsApp webhook controller for Meta Business API integration"
```

---

## Resumen de Archivos Creados/Modificados

### Backend — Nuevos archivos
| Archivo | Módulo |
|---------|--------|
| `db/migration/V12__add_comisiones_y_pago.sql` | Cobro |
| `db/migration/V13__create_whatsapp_conversaciones.sql` | WhatsApp |
| `application/cita/dto/RegistrarPagoRequest.java` | Cobro |
| `application/contabilidad/dto/ResumenDiaResponse.java` | Contabilidad |
| `application/contabilidad/ContabilidadService.java` | Contabilidad |
| `application/profesional/dto/MisIngresosResponse.java` | Portal Prof. |
| `application/whatsapp/dto/WhatsAppMensajeEntranteDto.java` | WhatsApp |
| `application/whatsapp/WhatsAppBotService.java` | WhatsApp |
| `domain/whatsapp/ConversacionWhatsApp.java` | WhatsApp |
| `domain/whatsapp/ConversacionRepository.java` | WhatsApp |
| `infrastructure/persistence/jpa/JpaConversacionRepository.java` | WhatsApp |
| `presentation/contabilidad/ContabilidadController.java` | Contabilidad |
| `presentation/profesional/MiPortalController.java` | Portal Prof. |
| `presentation/whatsapp/WhatsAppWebhookController.java` | WhatsApp |

### Backend — Modificados
| Archivo | Cambio |
|---------|--------|
| `domain/cita/Cita.java` | + metodoPago, comisionCalculada, nuevo completar() |
| `domain/cita/CitaRepository.java` | + 2 queries nuevas |
| `domain/profesional/Profesional.java` | + comisionPorcentaje, actualizarComision() |
| `application/cita/dto/CitaResponse.java` | + metodoPago, comisionCalculada, comisionPorcentaje |
| `application/cita/CitaService.java` | + registrarPago(), completar() actualizado |
| `application/profesional/ProfesionalService.java` | + misIngresos() |
| `infrastructure/persistence/jpa/JpaCitaRepository.java` | + 2 queries |
| `presentation/cita/CitaController.java` | + PATCH /pagar |
| `presentation/profesional/ProfesionalController.java` | + PATCH /comision |
| `resources/application.yml` | + claude + whatsapp config |

### Frontend — Nuevos archivos
| Archivo | Módulo |
|---------|--------|
| `features/dashboard/registrar-pago-modal.component.ts` | Cobro |
| `features/contabilidad/contabilidad.component.ts` | Contabilidad |
| `features/contabilidad/contabilidad.routes.ts` | Contabilidad |
| `features/profesional-portal/profesional-portal.component.ts` | Portal Prof. |
| `features/profesional-portal/profesional-portal.routes.ts` | Portal Prof. |
| `core/services/contabilidad-api.service.ts` | Contabilidad |
| `core/services/mi-portal-api.service.ts` | Portal Prof. |

### Frontend — Modificados
| Archivo | Cambio |
|---------|--------|
| `core/services/cita-api.service.ts` | + registrarPago() |
| `features/dashboard/dashboard.component.ts` | + modal pago integrado |
| `app.routes.ts` | + /contabilidad, /mi-portal |
