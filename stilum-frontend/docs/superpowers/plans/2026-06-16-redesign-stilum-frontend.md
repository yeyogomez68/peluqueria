# Stilum Frontend Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Reemplazar todos los componentes PrimeNG con componentes custom en HTML/SCSS nativo bajo la estética "Moderno / Limpio" (fondo #FAFAF9 / #111110, acento coral #E85D4A, fuentes Fraunces + DM Sans, dark/light toggle).

**Architecture:** Estrategia híbrida — se eliminan los componentes PrimeNG visibles (p-button, p-select, p-dialog, p-tag, p-menu, p-avatar) y se reemplazan con HTML nativo + clases CSS custom. Se conserva p-toast (solo funcional, se le aplican estilos custom) y FullCalendar. Se construye una librería shared/ui con componentes Angular reutilizables (StButton, StInput, StSelect, StModal, StBadge). El ThemeService existente ya gestiona el toggle dark/light mediante `data-theme` en `<html>`.

**Tech Stack:** Angular 17+ standalone, SCSS con CSS custom properties, HTML nativo, Google Fonts (Fraunces + DM Sans), FullCalendar (sin cambios), PrimeIcons (conservar solo íconos).

---

## Mapa de archivos

### Crear
- `src/app/shared/ui/button/st-button.component.ts` + `.scss`
- `src/app/shared/ui/input/st-input.component.ts` + `.scss`
- `src/app/shared/ui/select/st-select.component.ts` + `.scss`
- `src/app/shared/ui/modal/st-modal.component.ts` + `.scss`
- `src/app/shared/ui/badge/st-badge.component.ts` + `.scss`
- `src/app/shared/ui/toast/st-toast.component.ts` + `.scss` + `toast.service.ts`
- `src/app/shared/ui/index.ts` (barrel export)

### Modificar
- `src/index.html` — Google Fonts import
- `src/styles/_colors.scss` — añadir palette coral
- `src/styles/_themes.scss` — variables semánticas con coral como nuevo acento primario
- `src/styles/_typography.scss` — Fraunces + DM Sans
- `src/styles/styles.scss` — quitar primeng-overrides, añadir utility classes
- `src/styles/_primeng-overrides.scss` — vaciar (solo conservar toast mínimo)
- `src/app/app.config.ts` — quitar providePrimeNG + Lara theme
- `src/app/shell/app-shell.component.ts` + `.html` + `.scss`
- `src/app/features/auth/login/login.component.ts` + `.html` + `.scss`
- `src/app/features/dashboard/dashboard.component.html` + `.scss`
- `src/app/features/dashboard/nueva-cita-modal.component.ts` + `.html` + `.scss`
- `src/app/features/tenant/servicios/servicios-list.component.ts` + `.html` + `.scss`
- `src/app/features/tenant/profesionales/profesionales-list.component.ts` + `.html` + `.scss`
- `src/app/features/admin/tenants/tenants-list.component.ts` + `.html` + `.scss`

---

## Task 1: Design System — Tokens, Tipografía, Fuentes

**Files:**
- Modify: `src/index.html`
- Modify: `src/styles/_colors.scss`
- Modify: `src/styles/_themes.scss`
- Modify: `src/styles/_typography.scss`
- Modify: `src/styles/styles.scss`

- [ ] **Step 1: Agregar Google Fonts en index.html**

```html
<!-- src/index.html — dentro de <head> -->
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link href="https://fonts.googleapis.com/css2?family=Fraunces:opsz,wght@9..144,300;9..144,400;9..144,600;9..144,700&family=DM+Sans:wght@300;400;500;600&display=swap" rel="stylesheet">
```

- [ ] **Step 2: Actualizar `_colors.scss` con palette coral**

```scss
// src/styles/_colors.scss — reemplazar contenido completo

// ── Acento principal (coral) ───────────────────────────────────
$c-coral:           #E85D4A;
$c-coral-light:     #F08070;
$c-coral-dark:      #C94030;
$c-coral-subtle:    #FDF0EE;

// ── Neutros cálidos (light theme) ─────────────────────────────
$c-warm-white:      #FAFAF9;
$c-warm-50:         #F5F4F2;
$c-warm-100:        #EEECE8;
$c-warm-200:        #DDD9D2;
$c-warm-300:        #C8C3BA;

// ── Oscuros (dark theme) ───────────────────────────────────────
$c-ink:             #111110;
$c-ink-surface:     #1C1C1A;
$c-ink-card:        #242422;
$c-ink-elevated:    #2E2E2C;
$c-ink-border:      #3A3A38;

// ── Textos ─────────────────────────────────────────────────────
$c-text-primary:    #1A1A18;
$c-text-secondary:  #6B6A66;
$c-text-tertiary:   #9E9D99;
$c-text-on-dark:    #F5F4F2;
$c-text-muted-dark: #8A8A86;

// ── Semánticos ─────────────────────────────────────────────────
$c-success:         #22C55E;
$c-warning:         #F59E0B;
$c-danger:          #EF4444;
$c-info:            #3B82F6;

// ── Estados de cita ─────────────────────────────────────────────
$c-cita-pendiente:  #F59E0B;
$c-cita-confirmada: #3B82F6;
$c-cita-en-curso:   #22C55E;
$c-cita-completada: #9E9D99;
$c-cita-cancelada:  #EF4444;
$c-cita-no-show:    #8B5CF6;
```

- [ ] **Step 3: Actualizar `_themes.scss` con nuevas variables semánticas**

```scss
// src/styles/_themes.scss — reemplazar contenido completo
@use 'colors' as c;

// ── Tema Claro ────────────────────────────────────────────────
[data-theme='light'],
:root {
  // Acento primario
  --color-accent:           #{c.$c-coral};
  --color-accent-light:     #{c.$c-coral-light};
  --color-accent-dark:      #{c.$c-coral-dark};
  --color-accent-subtle:    #{c.$c-coral-subtle};
  --color-accent-text:      #ffffff;

  // Fondos
  --color-bg-page:          #{c.$c-warm-white};
  --color-bg-surface:       #ffffff;
  --color-bg-card:          #ffffff;
  --color-bg-elevated:      #{c.$c-warm-50};
  --color-bg-sidebar:       #{c.$c-text-primary};
  --color-bg-input:         #ffffff;
  --color-bg-hover:         #{c.$c-warm-50};
  --color-bg-active:        #{c.$c-warm-100};

  // Textos
  --color-text-1:           #{c.$c-text-primary};
  --color-text-2:           #{c.$c-text-secondary};
  --color-text-3:           #{c.$c-text-tertiary};
  --color-text-on-accent:   #ffffff;
  --color-text-sidebar:     rgba(255,255,255,0.7);
  --color-text-sidebar-active: #ffffff;

  // Bordes
  --color-border:           #{c.$c-warm-100};
  --color-border-strong:    #{c.$c-warm-200};
  --color-border-focus:     #{c.$c-coral};
  --color-border-input:     #{c.$c-warm-200};

  // Sombras
  --shadow-xs:   0 1px 2px rgba(0,0,0,0.04);
  --shadow-sm:   0 1px 4px rgba(0,0,0,0.06);
  --shadow-md:   0 4px 16px rgba(0,0,0,0.08);
  --shadow-lg:   0 8px 32px rgba(0,0,0,0.10);
  --shadow-card: 0 2px 8px rgba(0,0,0,0.05);
  --shadow-modal:0 24px 64px rgba(0,0,0,0.14);

  // Semánticos
  --color-success:        #{c.$c-success};
  --color-success-bg:     #{c.$c-success}18;
  --color-warning:        #{c.$c-warning};
  --color-warning-bg:     #{c.$c-warning}18;
  --color-danger:         #{c.$c-danger};
  --color-danger-bg:      #{c.$c-danger}18;
  --color-info:           #{c.$c-info};
  --color-info-bg:        #{c.$c-info}18;

  // Citas
  --cita-pendiente-bg:    #{c.$c-cita-pendiente}18;
  --cita-pendiente-text:  #{c.$c-cita-pendiente};
  --cita-confirmada-bg:   #{c.$c-cita-confirmada}18;
  --cita-confirmada-text: #{c.$c-cita-confirmada};
  --cita-en-curso-bg:     #{c.$c-cita-en-curso}18;
  --cita-en-curso-text:   #{c.$c-cita-en-curso};
  --cita-completada-bg:   #{c.$c-cita-completada}18;
  --cita-completada-text: #{c.$c-cita-completada};
  --cita-cancelada-bg:    #{c.$c-cita-cancelada}18;
  --cita-cancelada-text:  #{c.$c-cita-cancelada};
  --cita-no-show-bg:      #{c.$c-cita-no-show}18;
  --cita-no-show-text:    #{c.$c-cita-no-show};

  // Transiciones de tema
  --transition-theme: background-color 0.2s ease, color 0.2s ease, border-color 0.2s ease;
}

// ── Tema Oscuro ───────────────────────────────────────────────
[data-theme='dark'] {
  --color-accent:           #{c.$c-coral};
  --color-accent-light:     #{c.$c-coral-light};
  --color-accent-dark:      #{c.$c-coral-dark};
  --color-accent-subtle:    rgba(232,93,74,0.12);
  --color-accent-text:      #ffffff;

  --color-bg-page:          #{c.$c-ink};
  --color-bg-surface:       #{c.$c-ink-surface};
  --color-bg-card:          #{c.$c-ink-card};
  --color-bg-elevated:      #{c.$c-ink-elevated};
  --color-bg-sidebar:       #{c.$c-ink-surface};
  --color-bg-input:         #{c.$c-ink-elevated};
  --color-bg-hover:         rgba(255,255,255,0.04);
  --color-bg-active:        rgba(232,93,74,0.12);

  --color-text-1:           #{c.$c-text-on-dark};
  --color-text-2:           #{c.$c-text-muted-dark};
  --color-text-3:           rgba(255,255,255,0.35);
  --color-text-on-accent:   #ffffff;
  --color-text-sidebar:     rgba(255,255,255,0.55);
  --color-text-sidebar-active: #ffffff;

  --color-border:           #{c.$c-ink-border};
  --color-border-strong:    rgba(255,255,255,0.12);
  --color-border-focus:     #{c.$c-coral};
  --color-border-input:     #{c.$c-ink-border};

  --shadow-xs:   0 1px 2px rgba(0,0,0,0.3);
  --shadow-sm:   0 1px 4px rgba(0,0,0,0.4);
  --shadow-md:   0 4px 16px rgba(0,0,0,0.5);
  --shadow-lg:   0 8px 32px rgba(0,0,0,0.6);
  --shadow-card: 0 2px 8px rgba(0,0,0,0.4);
  --shadow-modal:0 24px 64px rgba(0,0,0,0.7);

  --color-success:        #{c.$c-success};
  --color-success-bg:     #{c.$c-success}20;
  --color-warning:        #{c.$c-warning};
  --color-warning-bg:     #{c.$c-warning}20;
  --color-danger:         #{c.$c-danger};
  --color-danger-bg:      #{c.$c-danger}20;
  --color-info:           #{c.$c-info};
  --color-info-bg:        #{c.$c-info}20;

  --cita-pendiente-bg:    #{c.$c-cita-pendiente}25;
  --cita-pendiente-text:  #{c.$c-cita-pendiente};
  --cita-confirmada-bg:   #{c.$c-cita-confirmada}25;
  --cita-confirmada-text: #{c.$c-cita-confirmada};
  --cita-en-curso-bg:     #{c.$c-cita-en-curso}25;
  --cita-en-curso-text:   #{c.$c-cita-en-curso};
  --cita-completada-bg:   #{c.$c-cita-completada}25;
  --cita-completada-text: #{c.$c-cita-completada};
  --cita-cancelada-bg:    #{c.$c-cita-cancelada}25;
  --cita-cancelada-text:  #{c.$c-cita-cancelada};
  --cita-no-show-bg:      #{c.$c-cita-no-show}25;
  --cita-no-show-text:    #{c.$c-cita-no-show};

  --transition-theme: background-color 0.2s ease, color 0.2s ease, border-color 0.2s ease;
}
```

- [ ] **Step 4: Actualizar `_typography.scss`**

```scss
// src/styles/_typography.scss — reemplazar contenido completo

// ── Variables de fuente ──────────────────────────────────────
:root {
  --font-display:    'Fraunces', Georgia, serif;
  --font-body:       'DM Sans', system-ui, sans-serif;
  --font-mono:       'JetBrains Mono', 'Fira Code', monospace;

  --font-light:      300;
  --font-regular:    400;
  --font-medium:     500;
  --font-semibold:   600;
  --font-bold:       700;

  --text-xs:    0.75rem;    // 12px
  --text-sm:    0.875rem;   // 14px
  --text-base:  1rem;       // 16px
  --text-lg:    1.125rem;   // 18px
  --text-xl:    1.25rem;    // 20px
  --text-2xl:   1.5rem;     // 24px
  --text-3xl:   1.875rem;   // 30px
  --text-4xl:   2.25rem;    // 36px

  --leading-tight:   1.2;
  --leading-snug:    1.35;
  --leading-normal:  1.5;
  --leading-relaxed: 1.65;

  --tracking-tight:  -0.02em;
  --tracking-normal: 0;
  --tracking-wide:   0.04em;
  --tracking-wider:  0.08em;

  // Espaciado
  --space-1:  0.25rem;
  --space-2:  0.5rem;
  --space-3:  0.75rem;
  --space-4:  1rem;
  --space-5:  1.25rem;
  --space-6:  1.5rem;
  --space-8:  2rem;
  --space-10: 2.5rem;
  --space-12: 3rem;
  --space-16: 4rem;

  // Radios
  --radius-sm:   4px;
  --radius-md:   8px;
  --radius-lg:   12px;
  --radius-xl:   16px;
  --radius-2xl:  20px;
  --radius-full: 9999px;

  // Duración animaciones
  --duration-fast:    120ms;
  --duration-normal:  200ms;
  --duration-slow:    350ms;
  --easing-default:   cubic-bezier(0.4, 0, 0.2, 1);
  --easing-spring:    cubic-bezier(0.34, 1.56, 0.64, 1);
}
```

- [ ] **Step 5: Actualizar `styles.scss` global**

```scss
// src/styles/styles.scss — reemplazar contenido completo
@use 'colors';
@use 'themes';
@use 'typography';

// Reset
*, *::before, *::after {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

html {
  font-size: 16px;
  scroll-behavior: smooth;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

body {
  font-family: var(--font-body);
  font-size: var(--text-base);
  font-weight: var(--font-regular);
  line-height: var(--leading-normal);
  color: var(--color-text-1);
  background-color: var(--color-bg-page);
  transition: var(--transition-theme);
  min-height: 100vh;
}

h1, h2, h3 {
  font-family: var(--font-display);
  font-weight: var(--font-semibold);
  line-height: var(--leading-tight);
  color: var(--color-text-1);
  letter-spacing: var(--tracking-tight);
}

h4, h5, h6 {
  font-family: var(--font-body);
  font-weight: var(--font-semibold);
  color: var(--color-text-1);
}

a {
  color: var(--color-accent);
  text-decoration: none;
  transition: color var(--duration-fast) var(--easing-default);
  &:hover { color: var(--color-accent-dark); }
}

// Utilities
.text-muted  { color: var(--color-text-2); }
.text-accent { color: var(--color-accent); }
.text-sm     { font-size: var(--text-sm); }
.text-xs     { font-size: var(--text-xs); }
.text-lg     { font-size: var(--text-lg); }
.font-display{ font-family: var(--font-display); }

.card {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  padding: var(--space-6);
  transition: var(--transition-theme);
}

// Scrollbar
::-webkit-scrollbar { width: 5px; height: 5px; }
::-webkit-scrollbar-track { background: transparent; }
::-webkit-scrollbar-thumb {
  background: var(--color-border-strong);
  border-radius: var(--radius-full);
  &:hover { background: var(--color-text-3); }
}

// PrimeNG toast override mínimo
.p-toast { z-index: 9999; }
.p-toast-message {
  border-radius: var(--radius-lg) !important;
  border: 1px solid var(--color-border) !important;
  backdrop-filter: blur(12px);
}
```

- [ ] **Step 6: Vaciar `_primeng-overrides.scss`**

```scss
// src/styles/_primeng-overrides.scss
// Vacío — ya no usamos PrimeNG components en UI principal
```

---

## Task 2: Shared UI — StButton

**Files:**
- Create: `src/app/shared/ui/button/st-button.component.ts`
- Create: `src/app/shared/ui/button/st-button.component.scss`

- [ ] **Step 1: Crear `st-button.component.ts`**

```typescript
// src/app/shared/ui/button/st-button.component.ts
import {
  Component, Input, Output, EventEmitter,
  ChangeDetectionStrategy, HostBinding
} from '@angular/core';
import { CommonModule } from '@angular/common';

export type BtnVariant = 'primary' | 'secondary' | 'ghost' | 'danger';
export type BtnSize    = 'sm' | 'md' | 'lg';

@Component({
  selector: 'st-button',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule],
  template: `
    <button
      [type]="type"
      [disabled]="disabled || loading"
      [class]="btnClass"
      (click)="onClick.emit($event)"
    >
      @if (loading) {
        <span class="btn-spinner"></span>
      } @else if (icon) {
        <i [class]="'pi ' + icon + ' btn-icon'"></i>
      }
      @if (label) {
        <span>{{ label }}</span>
      }
      <ng-content />
    </button>
  `,
  styleUrls: ['./st-button.component.scss']
})
export class StButtonComponent {
  @Input() label     = '';
  @Input() icon      = '';
  @Input() variant: BtnVariant = 'primary';
  @Input() size: BtnSize       = 'md';
  @Input() type: 'button' | 'submit' | 'reset' = 'button';
  @Input() disabled  = false;
  @Input() loading   = false;
  @Input() fullWidth = false;
  @Output() onClick  = new EventEmitter<MouseEvent>();

  get btnClass(): string {
    return [
      'st-btn',
      `st-btn--${this.variant}`,
      `st-btn--${this.size}`,
      this.fullWidth ? 'st-btn--full' : '',
      this.loading   ? 'st-btn--loading' : '',
      (!this.label && this.icon) ? 'st-btn--icon-only' : ''
    ].filter(Boolean).join(' ');
  }
}
```

- [ ] **Step 2: Crear `st-button.component.scss`**

```scss
// src/app/shared/ui/button/st-button.component.scss

:host { display: contents; }

.st-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  font-family: var(--font-body);
  font-weight: var(--font-medium);
  letter-spacing: var(--tracking-wide);
  text-transform: uppercase;
  border: none;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition:
    background-color var(--duration-fast) var(--easing-default),
    color var(--duration-fast) var(--easing-default),
    box-shadow var(--duration-fast) var(--easing-default),
    transform var(--duration-fast) var(--easing-spring);
  white-space: nowrap;
  text-decoration: none;
  outline: none;
  position: relative;
  overflow: hidden;

  &:focus-visible {
    box-shadow: 0 0 0 3px var(--color-accent-subtle), 0 0 0 5px var(--color-accent);
  }

  &:active:not(:disabled) {
    transform: scale(0.97);
  }

  &:disabled {
    opacity: 0.45;
    cursor: not-allowed;
    pointer-events: none;
  }

  // ── Variantes ────────────────────────────────────────────────
  &--primary {
    background: var(--color-accent);
    color: var(--color-text-on-accent);

    &:hover:not(:disabled) {
      background: var(--color-accent-dark);
      box-shadow: var(--shadow-md);
    }
  }

  &--secondary {
    background: var(--color-bg-elevated);
    color: var(--color-text-1);
    border: 1px solid var(--color-border-strong);

    &:hover:not(:disabled) {
      background: var(--color-bg-hover);
      border-color: var(--color-text-3);
    }
  }

  &--ghost {
    background: transparent;
    color: var(--color-text-2);
    border: none;

    &:hover:not(:disabled) {
      background: var(--color-bg-hover);
      color: var(--color-text-1);
    }
  }

  &--danger {
    background: var(--color-danger-bg);
    color: var(--color-danger);
    border: 1px solid var(--color-danger);

    &:hover:not(:disabled) {
      background: var(--color-danger);
      color: #fff;
    }
  }

  // ── Tamaños ──────────────────────────────────────────────────
  &--sm {
    font-size: var(--text-xs);
    padding: var(--space-2) var(--space-3);
    border-radius: var(--radius-sm);

    .btn-icon { font-size: 0.7rem; }
  }

  &--md {
    font-size: var(--text-sm);
    padding: var(--space-3) var(--space-5);

    .btn-icon { font-size: 0.8rem; }
  }

  &--lg {
    font-size: var(--text-base);
    padding: var(--space-4) var(--space-8);

    .btn-icon { font-size: 0.9rem; }
  }

  &--full { width: 100%; }

  &--icon-only {
    &.st-btn--sm { padding: var(--space-2); }
    &.st-btn--md { padding: var(--space-3); }
    &.st-btn--lg { padding: var(--space-4); }
  }
}

// Spinner de carga
.btn-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: currentColor;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
```

---

## Task 3: Shared UI — StInput, StSelect, StBadge

**Files:**
- Create: `src/app/shared/ui/input/st-input.component.ts` + `.scss`
- Create: `src/app/shared/ui/select/st-select.component.ts` + `.scss`
- Create: `src/app/shared/ui/badge/st-badge.component.ts` + `.scss`
- Create: `src/app/shared/ui/index.ts`

- [ ] **Step 1: Crear `st-input.component.ts`**

```typescript
// src/app/shared/ui/input/st-input.component.ts
import {
  Component, Input, forwardRef, ChangeDetectionStrategy
} from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  ControlValueAccessor, NG_VALUE_ACCESSOR, ReactiveFormsModule
} from '@angular/forms';

@Component({
  selector: 'st-input',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, ReactiveFormsModule],
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => StInputComponent),
    multi: true
  }],
  template: `
    <div class="st-field" [class.st-field--error]="hasError">
      @if (label) {
        <label class="st-label">
          {{ label }}
          @if (required) { <span class="st-label__req">*</span> }
        </label>
      }
      <div class="st-input-wrap" [class.st-input-wrap--prefix]="icon">
        @if (icon) { <i [class]="'pi ' + icon + ' st-input-icon'"></i> }
        <input
          class="st-input"
          [type]="type"
          [placeholder]="placeholder"
          [disabled]="isDisabled"
          [value]="value"
          (input)="onInput($event)"
          (blur)="onTouched()"
        />
      </div>
      @if (hasError && errorMsg) {
        <span class="st-error">{{ errorMsg }}</span>
      }
    </div>
  `,
  styleUrls: ['./st-input.component.scss']
})
export class StInputComponent implements ControlValueAccessor {
  @Input() label       = '';
  @Input() placeholder = '';
  @Input() type        = 'text';
  @Input() icon        = '';
  @Input() required    = false;
  @Input() hasError    = false;
  @Input() errorMsg    = '';

  value = '';
  isDisabled = false;

  onChange: (v: string) => void = () => {};
  onTouched: () => void = () => {};

  onInput(e: Event): void {
    this.value = (e.target as HTMLInputElement).value;
    this.onChange(this.value);
  }

  writeValue(v: string): void { this.value = v ?? ''; }
  registerOnChange(fn: (v: string) => void): void { this.onChange = fn; }
  registerOnTouched(fn: () => void): void { this.onTouched = fn; }
  setDisabledState(isDisabled: boolean): void { this.isDisabled = isDisabled; }
}
```

- [ ] **Step 2: Crear `st-input.component.scss`**

```scss
// src/app/shared/ui/input/st-input.component.scss

:host { display: block; }

.st-field { display: flex; flex-direction: column; gap: var(--space-2); }

.st-label {
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--color-text-1);
  letter-spacing: var(--tracking-wide);
  text-transform: uppercase;

  &__req { color: var(--color-accent); margin-left: 2px; }
}

.st-input-wrap {
  position: relative;

  &--prefix .st-input { padding-left: 2.5rem; }
}

.st-input-icon {
  position: absolute;
  left: var(--space-3);
  top: 50%;
  transform: translateY(-50%);
  color: var(--color-text-3);
  font-size: 0.875rem;
  pointer-events: none;
}

.st-input {
  width: 100%;
  background: var(--color-bg-input);
  color: var(--color-text-1);
  border: 1.5px solid var(--color-border-input);
  border-radius: var(--radius-md);
  padding: var(--space-3) var(--space-4);
  font-family: var(--font-body);
  font-size: var(--text-base);
  outline: none;
  transition:
    border-color var(--duration-fast) var(--easing-default),
    box-shadow var(--duration-fast) var(--easing-default),
    background-color var(--duration-fast);

  &::placeholder { color: var(--color-text-3); }

  &:focus {
    border-color: var(--color-border-focus);
    box-shadow: 0 0 0 3px var(--color-accent-subtle);
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

.st-field--error .st-input {
  border-color: var(--color-danger);
  &:focus { box-shadow: 0 0 0 3px var(--color-danger-bg); }
}

.st-error {
  font-size: var(--text-xs);
  color: var(--color-danger);
}
```

- [ ] **Step 3: Crear `st-select.component.ts`**

```typescript
// src/app/shared/ui/select/st-select.component.ts
import {
  Component, Input, forwardRef, ChangeDetectionStrategy
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

export interface SelectOption { label: string; value: string | number; }

@Component({
  selector: 'st-select',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule],
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => StSelectComponent),
    multi: true
  }],
  template: `
    <div class="st-field">
      @if (label) {
        <label class="st-label">
          {{ label }}
          @if (required) { <span class="st-label__req">*</span> }
        </label>
      }
      <div class="st-select-wrap">
        <select
          class="st-select"
          [disabled]="isDisabled"
          (change)="onSelect($event)"
        >
          @if (placeholder) {
            <option value="" [selected]="!value" disabled>{{ placeholder }}</option>
          }
          @for (opt of options; track opt.value) {
            <option [value]="opt.value" [selected]="opt.value === value">
              {{ opt.label }}
            </option>
          }
        </select>
        <i class="pi pi-chevron-down st-select-icon"></i>
      </div>
    </div>
  `,
  styleUrls: ['./st-select.component.scss']
})
export class StSelectComponent implements ControlValueAccessor {
  @Input() label       = '';
  @Input() placeholder = 'Seleccionar...';
  @Input() options: SelectOption[] = [];
  @Input() required    = false;

  value: string | number = '';
  isDisabled = false;

  onChange: (v: string | number) => void = () => {};
  onTouched: () => void = () => {};

  onSelect(e: Event): void {
    this.value = (e.target as HTMLSelectElement).value;
    this.onChange(this.value);
    this.onTouched();
  }

  writeValue(v: string | number): void { this.value = v ?? ''; }
  registerOnChange(fn: (v: string | number) => void): void { this.onChange = fn; }
  registerOnTouched(fn: () => void): void { this.onTouched = fn; }
  setDisabledState(d: boolean): void { this.isDisabled = d; }
}
```

- [ ] **Step 4: Crear `st-select.component.scss`**

```scss
// src/app/shared/ui/select/st-select.component.scss

:host { display: block; }

.st-field { display: flex; flex-direction: column; gap: var(--space-2); }

.st-label {
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--color-text-1);
  letter-spacing: var(--tracking-wide);
  text-transform: uppercase;

  &__req { color: var(--color-accent); margin-left: 2px; }
}

.st-select-wrap {
  position: relative;
}

.st-select {
  width: 100%;
  appearance: none;
  background: var(--color-bg-input);
  color: var(--color-text-1);
  border: 1.5px solid var(--color-border-input);
  border-radius: var(--radius-md);
  padding: var(--space-3) var(--space-10) var(--space-3) var(--space-4);
  font-family: var(--font-body);
  font-size: var(--text-base);
  outline: none;
  cursor: pointer;
  transition:
    border-color var(--duration-fast) var(--easing-default),
    box-shadow var(--duration-fast) var(--easing-default);

  option { background: var(--color-bg-card); color: var(--color-text-1); }

  &:focus {
    border-color: var(--color-border-focus);
    box-shadow: 0 0 0 3px var(--color-accent-subtle);
  }

  &:disabled { opacity: 0.5; cursor: not-allowed; }
}

.st-select-icon {
  position: absolute;
  right: var(--space-4);
  top: 50%;
  transform: translateY(-50%);
  font-size: 0.75rem;
  color: var(--color-text-3);
  pointer-events: none;
}
```

- [ ] **Step 5: Crear `st-badge.component.ts`**

```typescript
// src/app/shared/ui/badge/st-badge.component.ts
import { Component, Input, ChangeDetectionStrategy } from '@angular/core';

export type BadgeVariant = 'default' | 'success' | 'warning' | 'danger' | 'info' | 'pendiente' | 'confirmada' | 'en_curso' | 'completada' | 'cancelada' | 'no_show';

@Component({
  selector: 'st-badge',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<span [class]="'st-badge st-badge--' + variant">{{ label }}</span>`,
  styles: [`
    :host { display: contents; }

    .st-badge {
      display: inline-flex;
      align-items: center;
      padding: 0.2rem 0.6rem;
      border-radius: var(--radius-full);
      font-size: var(--text-xs);
      font-weight: var(--font-semibold);
      letter-spacing: var(--tracking-wide);
      text-transform: uppercase;
      white-space: nowrap;

      &--default    { background: var(--color-bg-elevated); color: var(--color-text-2); }
      &--success    { background: var(--color-success-bg); color: var(--color-success); }
      &--warning    { background: var(--color-warning-bg); color: var(--color-warning); }
      &--danger     { background: var(--color-danger-bg); color: var(--color-danger); }
      &--info       { background: var(--color-info-bg); color: var(--color-info); }
      &--pendiente  { background: var(--cita-pendiente-bg); color: var(--cita-pendiente-text); }
      &--confirmada { background: var(--cita-confirmada-bg); color: var(--cita-confirmada-text); }
      &--en_curso   { background: var(--cita-en-curso-bg); color: var(--cita-en-curso-text); }
      &--completada { background: var(--cita-completada-bg); color: var(--cita-completada-text); }
      &--cancelada  { background: var(--cita-cancelada-bg); color: var(--cita-cancelada-text); }
      &--no_show    { background: var(--cita-no-show-bg); color: var(--cita-no-show-text); }
    }
  `]
})
export class StBadgeComponent {
  @Input() label   = '';
  @Input() variant: BadgeVariant = 'default';
}
```

- [ ] **Step 6: Crear barrel export `src/app/shared/ui/index.ts`**

```typescript
// src/app/shared/ui/index.ts
export { StButtonComponent } from './button/st-button.component';
export { StInputComponent  } from './input/st-input.component';
export { StSelectComponent } from './select/st-select.component';
export { StBadgeComponent  } from './badge/st-badge.component';
export { StModalComponent  } from './modal/st-modal.component';
```

---

## Task 4: Shared UI — StModal + StToast

**Files:**
- Create: `src/app/shared/ui/modal/st-modal.component.ts` + `.scss`
- Create: `src/app/shared/ui/toast/st-toast.component.ts` + `.scss`
- Create: `src/app/shared/ui/toast/toast.service.ts`

- [ ] **Step 1: Crear `st-modal.component.ts`**

```typescript
// src/app/shared/ui/modal/st-modal.component.ts
import {
  Component, Input, Output, EventEmitter,
  ChangeDetectionStrategy, HostListener
} from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'st-modal',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule],
  template: `
    @if (visible) {
      <div class="st-modal-overlay" (click)="onOverlayClick($event)">
        <div class="st-modal-box" [style.width]="width" role="dialog" [attr.aria-label]="header">
          @if (header) {
            <div class="st-modal-header">
              <h2 class="st-modal-title">{{ header }}</h2>
              <button class="st-modal-close" type="button" (click)="onClose()">
                <i class="pi pi-times"></i>
              </button>
            </div>
          }
          <div class="st-modal-body">
            <ng-content />
          </div>
        </div>
      </div>
    }
  `,
  styleUrls: ['./st-modal.component.scss']
})
export class StModalComponent {
  @Input() visible = false;
  @Input() header  = '';
  @Input() width   = '520px';
  @Input() closeOnOverlay = true;
  @Output() visibleChange = new EventEmitter<boolean>();
  @Output() closed        = new EventEmitter<void>();

  @HostListener('document:keydown.escape')
  onEscape(): void { if (this.visible) this.onClose(); }

  onClose(): void {
    this.visibleChange.emit(false);
    this.closed.emit();
  }

  onOverlayClick(e: MouseEvent): void {
    if (this.closeOnOverlay && e.target === e.currentTarget) this.onClose();
  }
}
```

- [ ] **Step 2: Crear `st-modal.component.scss`**

```scss
// src/app/shared/ui/modal/st-modal.component.scss

.st-modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn var(--duration-normal) var(--easing-default);
  padding: var(--space-4);
}

.st-modal-box {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-modal);
  max-height: 90vh;
  overflow-y: auto;
  animation: slideUp var(--duration-normal) var(--easing-spring);
  width: 100%;
  max-width: v-bind(width);
}

.st-modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-6) var(--space-6) 0;
  margin-bottom: var(--space-5);
}

.st-modal-title {
  font-family: var(--font-display);
  font-size: var(--text-xl);
  font-weight: var(--font-semibold);
  color: var(--color-text-1);
  letter-spacing: var(--tracking-tight);
}

.st-modal-close {
  background: none;
  border: none;
  cursor: pointer;
  color: var(--color-text-3);
  padding: var(--space-2);
  border-radius: var(--radius-sm);
  transition: color var(--duration-fast), background var(--duration-fast);
  display: flex;
  align-items: center;

  &:hover {
    color: var(--color-text-1);
    background: var(--color-bg-hover);
  }
}

.st-modal-body {
  padding: 0 var(--space-6) var(--space-6);
}

@keyframes fadeIn {
  from { opacity: 0; }
  to   { opacity: 1; }
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(16px) scale(0.97); }
  to   { opacity: 1; transform: translateY(0) scale(1); }
}
```

- [ ] **Step 3: Crear `toast.service.ts`**

```typescript
// src/app/shared/ui/toast/toast.service.ts
import { Injectable, signal } from '@angular/core';

export interface ToastMessage {
  id: number;
  severity: 'success' | 'error' | 'warning' | 'info';
  summary: string;
  detail?: string;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  readonly messages = signal<ToastMessage[]>([]);
  private nextId = 0;

  add(msg: Omit<ToastMessage, 'id'>): void {
    const id = this.nextId++;
    this.messages.update(msgs => [...msgs, { ...msg, id }]);
    setTimeout(() => this.remove(id), 4000);
  }

  remove(id: number): void {
    this.messages.update(msgs => msgs.filter(m => m.id !== id));
  }
}
```

- [ ] **Step 4: Crear `st-toast.component.ts`**

```typescript
// src/app/shared/ui/toast/st-toast.component.ts
import { Component, inject, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService } from './toast.service';

@Component({
  selector: 'st-toast',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule],
  template: `
    <div class="st-toast-container">
      @for (msg of toast.messages(); track msg.id) {
        <div class="st-toast st-toast--{{ msg.severity }}" (click)="toast.remove(msg.id)">
          <i [class]="'pi ' + iconFor(msg.severity) + ' st-toast-icon'"></i>
          <div class="st-toast-content">
            <span class="st-toast-summary">{{ msg.summary }}</span>
            @if (msg.detail) {
              <span class="st-toast-detail">{{ msg.detail }}</span>
            }
          </div>
          <button class="st-toast-close"><i class="pi pi-times"></i></button>
        </div>
      }
    </div>
  `,
  styles: [`
    .st-toast-container {
      position: fixed;
      bottom: 1.5rem;
      right: 1.5rem;
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
      z-index: 9999;
    }

    .st-toast {
      display: flex;
      align-items: flex-start;
      gap: 0.75rem;
      padding: 0.875rem 1rem;
      border-radius: var(--radius-lg);
      border: 1px solid var(--color-border);
      background: var(--color-bg-card);
      box-shadow: var(--shadow-lg);
      cursor: pointer;
      min-width: 280px;
      max-width: 380px;
      animation: toastIn 0.25s var(--easing-spring);
      backdrop-filter: blur(12px);

      &--success { border-left: 3px solid var(--color-success); .st-toast-icon { color: var(--color-success); } }
      &--error   { border-left: 3px solid var(--color-danger);  .st-toast-icon { color: var(--color-danger); } }
      &--warning { border-left: 3px solid var(--color-warning); .st-toast-icon { color: var(--color-warning); } }
      &--info    { border-left: 3px solid var(--color-info);    .st-toast-icon { color: var(--color-info); } }
    }

    .st-toast-content { flex: 1; display: flex; flex-direction: column; gap: 2px; }
    .st-toast-summary { font-weight: var(--font-semibold); font-size: var(--text-sm); color: var(--color-text-1); }
    .st-toast-detail  { font-size: var(--text-xs); color: var(--color-text-2); }
    .st-toast-close   { background: none; border: none; cursor: pointer; color: var(--color-text-3); padding: 2px; }

    @keyframes toastIn {
      from { opacity: 0; transform: translateX(16px); }
      to   { opacity: 1; transform: translateX(0); }
    }
  `]
})
export class StToastComponent {
  readonly toast = inject(ToastService);

  iconFor(severity: string): string {
    const map: Record<string, string> = {
      success: 'pi-check-circle',
      error:   'pi-times-circle',
      warning: 'pi-exclamation-triangle',
      info:    'pi-info-circle'
    };
    return map[severity] ?? 'pi-info-circle';
  }
}
```

---

## Task 5: Shell — Sidebar + Header

**Files:**
- Modify: `src/app/shell/app-shell.component.ts`
- Modify: `src/app/shell/app-shell.component.html`
- Modify: `src/app/shell/app-shell.component.scss`

- [ ] **Step 1: Actualizar `app-shell.component.ts`** — quitar imports PrimeNG

```typescript
// src/app/shell/app-shell.component.ts
import { Component, inject, signal, computed, ChangeDetectionStrategy } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../core/auth/auth.service';
import { ThemeService } from '../core/theme/theme.service';
import { StButtonComponent } from '../shared/ui/button/st-button.component';
import { StToastComponent } from '../shared/ui/toast/st-toast.component';

interface NavItem { label: string; icon: string; route: string; }

@Component({
  selector: 'st-app-shell',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive, StButtonComponent, StToastComponent],
  templateUrl: './app-shell.component.html',
  styleUrls: ['./app-shell.component.scss']
})
export class AppShellComponent {
  readonly auth         = inject(AuthService);
  readonly themeService = inject(ThemeService);
  readonly collapsed    = signal(false);
  readonly userMenuOpen = signal(false);

  readonly user   = computed(() => this.auth.currentUser());
  readonly isDark = computed(() => this.themeService.isDark);

  readonly navItems = computed<NavItem[]>(() => {
    const rol = this.user()?.rol;
    if (rol === 'SUPER_ADMIN') {
      return [
        { label: 'Tenants',  icon: 'pi-building', route: '/admin/tenants' }
      ];
    }
    const base: NavItem[] = [
      { label: 'Dashboard',     icon: 'pi-calendar',  route: '/dashboard' },
      { label: 'Profesionales', icon: 'pi-users',      route: '/profesionales' },
      { label: 'Servicios',     icon: 'pi-list',       route: '/servicios' }
    ];
    if (rol === 'ADMIN_TENANT') {
      base.push({ label: 'Usuarios', icon: 'pi-user', route: '/usuarios' });
    }
    return base;
  });

  toggleSidebar(): void { this.collapsed.update(v => !v); }
  toggleTheme(): void { this.themeService.toggle(); }
  logout(): void { this.userMenuOpen.set(false); this.auth.logout(); }

  get userInitials(): string {
    return (this.user()?.nombre ?? '')
      .split(' ').slice(0, 2).map(n => n[0]).join('').toUpperCase();
  }
}
```

- [ ] **Step 2: Reemplazar `app-shell.component.html`**

```html
<!-- src/app/shell/app-shell.component.html -->
<div class="shell" [class.shell--collapsed]="collapsed()">

  <!-- Sidebar -->
  <aside class="sidebar">
    <div class="sidebar__logo">
      <div class="logo-mark">S</div>
      @if (!collapsed()) {
        <span class="logo-text">Stilum</span>
      }
    </div>

    <nav class="sidebar__nav">
      @for (item of navItems(); track item.route) {
        <a class="nav-item" [routerLink]="item.route" routerLinkActive="nav-item--active">
          <i [class]="'pi ' + item.icon + ' nav-item__icon'"></i>
          @if (!collapsed()) {
            <span class="nav-item__label">{{ item.label }}</span>
          }
        </a>
      }
    </nav>

    <div class="sidebar__footer">
      <button class="collapse-btn" (click)="toggleSidebar()" type="button">
        <i [class]="'pi ' + (collapsed() ? 'pi-chevron-right' : 'pi-chevron-left')"></i>
      </button>
    </div>
  </aside>

  <!-- Main area -->
  <div class="main-wrapper">

    <header class="header">
      <span class="header__tenant">{{ user()?.tenantNombre ?? 'Stilum Pro' }}</span>

      <div class="header__actions">
        <!-- Theme toggle -->
        <button class="icon-btn" type="button" (click)="toggleTheme()" [title]="isDark() ? 'Tema claro' : 'Tema oscuro'">
          <i [class]="'pi ' + (isDark() ? 'pi-sun' : 'pi-moon')"></i>
        </button>

        <!-- User menu -->
        <div class="user-menu" [class.user-menu--open]="userMenuOpen()">
          <button class="user-trigger" type="button" (click)="userMenuOpen.update(v => !v)">
            <div class="user-avatar">{{ userInitials }}</div>
            @if (!collapsed()) {
              <span class="user-name">{{ user()?.nombre?.split(' ')[0] }}</span>
            }
            <i class="pi pi-chevron-down user-chevron"></i>
          </button>

          @if (userMenuOpen()) {
            <div class="user-dropdown">
              <div class="user-dropdown__info">
                <span class="user-dropdown__name">{{ user()?.nombre }}</span>
                <span class="user-dropdown__role text-muted text-xs">{{ user()?.rol }}</span>
              </div>
              <hr class="user-dropdown__sep" />
              <button class="user-dropdown__item user-dropdown__item--danger" type="button" (click)="logout()">
                <i class="pi pi-sign-out"></i>
                Cerrar sesión
              </button>
            </div>
          }
        </div>
      </div>
    </header>

    <main class="page-content">
      <router-outlet />
    </main>
  </div>
</div>

<st-toast />
```

- [ ] **Step 3: Reemplazar `app-shell.component.scss`**

```scss
// src/app/shell/app-shell.component.scss

$sidebar-w:   240px;
$sidebar-col: 64px;
$header-h:    60px;

.shell {
  display: flex;
  min-height: 100vh;
  background: var(--color-bg-page);
}

// ── Sidebar ───────────────────────────────────────────────────

.sidebar {
  width: $sidebar-w;
  min-height: 100vh;
  background: var(--color-bg-sidebar);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  position: sticky;
  top: 0;
  height: 100vh;
  transition: width var(--duration-normal) var(--easing-default);
  overflow: hidden;
  z-index: 100;
}

.shell--collapsed .sidebar { width: $sidebar-col; }

.sidebar__logo {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-5) var(--space-4);
  border-bottom: 1px solid rgba(255,255,255,0.06);
  height: $header-h;
}

.logo-mark {
  width: 32px;
  height: 32px;
  background: var(--color-accent);
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-display);
  font-weight: var(--font-bold);
  font-size: var(--text-lg);
  color: #fff;
  flex-shrink: 0;
}

.logo-text {
  font-family: var(--font-display);
  font-size: var(--text-xl);
  font-weight: var(--font-semibold);
  color: var(--color-text-1);
  letter-spacing: var(--tracking-tight);
  white-space: nowrap;
}

.sidebar__nav {
  flex: 1;
  padding: var(--space-4) var(--space-2);
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  overflow: hidden;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-3);
  border-radius: var(--radius-md);
  color: var(--color-text-sidebar);
  text-decoration: none;
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  transition:
    background var(--duration-fast),
    color var(--duration-fast);
  white-space: nowrap;
  overflow: hidden;

  &:hover { background: rgba(255,255,255,0.06); color: var(--color-text-sidebar-active); }
  &--active {
    background: var(--color-accent) !important;
    color: #fff !important;
  }

  .nav-item__icon { font-size: 0.95rem; flex-shrink: 0; }
  .nav-item__label { overflow: hidden; text-overflow: ellipsis; }
}

.sidebar__footer {
  padding: var(--space-4) var(--space-2);
  border-top: 1px solid rgba(255,255,255,0.06);
}

.collapse-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-3);
  background: none;
  border: none;
  color: var(--color-text-sidebar);
  cursor: pointer;
  border-radius: var(--radius-md);
  transition: background var(--duration-fast), color var(--duration-fast);

  &:hover { background: rgba(255,255,255,0.06); color: var(--color-text-sidebar-active); }
}

// ── Main ──────────────────────────────────────────────────────

.main-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-width: 0;
}

.header {
  height: $header-h;
  background: var(--color-bg-surface);
  border-bottom: 1px solid var(--color-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--space-6);
  position: sticky;
  top: 0;
  z-index: 50;
  gap: var(--space-4);
}

.header__tenant {
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--color-text-2);
}

.header__actions {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.icon-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: none;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  cursor: pointer;
  color: var(--color-text-2);
  transition: background var(--duration-fast), color var(--duration-fast), border-color var(--duration-fast);

  &:hover { background: var(--color-bg-hover); color: var(--color-text-1); border-color: var(--color-border-strong); }
}

.user-menu { position: relative; }

.user-trigger {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  background: none;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: var(--space-2) var(--space-3);
  cursor: pointer;
  color: var(--color-text-1);
  transition: background var(--duration-fast), border-color var(--duration-fast);

  &:hover { background: var(--color-bg-hover); border-color: var(--color-border-strong); }
}

.user-avatar {
  width: 28px;
  height: 28px;
  background: var(--color-accent);
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  color: #fff;
  flex-shrink: 0;
}

.user-name {
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
}

.user-chevron { font-size: 0.6rem; color: var(--color-text-3); }

.user-dropdown {
  position: absolute;
  right: 0;
  top: calc(100% + 8px);
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  min-width: 200px;
  overflow: hidden;
  animation: fadeIn var(--duration-fast) var(--easing-default);
  z-index: 200;

  &__info {
    padding: var(--space-4);
    display: flex;
    flex-direction: column;
    gap: 2px;
  }

  &__name { font-weight: var(--font-semibold); font-size: var(--text-sm); }

  &__sep { border: none; border-top: 1px solid var(--color-border); margin: 0; }

  &__item {
    width: 100%;
    display: flex;
    align-items: center;
    gap: var(--space-3);
    padding: var(--space-3) var(--space-4);
    background: none;
    border: none;
    cursor: pointer;
    font-size: var(--text-sm);
    color: var(--color-text-2);
    transition: background var(--duration-fast), color var(--duration-fast);
    text-align: left;

    &:hover { background: var(--color-bg-hover); color: var(--color-text-1); }
    &--danger:hover { background: var(--color-danger-bg); color: var(--color-danger); }
  }
}

.page-content {
  flex: 1;
  padding: var(--space-8) var(--space-8);
  overflow-y: auto;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-6px); }
  to   { opacity: 1; transform: translateY(0); }
}
```

---

## Task 6: Login Page

**Files:**
- Modify: `src/app/features/auth/login/login.component.ts`
- Modify: `src/app/features/auth/login/login.component.html`
- Modify: `src/app/features/auth/login/login.component.scss`

- [ ] **Step 1: Actualizar `login.component.ts`** — quitar PrimeNG imports

```typescript
// src/app/features/auth/login/login.component.ts
import { Component, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/auth/auth.service';
import { StButtonComponent } from '../../../shared/ui/button/st-button.component';
import { StInputComponent } from '../../../shared/ui/input/st-input.component';

@Component({
  selector: 'st-login',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, ReactiveFormsModule, StButtonComponent, StInputComponent],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  private readonly fb     = inject(FormBuilder);
  private readonly auth   = inject(AuthService);
  private readonly router = inject(Router);

  readonly loading  = signal(false);
  readonly errorMsg = signal<string | null>(null);
  readonly showPass = signal(false);

  readonly form = this.fb.nonNullable.group({
    email:    ['', [Validators.required, Validators.email]],
    password: ['', Validators.required]
  });

  onSubmit(): void {
    if (this.form.invalid || this.loading()) return;
    this.errorMsg.set(null);
    this.loading.set(true);
    this.auth.login(this.form.getRawValue()).subscribe({
      next: () => { this.loading.set(false); this.router.navigate([this.auth.getPostLoginRoute()]); },
      error: (err) => {
        this.loading.set(false);
        this.errorMsg.set(err?.error?.detail ?? 'Credenciales inválidas. Intenta de nuevo.');
      }
    });
  }
}
```

- [ ] **Step 2: Reemplazar `login.component.html`**

```html
<!-- src/app/features/auth/login/login.component.html -->
<div class="login-page">

  <!-- Panel decorativo izquierdo -->
  <div class="login-visual">
    <div class="login-visual__content">
      <div class="login-logo">
        <div class="logo-mark">S</div>
        <span class="logo-name">Stilum</span>
      </div>
      <blockquote class="login-quote">
        <p>"La excelencia en cada cita,<br>la eficiencia en cada gestión."</p>
      </blockquote>
      <div class="login-dots">
        <span class="dot dot--active"></span>
        <span class="dot"></span>
        <span class="dot"></span>
      </div>
    </div>
  </div>

  <!-- Formulario -->
  <div class="login-form-wrap">
    <div class="login-card">
      <div class="login-card__header">
        <h1>Bienvenido</h1>
        <p class="text-muted">Ingresa tus credenciales para continuar</p>
      </div>

      <form [formGroup]="form" (ngSubmit)="onSubmit()" novalidate class="login-form">

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

        <st-input
          label="Contraseña"
          placeholder="••••••••"
          type="password"
          icon="pi-lock"
          formControlName="password"
          [required]="true"
          [hasError]="form.controls.password.invalid && form.controls.password.touched"
          errorMsg="La contraseña es requerida"
        />

        @if (errorMsg()) {
          <div class="login-error">
            <i class="pi pi-exclamation-circle"></i>
            <span>{{ errorMsg() }}</span>
          </div>
        }

        <st-button
          label="Ingresar"
          type="submit"
          [loading]="loading()"
          [disabled]="form.invalid"
          [fullWidth]="true"
          size="lg"
        />

      </form>
    </div>
  </div>
</div>
```

- [ ] **Step 3: Reemplazar `login.component.scss`**

```scss
// src/app/features/auth/login/login.component.scss

.login-page {
  display: flex;
  min-height: 100vh;
  background: var(--color-bg-page);
}

// ── Panel visual ──────────────────────────────────────────────

.login-visual {
  flex: 1;
  background: var(--color-bg-sidebar);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-12);
  position: relative;
  overflow: hidden;

  // Textura decorativa
  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background:
      radial-gradient(circle at 30% 50%, rgba(232,93,74,0.15) 0%, transparent 60%),
      radial-gradient(circle at 80% 20%, rgba(232,93,74,0.08) 0%, transparent 50%);
    pointer-events: none;
  }

  @media (max-width: 768px) { display: none; }
}

.login-visual__content {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: var(--space-10);
  max-width: 380px;
}

.login-logo {
  display: flex;
  align-items: center;
  gap: var(--space-3);

  .logo-mark {
    width: 48px;
    height: 48px;
    background: var(--color-accent);
    border-radius: var(--radius-md);
    display: flex;
    align-items: center;
    justify-content: center;
    font-family: var(--font-display);
    font-weight: var(--font-bold);
    font-size: var(--text-2xl);
    color: #fff;
  }

  .logo-name {
    font-family: var(--font-display);
    font-size: var(--text-3xl);
    font-weight: var(--font-semibold);
    color: var(--color-text-1);
    letter-spacing: var(--tracking-tight);
  }
}

.login-quote {
  border-left: 3px solid var(--color-accent);
  padding-left: var(--space-5);

  p {
    font-family: var(--font-display);
    font-size: var(--text-xl);
    font-weight: var(--font-light);
    color: var(--color-text-2);
    line-height: var(--leading-relaxed);
    font-style: italic;
  }
}

.login-dots {
  display: flex;
  gap: var(--space-2);

  .dot {
    width: 8px;
    height: 8px;
    border-radius: var(--radius-full);
    background: var(--color-text-3);
    transition: all var(--duration-normal);

    &--active { background: var(--color-accent); width: 24px; }
  }
}

// ── Formulario ────────────────────────────────────────────────

.login-form-wrap {
  width: 480px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-8);
  background: var(--color-bg-surface);

  @media (max-width: 768px) { width: 100%; }
}

.login-card {
  width: 100%;
  max-width: 380px;
  display: flex;
  flex-direction: column;
  gap: var(--space-8);

  &__header {
    display: flex;
    flex-direction: column;
    gap: var(--space-2);

    h1 {
      font-size: var(--text-3xl);
      font-weight: var(--font-semibold);
      letter-spacing: var(--tracking-tight);
    }
  }
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

.login-error {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  background: var(--color-danger-bg);
  border: 1px solid var(--color-danger);
  border-radius: var(--radius-md);
  color: var(--color-danger);
  font-size: var(--text-sm);

  i { flex-shrink: 0; }
}
```

---

## Task 7: Dashboard — Stats + Calendario

**Files:**
- Modify: `src/app/features/dashboard/dashboard.component.ts`
- Modify: `src/app/features/dashboard/dashboard.component.html`
- Modify: `src/app/features/dashboard/dashboard.component.scss`

- [ ] **Step 1: Actualizar `dashboard.component.ts`** — quitar PrimeNG imports, usar StButton + StBadge

```typescript
// Cambiar los imports del componente:
// Quitar: ButtonModule, TagModule de primeng
// Añadir:
import { StButtonComponent } from '../../shared/ui/button/st-button.component';
import { StBadgeComponent } from '../../shared/ui/badge/st-badge.component';

// En @Component imports array, reemplazar ButtonModule, TagModule con:
// StButtonComponent, StBadgeComponent
```

- [ ] **Step 2: Reemplazar `dashboard.component.html`**

```html
<!-- src/app/features/dashboard/dashboard.component.html -->

<div class="dash-header">
  <div>
    <h1 class="dash-title">Buenos días, <span class="dash-title__name">{{ nombreUsuario.split(' ')[0] }}</span></h1>
    <p class="text-muted text-sm">{{ fechaFormateada | titlecase }}</p>
  </div>
  <st-button label="Nueva cita" icon="pi-plus" (onClick)="abrirNuevaCita()" />
</div>

<!-- Stats -->
<div class="stats-grid">
  <div class="stat-card">
    <div class="stat-card__icon stat-card__icon--primary">
      <i class="pi pi-calendar"></i>
    </div>
    <div>
      <div class="stat-card__value">{{ citasHoy() }}</div>
      <div class="stat-card__label">Citas hoy</div>
    </div>
  </div>

  <div class="stat-card">
    <div class="stat-card__icon stat-card__icon--warning">
      <i class="pi pi-clock"></i>
    </div>
    <div>
      <div class="stat-card__value">{{ pendientes() }}</div>
      <div class="stat-card__label">Pendientes</div>
    </div>
  </div>

  <div class="stat-card">
    <div class="stat-card__icon stat-card__icon--success">
      <i class="pi pi-check-circle"></i>
    </div>
    <div>
      <div class="stat-card__value">{{ completadas() }}</div>
      <div class="stat-card__label">Completadas</div>
    </div>
  </div>

  <div class="stat-card">
    <div class="stat-card__icon stat-card__icon--accent">
      <i class="pi pi-dollar"></i>
    </div>
    <div>
      <div class="stat-card__value">{{ ingresos() | currency:'COP':'symbol-narrow':'1.0-0' }}</div>
      <div class="stat-card__label">Ingresos hoy</div>
    </div>
  </div>
</div>

<!-- Calendario -->
<div class="section-card">
  <div class="section-header">
    <h2 class="section-title">Agenda del día</h2>
    <div class="section-header__nav">
      <button class="nav-arrow" type="button" (click)="irDiaAnterior()">
        <i class="pi pi-chevron-left"></i>
      </button>
      <button class="date-chip" [class.date-chip--today]="esHoy" type="button" (click)="irHoy()">
        {{ fechaDiaActual | titlecase }}
      </button>
      <button class="nav-arrow" type="button" (click)="irDiaSiguiente()">
        <i class="pi pi-chevron-right"></i>
      </button>
    </div>
  </div>

  <div class="calendar-wrap" [class.calendar-wrap--loading]="loadingCitas()">
    @if (profesionales().length === 0) {
      <div class="empty-state">
        <i class="pi pi-users empty-state__icon"></i>
        <p>No hay profesionales activos</p>
        <span class="text-muted text-sm">Agrega profesionales en la sección Profesionales</span>
      </div>
    } @else {
      <full-calendar [options]="calendarOptions()" />
    }
  </div>
</div>

<!-- Próximas citas -->
<div class="section-card">
  <div class="section-header">
    <h2 class="section-title">Próximas citas</h2>
    <span class="text-muted text-sm">
      @if (loadingCitas()) { Cargando... } @else { {{ citasHoy() }} citas hoy }
    </span>
  </div>

  <div class="citas-list">
    @if (citasProximas().length === 0 && !loadingCitas()) {
      <div class="empty-state">
        <i class="pi pi-calendar-plus empty-state__icon"></i>
        <p class="text-muted">No hay citas próximas</p>
      </div>
    }
    @for (cita of citasProximas(); track cita.id) {
      <div class="cita-row" (click)="citaSeleccionada.set(cita)">
        <div class="cita-row__time">{{ cita.fechaHoraInicio | date:'HH:mm' }}</div>
        <div class="cita-row__bar" [style.background]="'var(--cita-' + cita.estado.toLowerCase().replace('_','-') + '-text)'"></div>
        <div class="cita-row__info">
          <span class="cita-row__cliente">{{ cita.cliente.nombre }}</span>
          <span class="cita-row__meta text-muted text-sm">
            {{ cita.servicio.nombre }} · {{ cita.duracionMin }} min · {{ cita.profesional.nombre }}
          </span>
        </div>
        <st-badge
          [label]="estadoLabel[cita.estado]"
          [variant]="cita.estado.toLowerCase()"
        />
      </div>
    }
  </div>
</div>

<!-- Nueva Cita Modal -->
<st-nueva-cita-modal
  [visible]="mostrarNuevaCita()"
  (visibleChange)="mostrarNuevaCita.set($event)"
  [preProfesionalId]="preSlotProfesional()"
  [preFechaHoraInicio]="preSlotInicio()"
  (citaCreada)="onCitaCreada($event)"
/>

<!-- Panel detalle cita -->
@if (citaSeleccionada()) {
  <div class="overlay" (click)="cerrarDetalle()"></div>
  <aside class="detail-panel">
    <div class="detail-panel__header">
      <h3>Detalle de cita</h3>
      <button class="icon-btn-sm" type="button" (click)="cerrarDetalle()">
        <i class="pi pi-times"></i>
      </button>
    </div>
    <div class="detail-panel__body">
      <div class="detail-row"><span class="detail-label">Estado</span>
        <st-badge [label]="estadoLabel[citaSeleccionada()!.estado]" [variant]="citaSeleccionada()!.estado.toLowerCase()" />
      </div>
      <div class="detail-row"><span class="detail-label">Cliente</span><span>{{ citaSeleccionada()!.cliente.nombre }}</span></div>
      <div class="detail-row"><span class="detail-label">Teléfono</span><span>{{ citaSeleccionada()!.cliente.telefono }}</span></div>
      <div class="detail-row"><span class="detail-label">Servicio</span><span>{{ citaSeleccionada()!.servicio.nombre }}</span></div>
      <div class="detail-row"><span class="detail-label">Profesional</span><span>{{ citaSeleccionada()!.profesional.nombre }}</span></div>
      <div class="detail-row"><span class="detail-label">Inicio</span><span>{{ citaSeleccionada()!.fechaHoraInicio | date:'HH:mm' }}</span></div>
      <div class="detail-row"><span class="detail-label">Duración</span><span>{{ citaSeleccionada()!.duracionMin }} min</span></div>
      @if (citaSeleccionada()!.notas) {
        <div class="detail-row detail-row--block">
          <span class="detail-label">Notas</span>
          <span class="text-sm">{{ citaSeleccionada()!.notas }}</span>
        </div>
      }
    </div>
  </aside>
}
```

- [ ] **Step 3: Reemplazar `dashboard.component.scss`** — ver código completo a continuación

```scss
// src/app/features/dashboard/dashboard.component.scss

.dash-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: var(--space-8);
}

.dash-title {
  font-size: var(--text-3xl);
  font-weight: var(--font-semibold);
  letter-spacing: var(--tracking-tight);
  margin-bottom: var(--space-1);

  &__name { color: var(--color-accent); }
}

// ── Stats ─────────────────────────────────────────────────────

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-4);
  margin-bottom: var(--space-6);

  @media (max-width: 1024px) { grid-template-columns: repeat(2, 1fr); }
  @media (max-width: 480px)  { grid-template-columns: 1fr; }
}

.stat-card {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--space-5);
  display: flex;
  align-items: center;
  gap: var(--space-4);
  transition: box-shadow var(--duration-fast), transform var(--duration-fast);

  &:hover { box-shadow: var(--shadow-md); transform: translateY(-1px); }

  &__icon {
    width: 44px;
    height: 44px;
    border-radius: var(--radius-md);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 1.1rem;
    flex-shrink: 0;

    &--primary { background: var(--color-accent-subtle); color: var(--color-accent); }
    &--warning { background: var(--color-warning-bg); color: var(--color-warning); }
    &--success { background: var(--color-success-bg); color: var(--color-success); }
    &--accent  { background: var(--color-info-bg); color: var(--color-info); }
  }

  &__value {
    font-family: var(--font-display);
    font-size: var(--text-2xl);
    font-weight: var(--font-semibold);
    color: var(--color-text-1);
    line-height: 1;
    margin-bottom: 2px;
  }

  &__label {
    font-size: var(--text-xs);
    color: var(--color-text-2);
    text-transform: uppercase;
    letter-spacing: var(--tracking-wide);
    font-weight: var(--font-medium);
  }
}

// ── Section Cards ─────────────────────────────────────────────

.section-card {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-xl);
  padding: var(--space-6);
  margin-bottom: var(--space-6);
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-5);
}

.section-title {
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  letter-spacing: var(--tracking-tight);
}

.section-header__nav {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.nav-arrow {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: none;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  cursor: pointer;
  color: var(--color-text-2);
  transition: background var(--duration-fast), color var(--duration-fast);
  font-size: 0.7rem;

  &:hover { background: var(--color-bg-hover); color: var(--color-text-1); }
}

.date-chip {
  padding: var(--space-2) var(--space-4);
  background: var(--color-bg-elevated);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  cursor: pointer;
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--color-text-1);
  font-family: var(--font-body);
  transition: all var(--duration-fast);

  &--today {
    background: var(--color-accent-subtle);
    border-color: var(--color-accent);
    color: var(--color-accent);
  }

  &:hover:not(.date-chip--today) {
    background: var(--color-bg-hover);
    border-color: var(--color-border-strong);
  }
}

.calendar-wrap {
  border-radius: var(--radius-lg);
  overflow: hidden;
  transition: opacity var(--duration-fast);

  &--loading { opacity: 0.5; pointer-events: none; }
}

// ── Citas list ────────────────────────────────────────────────

.citas-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.cita-row {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: background var(--duration-fast);

  &:hover { background: var(--color-bg-hover); }

  &__time {
    font-family: var(--font-display);
    font-size: var(--text-base);
    font-weight: var(--font-semibold);
    color: var(--color-text-2);
    width: 48px;
    flex-shrink: 0;
  }

  &__bar {
    width: 3px;
    height: 40px;
    border-radius: var(--radius-full);
    flex-shrink: 0;
  }

  &__info {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 2px;
    min-width: 0;
  }

  &__cliente {
    font-weight: var(--font-medium);
    font-size: var(--text-sm);
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  &__meta {
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}

// ── Empty state ───────────────────────────────────────────────

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-16) var(--space-8);
  gap: var(--space-3);
  text-align: center;

  &__icon {
    font-size: 2.5rem;
    color: var(--color-text-3);
    margin-bottom: var(--space-2);
  }

  p { color: var(--color-text-2); font-size: var(--text-sm); }
}

// ── Detail panel ──────────────────────────────────────────────

.overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.4);
  z-index: 200;
  animation: fadeIn var(--duration-normal);
}

.detail-panel {
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  width: 360px;
  background: var(--color-bg-card);
  border-left: 1px solid var(--color-border);
  z-index: 201;
  display: flex;
  flex-direction: column;
  animation: slideFromRight var(--duration-normal) var(--easing-spring);

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: var(--space-5) var(--space-6);
    border-bottom: 1px solid var(--color-border);

    h3 { font-size: var(--text-lg); font-weight: var(--font-semibold); }
  }

  &__body {
    flex: 1;
    padding: var(--space-6);
    overflow-y: auto;
    display: flex;
    flex-direction: column;
    gap: var(--space-4);
  }
}

.icon-btn-sm {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: none;
  border: none;
  cursor: pointer;
  color: var(--color-text-3);
  border-radius: var(--radius-sm);
  transition: background var(--duration-fast), color var(--duration-fast);

  &:hover { background: var(--color-bg-hover); color: var(--color-text-1); }
}

.detail-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  font-size: var(--text-sm);

  &--block { align-items: flex-start; flex-direction: column; gap: var(--space-2); }
}

.detail-label {
  font-weight: var(--font-medium);
  color: var(--color-text-2);
  text-transform: uppercase;
  font-size: var(--text-xs);
  letter-spacing: var(--tracking-wide);
}

@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
@keyframes slideFromRight { from { transform: translateX(100%); } to { transform: translateX(0); } }
```

---

## Task 8: Modal Nueva Cita

**Files:**
- Modify: `src/app/features/dashboard/nueva-cita-modal.component.ts`
- Modify: `src/app/features/dashboard/nueva-cita-modal.component.html`
- Modify: `src/app/features/dashboard/nueva-cita-modal.component.scss`

- [ ] **Step 1: Actualizar `nueva-cita-modal.component.ts`** — quitar PrimeNG, usar ToastService + StModal

```typescript
// Cambiar imports en el componente:
// Quitar: DialogModule, ButtonModule, SelectModule, InputTextModule, ToastModule, MessageService de primeng
// Añadir:
import { StModalComponent } from '../../shared/ui/modal/st-modal.component';
import { StButtonComponent } from '../../shared/ui/button/st-button.component';
import { StInputComponent  } from '../../shared/ui/input/st-input.component';
import { StSelectComponent, SelectOption } from '../../shared/ui/select/st-select.component';
import { ToastService } from '../../shared/ui/toast/toast.service';

// Quitar: providers: [MessageService]
// Reemplazar: private readonly toast = inject(MessageService) con:
//             private readonly toast = inject(ToastService);

// Cambiar llamadas toast.add({ severity, summary, detail }) a:
// this.toast.add({ severity: 'success', summary: 'Cita agendada' });
// this.toast.add({ severity: 'error', summary: 'Error al agendar', detail: ... });
```

- [ ] **Step 2: Reemplazar `nueva-cita-modal.component.html`**

```html
<!-- src/app/features/dashboard/nueva-cita-modal.component.html -->
<st-modal
  [visible]="visible"
  header="Nueva cita"
  width="540px"
  (visibleChange)="visibleChange.emit($event)"
>
  <form [formGroup]="form" (ngSubmit)="guardar()" novalidate class="cita-form">

    <div class="form-row">
      <st-select
        label="Profesional"
        placeholder="Selecciona el profesional"
        [options]="profesionalOptions()"
        formControlName="profesionalId"
        [required]="true"
        (ngModelChange)="onProfesionalChange()"
      />
      <st-select
        label="Servicio"
        placeholder="Selecciona el servicio"
        [options]="servicioOptions()"
        formControlName="servicioId"
        [required]="true"
        (ngModelChange)="onServicioChange()"
      />
    </div>

    <div class="field">
      <label class="st-label">Horario disponible <span class="st-label__req">*</span></label>
      @if (loadingSlots()) {
        <div class="slots-loading">
          <span class="slot-spinner"></span>
          <span class="text-sm text-muted">Buscando disponibilidad…</span>
        </div>
      } @else if (slotOptions().length === 0 && form.value.profesionalId && form.value.servicioId) {
        <div class="slots-empty">
          <i class="pi pi-calendar-times"></i>
          <span class="text-sm text-muted">Sin horarios disponibles para hoy</span>
        </div>
      } @else {
        <st-select
          placeholder="Selecciona el horario"
          [options]="slotOptions()"
          formControlName="slot"
          [required]="true"
        />
      }
    </div>

    <hr class="divider" />

    <div class="form-section-label">Datos del cliente</div>

    <div class="form-row">
      <st-input
        label="Teléfono"
        placeholder="3001234567"
        icon="pi-phone"
        formControlName="clienteTelefono"
        [required]="true"
      />
      <st-input
        label="Nombre"
        placeholder="Nombre del cliente"
        icon="pi-user"
        formControlName="clienteNombre"
      />
    </div>

    <st-input
      label="Notas"
      placeholder="Observaciones opcionales"
      formControlName="notas"
    />

    <div class="form-actions">
      <st-button label="Cancelar" variant="ghost" type="button" (onClick)="cerrar()" />
      <st-button
        label="Agendar cita"
        icon="pi-calendar-plus"
        type="submit"
        [loading]="saving()"
        [disabled]="form.invalid"
      />
    </div>

  </form>
</st-modal>
```

- [ ] **Step 3: Reemplazar `nueva-cita-modal.component.scss`**

```scss
// src/app/features/dashboard/nueva-cita-modal.component.scss

.cita-form {
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-4);

  @media (max-width: 480px) { grid-template-columns: 1fr; }
}

.field { display: flex; flex-direction: column; gap: var(--space-2); }

.st-label {
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--color-text-1);
  letter-spacing: var(--tracking-wide);
  text-transform: uppercase;

  &__req { color: var(--color-accent); margin-left: 2px; }
}

.slots-loading, .slots-empty {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  background: var(--color-bg-elevated);
  border: 1.5px dashed var(--color-border-strong);
  border-radius: var(--radius-md);
  color: var(--color-text-2);
}

.slot-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid var(--color-border-strong);
  border-top-color: var(--color-accent);
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
  flex-shrink: 0;
}

@keyframes spin { to { transform: rotate(360deg); } }

.divider {
  border: none;
  border-top: 1px solid var(--color-border);
  margin: var(--space-1) 0;
}

.form-section-label {
  font-size: var(--text-xs);
  font-weight: var(--font-semibold);
  text-transform: uppercase;
  letter-spacing: var(--tracking-wider);
  color: var(--color-text-3);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  padding-top: var(--space-2);
}
```

---

## Task 9: Listas (Servicios, Profesionales, Tenants)

**Files:**
- Modify: `src/app/features/tenant/servicios/servicios-list.component.ts` + `.html` + `.scss`
- Modify: `src/app/features/tenant/profesionales/profesionales-list.component.ts` + `.html` + `.scss`
- Modify: `src/app/features/admin/tenants/tenants-list.component.ts` + `.html` + `.scss`

Las tres listas siguen el mismo patrón. Solo se documenta Servicios en detalle — repetir para las otras dos.

- [ ] **Step 1: Actualizar `servicios-list.component.ts`** — quitar PrimeNG, añadir shared UI

```typescript
// Quitar imports: TableModule, ButtonModule, TagModule, DialogModule, InputTextModule,
//                InputNumberModule, ToastModule, TooltipModule, MessageService
// Añadir:
import { StButtonComponent } from '../../../shared/ui/button/st-button.component';
import { StInputComponent }  from '../../../shared/ui/input/st-input.component';
import { StModalComponent }  from '../../../shared/ui/modal/st-modal.component';
import { StBadgeComponent }  from '../../../shared/ui/badge/st-badge.component';
import { ToastService }      from '../../../shared/ui/toast/toast.service';

// Quitar: providers: [MessageService]
// Reemplazar: inject(MessageService) con inject(ToastService)
// Reemplazar: this.toast.add({ severity, summary, detail }) — misma API
```

- [ ] **Step 2: Reemplazar HTML de `servicios-list.component.html`**

```html
<!-- src/app/features/tenant/servicios/servicios-list.component.html -->
<div class="list-page">
  <div class="list-header">
    <div>
      <h1>Servicios</h1>
      <p class="text-muted text-sm">Catálogo de servicios del salón</p>
    </div>
    <st-button label="Nuevo servicio" icon="pi-plus" (onClick)="abrirCrear()" />
  </div>

  <div class="list-card">
    @if (loading()) {
      <div class="table-loading">
        <span class="spinner"></span>
        <span class="text-muted text-sm">Cargando servicios…</span>
      </div>
    } @else if (servicios().length === 0) {
      <div class="empty-state">
        <i class="pi pi-list empty-state__icon"></i>
        <p>No hay servicios registrados</p>
        <st-button label="Crear primer servicio" variant="secondary" (onClick)="abrirCrear()" />
      </div>
    } @else {
      <table class="st-table">
        <thead>
          <tr>
            <th>Nombre</th>
            <th>Duración</th>
            <th>Precio</th>
            <th>Estado</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          @for (s of servicios(); track s.id) {
            <tr>
              <td>
                <span class="table-primary">{{ s.nombre }}</span>
                @if (s.descripcion) { <span class="table-secondary">{{ s.descripcion }}</span> }
              </td>
              <td><span class="chip">{{ s.duracionMin }} min</span></td>
              <td>{{ s.precio | currency:'COP':'symbol-narrow':'1.0-0' }}</td>
              <td>
                <st-badge [label]="s.activo ? 'Activo' : 'Inactivo'" [variant]="s.activo ? 'success' : 'default'" />
              </td>
              <td class="table-actions">
                <button class="action-btn" type="button" (click)="abrirEditar(s)" title="Editar">
                  <i class="pi pi-pencil"></i>
                </button>
                <button class="action-btn" [class.action-btn--danger]="s.activo" type="button" (click)="toggleEstado(s)" [title]="s.activo ? 'Desactivar' : 'Activar'">
                  <i [class]="'pi ' + (s.activo ? 'pi-eye-slash' : 'pi-eye')"></i>
                </button>
              </td>
            </tr>
          }
        </tbody>
      </table>
    }
  </div>
</div>

<!-- Modal -->
<st-modal [visible]="showForm()" [header]="dialogHeader" (visibleChange)="showForm.set($event)">
  <form [formGroup]="form" (ngSubmit)="guardar()" novalidate class="modal-form">

    <st-input label="Nombre" placeholder="Ej: Corte de cabello" formControlName="nombre" [required]="true" />
    <st-input label="Descripción" placeholder="Descripción opcional" formControlName="descripcion" />

    <div class="form-row">
      <st-input label="Duración (min)" type="number" placeholder="30" formControlName="duracionMin" [required]="true" />
      <st-input label="Precio (COP)" type="number" placeholder="50000" formControlName="precio" [required]="true" />
    </div>

    <div class="modal-actions">
      <st-button label="Cancelar" variant="ghost" type="button" (onClick)="showForm.set(false)" />
      <st-button [label]="editTarget() ? 'Guardar cambios' : 'Crear servicio'" type="submit" [loading]="saving()" [disabled]="form.invalid" />
    </div>
  </form>
</st-modal>
```

- [ ] **Step 3: Crear SCSS compartido para tablas en `styles.scss` o archivo separado**

```scss
// Agregar al final de src/styles/styles.scss:

// ── Shared table styles ──────────────────────────────────────
.list-page {
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
}

.list-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-4);

  h1 { font-size: var(--text-2xl); font-weight: var(--font-semibold); letter-spacing: var(--tracking-tight); }
}

.list-card {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-xl);
  overflow: hidden;
}

.st-table {
  width: 100%;
  border-collapse: collapse;
  font-size: var(--text-sm);

  thead tr {
    border-bottom: 1px solid var(--color-border);
    background: var(--color-bg-elevated);
  }

  th {
    padding: var(--space-3) var(--space-5);
    text-align: left;
    font-size: var(--text-xs);
    font-weight: var(--font-semibold);
    text-transform: uppercase;
    letter-spacing: var(--tracking-wide);
    color: var(--color-text-2);
    white-space: nowrap;
  }

  tbody tr {
    border-bottom: 1px solid var(--color-border);
    transition: background var(--duration-fast);

    &:last-child { border-bottom: none; }
    &:hover { background: var(--color-bg-hover); }
  }

  td {
    padding: var(--space-4) var(--space-5);
    vertical-align: middle;
    color: var(--color-text-1);
  }
}

.table-primary { display: block; font-weight: var(--font-medium); }
.table-secondary { display: block; font-size: var(--text-xs); color: var(--color-text-2); margin-top: 2px; }

.table-actions {
  display: flex;
  gap: var(--space-1);
  justify-content: flex-end;
}

.action-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: none;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  cursor: pointer;
  color: var(--color-text-2);
  font-size: 0.8rem;
  transition: all var(--duration-fast);

  &:hover { background: var(--color-bg-hover); color: var(--color-text-1); border-color: var(--color-border-strong); }
  &--danger:hover { background: var(--color-danger-bg); color: var(--color-danger); border-color: var(--color-danger); }
}

.chip {
  display: inline-flex;
  padding: 2px var(--space-3);
  background: var(--color-bg-elevated);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
  font-weight: var(--font-medium);
  color: var(--color-text-2);
}

.table-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-3);
  padding: var(--space-16);
}

.spinner {
  width: 18px;
  height: 18px;
  border: 2px solid var(--color-border-strong);
  border-top-color: var(--color-accent);
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

.modal-form {
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-4);
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  padding-top: var(--space-2);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-16);
  text-align: center;

  &__icon { font-size: 2.5rem; color: var(--color-text-3); }
  p { color: var(--color-text-2); }
}
```

- [ ] **Step 4: Repetir Steps 1-2 para `profesionales-list.component.ts` + `.html`**

Mismos cambios de imports. En el HTML usar el mismo patrón de tabla con columnas: Nombre / Especialidad / Estado / Acciones.

- [ ] **Step 5: Repetir Steps 1-2 para `tenants-list.component.ts` + `.html`**

Mismos cambios de imports. Columnas: Nombre / Email / Plan / Estado / Acciones.

---

## Task 10: Limpieza de `app.config.ts`

**Files:**
- Modify: `src/app/app.config.ts`

- [ ] **Step 1: Quitar providePrimeNG y el theme Lara de `app.config.ts`**

```typescript
// src/app/app.config.ts — reemplazar contenido completo
import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { routes } from './app.routes';
import { jwtInterceptor } from './core/interceptors/jwt.interceptor';
import { errorInterceptor } from './core/interceptors/error.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes, withComponentInputBinding()),
    provideHttpClient(withInterceptors([jwtInterceptor, errorInterceptor])),
    provideAnimationsAsync()
  ]
};
```

- [ ] **Step 2: Verificar que la app compila**

```bash
cd "c:\Users\PC\OneDrive\Documentos\Claude\Projects\Sistema de citas Peluqueria\stilum-frontend"
npm run build 2>&1 | tail -30
```

Esperado: `✔ Application bundle generation complete.`

- [ ] **Step 3: Levantar dev server y verificar visualmente**

```bash
npm start
```

Navegar a `http://localhost:4200` y verificar:
- Login se ve con panel visual izquierdo + formulario custom
- Sidebar con fondo oscuro + acento coral en item activo
- Dashboard con stat cards y calendario
- Modal de nueva cita sin ningún componente PrimeNG visible

---

## Self-Review

**Spec coverage:**
- ✅ Google Fonts Fraunces + DM Sans
- ✅ Color system coral como acento primario
- ✅ Dark/light con las variables CSS actualizadas
- ✅ StButton reemplaza p-button en todos los componentes
- ✅ StInput reemplaza p-inputtext
- ✅ StSelect reemplaza p-select
- ✅ StModal reemplaza p-dialog
- ✅ StBadge reemplaza p-tag
- ✅ ToastService reemplaza MessageService de PrimeNG
- ✅ Sidebar y Header sin PrimeNG
- ✅ Login rediseñado con panel visual
- ✅ Dashboard rediseñado
- ✅ Modal de cita rediseñado
- ✅ Listas con tabla HTML nativa
- ✅ app.config.ts limpiado

**Brechas identificadas:**
- Los SCSS individuales de profesionales-list y tenants-list no se documentaron en detalle (se cubre con el patrón compartido en styles.scss)
- La tabla de Profesionales tiene FormArray de horarios — el HTML específico debe replicar el patrón del servicios HTML

**Tipo/nombre consistency:** StModal usa `visible`, `header`, `visibleChange` — consistente con el uso en dashboard y nueva-cita-modal. ToastService usa misma firma que MessageService.
