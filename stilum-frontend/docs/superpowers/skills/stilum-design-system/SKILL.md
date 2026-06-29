---
name: stilum-design-system
description: >
  Sistema de diseño de Stilum Frontend — tokens CSS y 22 componentes compartidos (`st-*`).
  Usa esta skill SIEMPRE que vayas a implementar o modificar cualquier elemento de UI en stilum-frontend:
  nuevas páginas, formularios, tablas, modales, badges, botones, inputs, textareas, checkboxes, toggles,
  tabs, paginación, búsqueda, avatares, stat cards, progress bars, chips, alerts, spinners, skeletons,
  empty states o confirm dialogs. Si añades HTML o CSS nuevo, consulta esta skill primero.
  No usar los componentes centralizados genera inconsistencias visuales, deuda técnica y rompe dark mode.
---

# Sistema de Diseño Stilum

Este documento es la fuente de verdad para cualquier trabajo de UI en `stilum-frontend`.
Antes de escribir CSS inline, crear un `<button>` nativo, o hardcodear un color hex, lee esta skill.

## Por qué centralizar los componentes

**Problema que resuelven los componentes `st-*`:**

Cuando cada feature implementa su propio botón, input o badge, el resultado es:
- Inconsistencia visual entre páginas
- Dark mode roto (los colores hardcodeados no cambian con el tema)
- Mantenimiento multiplicado: cambiar un radio o color requiere editar N archivos
- Bundles más grandes (cada feature duplica estilos)

**Regla de oro:** Si existe un componente `st-*` para lo que necesitas, úsalo. Nunca reimplementes
lo que ya existe en `src/app/shared/ui/`.

---

## Tokens CSS — Variables Globales

Todos los estilos deben usar las CSS custom properties definidas en `src/styles/`. Nunca hardcodear
valores de color, spacing, radio, fuente, duración o sombra.

### Colores (`src/styles/_themes.scss`)

El tema se aplica en `[data-theme='light']` / `[data-theme='dark']` sobre `<html>`. Al usar `var(--...)`,
el componente responde automáticamente al cambio de tema sin código adicional.

**Acento principal:**
```css
var(--color-accent)          /* #E85D4A coral — CTAs, links, activos */
var(--color-accent-light)    /* hover state */
var(--color-accent-dark)     /* pressed state */
var(--color-accent-subtle)   /* fondo sutil de elemento seleccionado */
var(--color-accent-text)     /* texto sobre fondo accent (#fff) */
```

**Fondos:**
```css
var(--color-bg-page)         /* fondo de la aplicación */
var(--color-bg-surface)      /* header, panels */
var(--color-bg-card)         /* tarjetas, dropdowns */
var(--color-bg-elevated)     /* thead, chips, elementos elevados */
var(--color-bg-sidebar)      /* barra lateral */
var(--color-bg-input)        /* fondo de inputs */
var(--color-bg-hover)        /* hover de filas, nav items */
```

**Texto:**
```css
var(--color-text-1)          /* texto principal */
var(--color-text-2)          /* texto secundario / .text-muted */
var(--color-text-3)          /* texto placeholder, deshabilitado */
var(--color-text-on-accent)  /* texto sobre botón primary (#fff) */
var(--color-text-sidebar)    /* nav items inactivos (rgba blanco) */
var(--color-text-sidebar-active) /* nav items activos (#fff) */
```

**Bordes:**
```css
var(--color-border)          /* separadores, bordes de card */
var(--color-border-strong)   /* bordes con más énfasis */
var(--color-border-focus)    /* ring de focus en inputs (coral) */
var(--color-border-input)    /* borde normal de inputs */
```

**Semánticos:**
```css
var(--color-success)   var(--color-success-bg)
var(--color-warning)   var(--color-warning-bg)
var(--color-danger)    var(--color-danger-bg)
var(--color-info)      var(--color-info-bg)
```

**Estados de cita (dominio Stilum):**
```css
var(--cita-pendiente-bg)   var(--cita-pendiente-text)
var(--cita-confirmada-bg)  var(--cita-confirmada-text)
var(--cita-en-curso-bg)    var(--cita-en-curso-text)
var(--cita-completada-bg)  var(--cita-completada-text)
var(--cita-cancelada-bg)   var(--cita-cancelada-text)
var(--cita-no-show-bg)     var(--cita-no-show-text)
```

**Sombras:**
```css
var(--shadow-xs)    /* 1px */
var(--shadow-sm)    /* 4px */
var(--shadow-md)    /* 16px */
var(--shadow-lg)    /* 32px */
var(--shadow-card)  /* para tarjetas */
var(--shadow-modal) /* para modales */
```

---

### Tipografía (`src/styles/_typography.scss`)

```css
/* Familias */
var(--font-display)   /* Fraunces — h1, h2, h3, logos, citas */
var(--font-body)      /* DM Sans — body, labels, UI general */
var(--font-mono)      /* JetBrains Mono — código */

/* Pesos */
var(--font-light)     /* 300 */
var(--font-regular)   /* 400 */
var(--font-medium)    /* 500 */
var(--font-semibold)  /* 600 */
var(--font-bold)      /* 700 */

/* Tamaños */
var(--text-xs)    /* 0.75rem / 12px */
var(--text-sm)    /* 0.875rem / 14px */
var(--text-base)  /* 1rem / 16px */
var(--text-lg)    /* 1.125rem / 18px */
var(--text-xl)    /* 1.25rem / 20px */
var(--text-2xl)   /* 1.5rem / 24px */
var(--text-3xl)   /* 1.875rem / 30px */
var(--text-4xl)   /* 2.25rem / 36px */

/* Line-height */
var(--leading-tight)    /* 1.2 */
var(--leading-snug)     /* 1.35 */
var(--leading-normal)   /* 1.5 */
var(--leading-relaxed)  /* 1.65 */

/* Letter-spacing */
var(--tracking-tight)   /* -0.02em — títulos */
var(--tracking-normal)  /* 0 */
var(--tracking-wide)    /* 0.04em — badges uppercase */
var(--tracking-wider)   /* 0.08em */
```

---

### Espaciado y Radio (`src/styles/_spacing.scss` + `_typography.scss`)

Sistema basado en grid de 4px. **Nunca usar `px` hardcodeado.**

```css
var(--space-1)   /* 4px  */    var(--space-2)   /* 8px  */
var(--space-3)   /* 12px */    var(--space-4)   /* 16px */
var(--space-5)   /* 20px */    var(--space-6)   /* 24px */
var(--space-8)   /* 32px */    var(--space-10)  /* 40px */
var(--space-12)  /* 48px */    var(--space-16)  /* 64px */

var(--radius-sm)    /* 4px  */    var(--radius-md)   /* 8px  */
var(--radius-lg)    /* 12px */    var(--radius-xl)   /* 16px */
var(--radius-2xl)   /* 24px */    var(--radius-full) /* 9999px — pills */
```

### Duración y Easing (`src/styles/_typography.scss`)

```css
var(--duration-fast)    /* 120ms — hover, color */
var(--duration-normal)  /* 200ms — slide, collapse */
var(--duration-slow)    /* 350ms — animaciones complejas */
var(--easing-default)   /* cubic-bezier(0.4, 0, 0.2, 1) */
var(--easing-spring)    /* cubic-bezier(0.34, 1.56, 0.64, 1) — pop */
```

---

## Componentes Compartidos — `src/app/shared/ui/`

### Cómo importar

Todos los componentes se exportan desde el barrel `src/app/shared/ui/index.ts`:

```typescript
import {
  // Inputs de formulario
  StButtonComponent,
  StInputComponent,
  StSelectComponent,
  StTextareaComponent,
  StCheckboxComponent,
  StToggleComponent,
  StSearchInputComponent,
  // Feedback y estado
  StAlertComponent,
  StBadgeComponent,
  StChipComponent,
  StEmptyStateComponent,
  StSpinnerComponent,
  StSkeletonComponent,
  // Datos y métricas
  StAvatarComponent,
  StStatCardComponent,
  StProgressBarComponent,
  // Navegación
  StTabsComponent,
  StPaginatorComponent,
  // Overlays
  StModalComponent,
  StConfirmDialogComponent,
  StToastComponent,
  ToastService,
  // Tipos
  type SelectOption,
  type BadgeVariant,
  type StTab,
  type ToastMessage
} from '../../../shared/ui';
```

Ajusta la ruta relativa según la profundidad del componente que importa. Agrega solo los que uses al array `imports: []`.

---

### `StButtonComponent` — `<st-button>`

**Selector:** `st-button`
**Archivo:** `src/app/shared/ui/button/st-button.component.ts`

Botón centralizado con soporte de variantes, tamaños, loading spinner e icono PrimeIcon.
**Nunca uses `<button>` nativo para acciones UI primarias.**

```typescript
// Tipos
type BtnVariant = 'primary' | 'secondary' | 'ghost' | 'danger';
type BtnSize    = 'sm' | 'md' | 'lg';

// Inputs
@Input() label:     string     = '';
@Input() icon:      string     = '';      // nombre del pi-icon sin "pi-"
@Input() variant:   BtnVariant = 'primary';
@Input() size:      BtnSize    = 'md';
@Input() type:      'button' | 'submit' | 'reset' = 'button';
@Input() disabled:  boolean    = false;
@Input() loading:   boolean    = false;
@Input() fullWidth: boolean    = false;

// Outputs
@Output() onClick = new EventEmitter<MouseEvent>();
```

**Ejemplos:**

```html
<!-- CTA principal -->
<st-button label="Guardar" type="submit" [loading]="saving()" />

<!-- Con icono -->
<st-button label="Nuevo usuario" icon="pi-plus" (onClick)="openForm()" />

<!-- Solo icono (icon-only) -->
<st-button icon="pi-pencil" variant="ghost" size="sm" (onClick)="edit(item)" />

<!-- Peligroso -->
<st-button label="Eliminar" variant="danger" icon="pi-trash" (onClick)="delete(item)" />

<!-- Ancho completo + loading -->
<st-button label="Ingresar" type="submit" [fullWidth]="true" size="lg" [loading]="loading()" />

<!-- Secundario -->
<st-button label="Cancelar" variant="secondary" (onClick)="close()" />
```

**Notas:**
- `loading=true` muestra spinner y deshabilita el botón automáticamente
- Si solo hay `icon` (sin `label`), aplica clase `st-btn--icon-only` (padding cuadrado)
- Para `type="submit"`, el evento `onClick` no se necesita — el form lo maneja

---

### `StInputComponent` — `<st-input>`

**Selector:** `st-input`
**Archivo:** `src/app/shared/ui/input/st-input.component.ts`
**Implementa:** `ControlValueAccessor` — compatible con `formControlName` y `[(ngModel)]`

```typescript
@Input() label:       string  = '';
@Input() placeholder: string  = '';
@Input() type:        string  = 'text';    // 'text' | 'email' | 'password' | 'number'
@Input() icon:        string  = '';        // nombre pi-icon sin "pi-"
@Input() required:    boolean = false;     // muestra asterisco en label
@Input() hasError:    boolean = false;     // activa estilo de error
@Input() errorMsg:    string  = '';        // texto bajo el input en estado error
```

**Ejemplos:**

```html
<!-- Con ReactiveFormsModule -->
<st-input
  label="Correo electrónico"
  placeholder="tu@email.com"
  type="email"
  icon="pi-envelope"
  formControlName="email"
  [required]="true"
  [hasError]="form.controls.email.invalid && form.controls.email.touched"
  errorMsg="Ingresa un correo válido"
/>

<!-- Contraseña -->
<st-input
  label="Contraseña"
  type="password"
  icon="pi-lock"
  formControlName="password"
  [hasError]="form.controls.password.invalid && form.controls.password.touched"
  errorMsg="La contraseña es requerida"
/>

<!-- Sin icono, solo label -->
<st-input label="Nombre completo" formControlName="nombre" [required]="true" />
```

**Notas:**
- Incluye `ReactiveFormsModule` en `imports: []` del componente padre para usar `formControlName`
- El icono se posiciona como prefix dentro del input
- `hasError` añade borde rojo y activa `errorMsg`

---

### `StSelectComponent` — `<st-select>`

**Selector:** `st-select`
**Archivo:** `src/app/shared/ui/select/st-select.component.ts`
**Implementa:** `ControlValueAccessor`

```typescript
export interface SelectOption { label: string; value: string | number; }

@Input() label:       string         = '';
@Input() placeholder: string         = 'Seleccionar...';
@Input() options:     SelectOption[] = [];
@Input() required:    boolean        = false;

@Output() selectionChange = new EventEmitter<string | number>();
```

**Ejemplos:**

```html
<st-select
  label="Rol"
  placeholder="Seleccionar rol..."
  formControlName="rol"
  [options]="rolOptions"
  [required]="true"
/>
```

```typescript
// En el componente
rolOptions: SelectOption[] = [
  { label: 'Administrador', value: 'ADMIN_TENANT' },
  { label: 'Profesional',   value: 'PROFESIONAL' },
  { label: 'Recepcionista', value: 'RECEPCIONISTA' }
];
```

**Cuando el source es un observable/signal**, mapea antes de pasar:
```typescript
readonly profesionalOptions = computed(() =>
  this.profesionales().map(p => ({ label: p.nombre, value: p.id }))
);
```

---

### `StBadgeComponent` — `<st-badge>`

**Selector:** `st-badge`
**Archivo:** `src/app/shared/ui/badge/st-badge.component.ts`

Badge de estado con variantes semánticas y específicas de dominio Stilum.
**Úsalo para estados de citas, roles, niveles de plan y cualquier etiqueta de estado.**

```typescript
type BadgeVariant =
  | 'default' | 'success' | 'warning' | 'danger' | 'info'  // semánticos
  | 'pendiente' | 'confirmada' | 'en_curso'                 // estados de cita
  | 'completada' | 'cancelada' | 'no_show';

@Input() label:   string       = '';
@Input() variant: BadgeVariant = 'default';
```

**Ejemplos:**

```html
<!-- Estado de cita -->
<st-badge [label]="cita.estado" [variant]="citaVariant(cita.estado)" />

<!-- Semánticos -->
<st-badge label="Activo"     variant="success" />
<st-badge label="Pendiente"  variant="warning" />
<st-badge label="Cancelado"  variant="danger" />
<st-badge label="Demo"       variant="info" />
```

**Helper para mapear estados de cita:**
```typescript
citaVariant(estado: string): BadgeVariant {
  const map: Record<string, BadgeVariant> = {
    PENDIENTE:  'pendiente',
    CONFIRMADA: 'confirmada',
    EN_CURSO:   'en_curso',
    COMPLETADA: 'completada',
    CANCELADA:  'cancelada',
    NO_SHOW:    'no_show'
  };
  return map[estado] ?? 'default';
}
```

---

### `StModalComponent` — `<st-modal>`

**Selector:** `st-modal`
**Archivo:** `src/app/shared/ui/modal/st-modal.component.ts`

Modal con header, overlay con cierre al click, cierre por ESC, y slot de contenido via `ng-content`.

```typescript
@Input() visible:         boolean = false;
@Input() header:          string  = '';
@Input() width:           string  = '520px';    // CSS width del panel
@Input() closeOnOverlay:  boolean = true;

@Output() visibleChange = new EventEmitter<boolean>(); // two-way binding [(visible)]
@Output() closed        = new EventEmitter<void>();
```

**Patrón estándar — signal + two-way binding:**

```typescript
// componente.ts
readonly showForm = signal(false);
```

```html
<!-- componente.html -->
<st-button label="Nuevo" icon="pi-plus" (onClick)="showForm.set(true)" />

<st-modal
  header="Crear usuario"
  [(visible)]="showForm"
  width="560px"
  (closed)="resetForm()"
>
  <form [formGroup]="form" (ngSubmit)="onSubmit()" class="modal-form">
    <st-input label="Nombre" formControlName="nombre" [required]="true" />
    <st-select label="Rol" formControlName="rol" [options]="rolOptions" />
    <div class="modal-actions">
      <st-button label="Cancelar" variant="secondary" (onClick)="showForm.set(false)" />
      <st-button label="Guardar"  type="submit" [loading]="saving()" />
    </div>
  </form>
</st-modal>
```

**Notas:**
- El contenido del modal va dentro de `<st-modal>...</st-modal>` via `ng-content`
- `(closed)` se emite tanto al cerrar por ESC como al hacer click en la X o el overlay
- Para formularios de edición, usa `showForm.set(false)` en `(closed)` para resetear estado

---

### `StToastComponent` + `ToastService`

**Selector:** `st-toast` (solo una instancia, en `app-shell.component.html`)
**Archivo:** `src/app/shared/ui/toast/`

Sistema de notificaciones. `StToastComponent` ya está montado en el shell; en los features solo
inyectas `ToastService`.

```typescript
// Interfaz
interface ToastMessage {
  severity: 'success' | 'error' | 'warning' | 'info';
  summary: string;
  detail?: string;
}
```

**Uso en cualquier componente:**
```typescript
private readonly toast = inject(ToastService);

onSubmit(): void {
  this.service.save(data).subscribe({
    next: () => {
      this.toast.add({ severity: 'success', summary: 'Guardado', detail: 'Los cambios se guardaron.' });
      this.showForm.set(false);
    },
    error: (err) => {
      this.toast.add({ severity: 'error', summary: 'Error', detail: err.message });
    }
  });
}
```

**Severidades:**
```typescript
this.toast.add({ severity: 'success', summary: 'Éxito' });
this.toast.add({ severity: 'error',   summary: 'Error al guardar' });
this.toast.add({ severity: 'warning', summary: 'Advertencia' });
this.toast.add({ severity: 'info',    summary: 'Información' });
```

**Nunca:** instanciar `MessageService` de PrimeNG — fue eliminado del proyecto.

---

---

### `StTextareaComponent` — `<st-textarea>`

ControlValueAccessor. Igual que `st-input` pero multilínea.

```typescript
@Input() label = '';
@Input() placeholder = '';
@Input() rows = 4;
@Input() required = false;
@Input() hasError = false;
@Input() errorMsg = '';
@Input() maxLength?: number; // muestra contador "X / maxLength", rojo al 90%
```

```html
<st-textarea label="Notas" formControlName="notas" [rows]="5"
  [maxLength]="500" placeholder="Observaciones adicionales..." />
```

---

### `StCheckboxComponent` — `<st-checkbox>`

ControlValueAccessor (value = `boolean`). Checkbox custom visualmente consistente.

```typescript
@Input() label = '';
@Input() description = ''; // texto de ayuda bajo el label
@Input() required = false;
@Input() disabled = false;
```

```html
<st-checkbox label="Activo" formControlName="activo" />
<st-checkbox label="Enviar notificaciones" description="Recibirás emails de confirmación"
  formControlName="notificaciones" />
```

---

### `StToggleComponent` — `<st-toggle>`

ControlValueAccessor (value = `boolean`). Toggle pill switch.

```typescript
@Input() label = '';
@Input() description = '';
@Input() labelPosition: 'left' | 'right' = 'right';
@Input() disabled = false;
@Input() size: 'sm' | 'md' = 'md';
```

```html
<st-toggle label="Disponible" formControlName="disponible" />
<st-toggle label="Modo mantenimiento" description="Bloquea nuevas citas"
  formControlName="mantenimiento" size="sm" />
```

---

### `StSearchInputComponent` — `<st-search-input>`

ControlValueAccessor (value = `string`). Input de búsqueda con debounce y botón clear.

```typescript
@Input() placeholder = 'Buscar...';
@Input() debounceMs = 300;
@Output() search = new EventEmitter<string>();
```

```html
<!-- Con output directo -->
<st-search-input placeholder="Buscar profesionales..." (search)="onSearch($event)" />

<!-- Con formControl -->
<st-search-input formControlName="query" [debounceMs]="500" (search)="filter($event)" />
```

---

### `StAlertComponent` — `<st-alert>`

Alerta inline con 4 severidades. Opcionalmente descartable.

```typescript
type AlertSeverity = 'success' | 'error' | 'warning' | 'info';
@Input() severity: AlertSeverity = 'info';
@Input() title = '';
@Input() message = '';
@Input() dismissible = false;
@Output() dismissed = new EventEmitter<void>();
```

```html
<st-alert severity="error" title="Error al guardar" message="Verifica los campos." />
<st-alert severity="success" message="Usuario creado correctamente." [dismissible]="true" />
<st-alert severity="warning" title="Sin conexión" message="Trabajando en modo offline." />
```

---

### `StChipComponent` — `<st-chip>`

Etiqueta inline con variantes de color y opción de remover.

```typescript
type ChipVariant = 'default' | 'accent' | 'success' | 'warning' | 'danger' | 'info';
@Input() label = '';
@Input() icon = '';        // pi-icon sin "pi-"
@Input() removable = false;
@Input() variant: ChipVariant = 'default';
@Input() size: 'sm' | 'md' = 'md';
@Output() removed = new EventEmitter<void>();
```

```html
<st-chip label="Colorista" variant="accent" />
<st-chip label="Activo" variant="success" />
<st-chip label="TypeScript" icon="pi-code" [removable]="true" (removed)="removeTag(tag)" />
```

---

### `StEmptyStateComponent` — `<st-empty-state>`

Estado vacío centrado con ícono, título, mensaje y CTA opcional.

```typescript
@Input() icon = 'pi-inbox';   // pi-icon sin "pi-"
@Input() title = 'Sin resultados';
@Input() message = '';
@Input() actionLabel = '';    // si existe, muestra botón primario
@Output() action = new EventEmitter<void>();
```

```html
<st-empty-state
  icon="pi-calendar-times"
  title="Sin citas programadas"
  message="No hay citas para el día de hoy."
  actionLabel="Crear cita"
  (action)="openNewAppointment()"
/>
```

---

### `StSpinnerComponent` — `<st-spinner>`

Spinner animado con 4 tamaños y modo overlay.

```typescript
type SpinnerSize = 'sm' | 'md' | 'lg' | 'xl';
@Input() size: SpinnerSize = 'md';
@Input() label = '';        // texto opcional debajo del spinner
@Input() overlay = false;   // cubre el padre con fondo semitransparente
```

```html
<st-spinner size="lg" label="Cargando citas..." />

<!-- Overlay sobre un contenedor con position: relative -->
<div style="position: relative;">
  <st-spinner [overlay]="loading()" />
  <!-- contenido -->
</div>
```

---

### `StSkeletonComponent` — `<st-skeleton>`

Bloques skeleton con animación shimmer para estados de carga.

```typescript
type SkeletonVariant = 'text' | 'circle' | 'rect';
@Input() variant: SkeletonVariant = 'text';
@Input() width = '100%';
@Input() height = '1rem';
@Input() lines = 1;          // para 'text': repite N líneas; la última al 60%
@Input() borderRadius = '';
```

```html
<!-- Texto: 3 líneas -->
<st-skeleton variant="text" [lines]="3" height="0.875rem" />

<!-- Avatar circular -->
<st-skeleton variant="circle" width="40px" />

<!-- Bloque de imagen -->
<st-skeleton variant="rect" width="100%" height="200px" />
```

---

### `StAvatarComponent` — `<st-avatar>`

Avatar circular con iniciales generadas del nombre o imagen (con fallback).

```typescript
type AvatarSize = 'xs' | 'sm' | 'md' | 'lg' | 'xl';
@Input() name = '';     // "María García" → "MG"
@Input() src = '';      // URL de imagen; fallback a iniciales si falla
@Input() size: AvatarSize = 'md';
@Input() color = '';    // override del color de fondo
```

```html
<st-avatar name="Carlos López" size="md" />
<st-avatar [name]="user.nombre" [src]="user.foto" size="lg" />

<!-- En tabla: xs o sm -->
<st-avatar [name]="row.nombre" size="sm" />
```

**Tamaños:** `xs=24px`, `sm=32px`, `md=40px`, `lg=52px`, `xl=72px`.
El color de fondo se calcula automáticamente del nombre (paleta de 8 colores consistentes).

---

### `StStatCardComponent` — `<st-stat-card>`

Tarjeta de métrica/KPI para el dashboard.

```typescript
type StatVariant = 'default' | 'accent' | 'success' | 'warning' | 'info';
@Input() label = '';                      // "Citas hoy"
@Input() value: string | number = '';     // "24" o "94%"
@Input() icon = '';                       // pi-icon sin "pi-"
@Input() trend?: number;                  // 12.5 = +12.5%; -3.2 = -3.2%
@Input() trendLabel = '';                 // "vs ayer"
@Input() variant: StatVariant = 'default';
```

```html
<st-stat-card label="Citas hoy" value="24" icon="pi-calendar"
  [trend]="12.5" trendLabel="vs ayer" variant="accent" />
<st-stat-card label="Ingresos" value="$1,240" icon="pi-dollar"
  [trend]="-3.2" trendLabel="vs semana pasada" />
```

---

### `StProgressBarComponent` — `<st-progress-bar>`

Barra de progreso horizontal con variantes de color y tamaño.

```typescript
type ProgressVariant = 'default' | 'success' | 'warning' | 'danger';
type ProgressSize = 'sm' | 'md' | 'lg';
@Input() value = 0;           // 0–100
@Input() label = '';
@Input() showValue = true;    // muestra "X%"
@Input() variant: ProgressVariant = 'default';
@Input() size: ProgressSize = 'md';
@Input() animated = false;    // shimmer animado
```

```html
<st-progress-bar label="Ocupación" [value]="75" variant="success" />
<st-progress-bar [value]="capacidad()" [showValue]="true" size="sm"
  [variant]="capacidad() > 90 ? 'danger' : 'default'" />
```

---

### `StTabsComponent` — `<st-tabs>`

Navegación por pestañas con two-way binding. Dos variantes visuales.

```typescript
export interface StTab { label: string; value: string; icon?: string; badge?: number; disabled?: boolean; }

@Input() tabs: StTab[] = [];
@Input() active = '';
@Output() activeChange = new EventEmitter<string>(); // two-way: [(active)]
@Input() variant: 'line' | 'pills' = 'line';
```

```html
<st-tabs [tabs]="tabs" [(active)]="activeTab" />

<!-- En el componente: -->
tabs: StTab[] = [
  { label: 'General', value: 'general', icon: 'pi-user' },
  { label: 'Horarios', value: 'horarios', icon: 'pi-clock' },
  { label: 'Servicios', value: 'servicios', icon: 'pi-list', badge: 3 },
];
readonly activeTab = signal('general');

<!-- Contenido condicionado al tab activo: -->
@switch (activeTab()) {
  @case ('general') { <app-perfil-general /> }
  @case ('horarios') { <app-horarios /> }
  @case ('servicios') { <app-servicios-profesional /> }
}
```

---

### `StPaginatorComponent` — `<st-paginator>`

Paginación con info de registros y navegación por páginas.

```typescript
@Input() total = 0;       // total de registros
@Input() page = 1;        // página actual (1-indexed)
@Input() pageSize = 10;   // registros por página
@Output() pageChange = new EventEmitter<number>();
```

```html
<st-paginator [total]="total()" [page]="page()" [pageSize]="20"
  (pageChange)="page.set($event)" />
```

Muestra "Mostrando X–Y de Z registros" + botones de página con ellipsis inteligente.

---

### `StConfirmDialogComponent` — `<st-confirm-dialog>`

Modal de confirmación para acciones destructivas. El padre controla el cierre.

```typescript
type ConfirmVariant = 'danger' | 'warning' | 'info';
@Input() visible = false;
@Input() title = '¿Confirmar acción?';
@Input() message = '';
@Input() confirmLabel = 'Confirmar';
@Input() cancelLabel = 'Cancelar';
@Input() variant: ConfirmVariant = 'danger';
@Input() loading = false;     // spinner en botón confirm
@Output() visibleChange = new EventEmitter<boolean>();
@Output() confirmed = new EventEmitter<void>();
@Output() cancelled = new EventEmitter<void>();
```

**Patrón estándar para eliminar:**

```typescript
// componente.ts
readonly showConfirm = signal(false);
readonly confirmLoading = signal(false);
pendingItem: MyItem | null = null;

askDelete(item: MyItem): void { this.pendingItem = item; this.showConfirm.set(true); }

onConfirm(): void {
  this.confirmLoading.set(true);
  this.service.delete(this.pendingItem!.id).subscribe({
    next: () => { this.showConfirm.set(false); this.confirmLoading.set(false); this.load(); },
    error: () => { this.confirmLoading.set(false); }
  });
}
```

```html
<button class="action-btn action-btn--danger" (click)="askDelete(item)">
  <i class="pi pi-trash"></i>
</button>

<st-confirm-dialog
  [(visible)]="showConfirm"
  title="Eliminar usuario"
  [message]="'¿Eliminar a ' + (pendingItem?.nombre ?? '') + '? Esta acción no se puede deshacer.'"
  confirmLabel="Eliminar"
  variant="danger"
  [loading]="confirmLoading()"
  (confirmed)="onConfirm()"
/>
```

**Importante:** `confirmed` NO cierra el diálogo automáticamente. El padre debe hacerlo cuando termina la operación (`showConfirm.set(false)`). Esto permite mostrar el spinner de loading.

---

## Clases Globales de Utilidad (`src/styles/styles.scss`)

Estas clases están disponibles globalmente — no requieren importación.

### Texto
```html
<span class="text-muted">texto secundario</span>
<span class="text-accent">texto coral</span>
<span class="text-sm">14px</span>
<span class="text-xs">12px</span>
<span class="text-lg">18px</span>
<span class="font-display">Fraunces</span>
```

### Layout de página lista (patrón estándar)
```html
<div class="list-page">
  <div class="list-header">
    <div>
      <h1>Usuarios</h1>
      <p class="text-muted">Gestión de accesos</p>
    </div>
    <st-button label="Nuevo" icon="pi-plus" (onClick)="openForm()" />
  </div>

  <div class="list-card">
    <table class="st-table">
      <thead>
        <tr>
          <th>Nombre</th>
          <th>Rol</th>
          <th></th>
        </tr>
      </thead>
      <tbody>
        @for (item of items(); track item.id) {
          <tr>
            <td>
              <span class="table-primary">{{ item.nombre }}</span>
              <span class="table-secondary">{{ item.email }}</span>
            </td>
            <td><st-badge [label]="item.rol" variant="info" /></td>
            <td>
              <div class="table-actions">
                <button class="action-btn" type="button" (click)="edit(item)">
                  <i class="pi pi-pencil"></i>
                </button>
                <button class="action-btn action-btn--danger" type="button" (click)="delete(item)">
                  <i class="pi pi-trash"></i>
                </button>
              </div>
            </td>
          </tr>
        }
      </tbody>
    </table>
  </div>
</div>
```

### Formulario en modal
```html
<form [formGroup]="form" (ngSubmit)="onSubmit()" class="modal-form">
  <st-input label="Nombre" formControlName="nombre" />
  <div class="form-row-2">
    <st-input label="Email" formControlName="email" type="email" />
    <st-select label="Rol" formControlName="rol" [options]="rolOptions" />
  </div>
  <div class="modal-actions">
    <st-button label="Cancelar" variant="secondary" (onClick)="close()" />
    <st-button label="Guardar"  type="submit" [loading]="saving()" />
  </div>
</form>
```

### Card genérica
```html
<div class="card">
  <!-- contenido -->
</div>
```

### Estado vacío (empty state)
```html
<div class="empty-state">
  <i class="pi pi-inbox empty-state__icon"></i>
  <p>No hay registros aún</p>
  <st-button label="Crear primero" icon="pi-plus" (onClick)="openForm()" />
</div>
```

### Loading en tabla
```html
@if (loading()) {
  <div class="table-loading">
    <span class="spinner"></span>
    <span class="text-muted text-sm">Cargando...</span>
  </div>
}
```

### Chip / etiqueta inline
```html
<span class="chip">Plan Pro</span>
```

---

## Iconos — PrimeIcons

El proyecto usa la librería `primeicons` para iconos. Se referencia como clase CSS:

```html
<i class="pi pi-calendar"></i>
<i class="pi pi-user"></i>
```

En los inputs de `StButtonComponent` e `StInputComponent`, se pasa solo el sufijo:
```html
<st-button icon="pi-plus" label="Agregar" />
<st-input  icon="pi-envelope" label="Email" />
```

**Íconos frecuentes en este proyecto:**
```
pi-calendar       citas / dashboard
pi-users          profesionales
pi-list           servicios
pi-user           usuarios / perfil
pi-building       tenants
pi-plus           crear
pi-pencil         editar
pi-trash          eliminar
pi-check          confirmar / éxito
pi-times          cerrar / cancelar
pi-search         búsqueda
pi-filter         filtro
pi-sign-out       cerrar sesión
pi-sun / pi-moon  toggle de tema
pi-chevron-down   dropdown
pi-info-circle    info / hints
pi-exclamation-circle  error / advertencia
pi-play / pi-play-circle  demo mode
```

---

---

## Convención de estilos por componente

### Regla: `styleUrls` + SCSS externo es el estándar

Todos los componentes `st-*` de complejidad media o alta definen sus estilos en un archivo `.scss` separado co-ubicado con el `.ts`. Este par de archivos es **una unidad** — Angular los encapsula y bundlea juntos en tiempo de build. El usuario nunca ve dos archivos separados.

```
src/app/shared/ui/button/
├── st-button.component.ts     ← lógica + template
└── st-button.component.scss   ← estilos (encapsulados y bundleados juntos)
```

**Por qué `styleUrls` sobre `styles: [...]` inline:**
- SCSS completo: nesting (`&--variant`), `@keyframes`, `@mixin`, encadenamiento
- IDE: linting, autocomplete, format y refactor funcionan en `.scss`, no en string templates
- DevTools: el nombre del archivo fuente aparece al inspeccionar estilos
- Legibilidad: componentes grandes con 50+ reglas son ilegibles inline

### Cuándo usar `styles: [...]` inline

Solo para componentes con muy pocas reglas CSS donde el SCSS no aporta ventaja:

| Componente | Reglas CSS | Estilo |
|-----------|-----------|--------|
| StBadge | ~11 | `styles: []` inline ✓ |
| StChip | ~15 | `styles: []` inline ✓ |
| StSpinner | ~10 | `styles: []` inline ✓ |
| StEmptyState | ~12 | `styles: []` inline ✓ |
| StAvatar | ~5 | `styles: []` inline ✓ (usa `[style]` bindings dinámicos) |
| StButton | 116 | `styleUrls` externo ✓ |
| StAlert | ~40 | `styleUrls` externo ✓ |
| StStatCard | ~35 | `styleUrls` externo ✓ |

**Umbral práctico:** si el componente tiene variantes BEM (`--success`, `--danger`, `--sm`), animaciones con `@keyframes`, o más de ~20 reglas → `styleUrls`.

### Tokens CSS son siempre transversales (globales)

Los tokens de diseño (`var(--color-accent)`, `var(--space-4)`, etc.) se cargan globalmente desde `src/styles/styles.scss`. Esto es **obligatorio** para que el sistema dark/light mode funcione — cambiar `data-theme` en `<html>` actualiza todos los componentes automáticamente sin código adicional.

**No intentes embedar los tokens dentro de los componentes.** Son el contrato entre el tema y los componentes, y deben vivir globalmente.

### Patrón BEM para variantes dinámicas

Cuando un `@Input() variant` controla colores o tamaños, usa clases BEM en el template en lugar de bindings `[style]` dinámicos. Esto mantiene los estilos en SCSS y el componente limpio:

```typescript
// ✅ Correcto — clase BEM en template
get fillClass(): string {
  return `st-progress__fill st-progress__fill--${this.variant}`;
}
```
```html
<div [class]="fillClass" [style.width.%]="value"></div>
```
```scss
// Todos los estados en SCSS
.st-progress__fill--default { background: var(--color-accent);  }
.st-progress__fill--success { background: var(--color-success); }
.st-progress__fill--danger  { background: var(--color-danger);  }
```

```typescript
// ❌ Evitar — estilos calculados en el componente
get fillStyle(): Record<string, string> {
  return { background: VARIANT_COLOR[this.variant], width: `${this.value}%` };
}
```

**Excepción válida:** `[style.width.%]`, `[style.height]`, etc. para valores **numéricos dinámicos** que no se pueden expresar como clases (como el porcentaje de un progress bar o el tamaño calculado de un avatar).

---

## Reglas Críticas

1. **Nunca hardcodear colores.** `color: #E85D4A` → `color: var(--color-accent)`.
2. **Nunca hardcodear spacing.** `padding: 16px` → `padding: var(--space-4)`.
3. **Nunca reimplementar `<button>`** para acciones UI — usa `<st-button>`.
4. **Nunca usar `MessageService` de PrimeNG** — fue eliminado. Usa `ToastService`.
5. **Nunca usar componentes de PrimeNG** (`p-button`, `p-dialog`, `p-table`, etc.) — fueron eliminados.
6. **Los estilos de página van en el `.scss` del componente**, no inline ni en `styles.scss` global (salvo que sean realmente globales como las clases de utilidad de arriba).
7. **`ChangeDetectionStrategy.OnPush`** en todos los componentes nuevos.
8. **En CSS nesting, los selectores BEM con guion bajo** (`&--en_curso`) no son válidos en CSS nativo — usa la forma plana: `.mi-clase--en_curso { }`.
