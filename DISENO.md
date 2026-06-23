# Sistema de Diseño — Stilum Pro

**Versión:** 1.1  
**Fecha:** 2026-06-07  
**Tono:** Elegante · Minimalista · Profesional

---

## 1. Identidad Visual

### Concepto

El diseño habla el lenguaje de la peluquería de alta gama: limpieza absoluta, jerarquía clara y un toque dorado que evoca precisión y oficio. Nada sobra. Cada elemento tiene una razón de estar ahí.

- **Sidebar oscuro** — ancla la navegación, da peso y seriedad
- **Superficies blancas** — el contenido respira, nada compite con la información
- **Oro como acento único** — marca lo importante sin gritar
- **Tipografía con jerarquía fuerte** — el usuario sabe dónde mirar

### Nombre de la App

**Stilum Pro** — latín de *stilus* (instrumento de precisión). Aplica tanto a barbería como a salón de belleza.

---

## 2. Sistema de Tokens — Fuente Única de Verdad

> **Regla absoluta:** ningún color, tamaño de fuente, espaciado ni tiempo de animación se escribe directamente en un componente. Todo va por token. Crear un nuevo tema es cambiar únicamente los archivos de tokens.

### Estructura de archivos de tokens

```
src/styles/
├── tokens/
│   ├── _colors.scss        ← valores base (solo aquí se escriben hex)
│   ├── _themes.scss        ← tokens semánticos por tema (light/dark)
│   ├── _typography.scss    ← escala de fuentes y familias
│   ├── _spacing.scss       ← escala de espaciado
│   ├── _animation.scss     ← duraciones y easings
│   └── _index.scss         ← exporta todo
├── _primeng-theme.scss
└── styles.scss
```

---

### 2.1 Colores base — `_colors.scss`

Los únicos hex del proyecto. Nadie importa este archivo directamente excepto `_themes.scss`.

```scss
// styles/tokens/_colors.scss
// ⚠️ Este es el ÚNICO archivo donde se permiten valores hex.
// Todo lo demás usa variables de _themes.scss.

$c-brand-dark:      #1C1C2E;
$c-brand-dark-2:    #2A2A3E;
$c-brand-dark-deep: #080810;
$c-gold:            #C9A84C;
$c-gold-light:      #E8D5A0;
$c-gold-dark:       #A07828;

$c-white:           #FFFFFF;
$c-warm-50:         #F8F7F4;
$c-warm-100:        #F2F0EB;
$c-warm-200:        #E8E6DF;

$c-dark-50:         #0D0D1A;
$c-dark-100:        #1A1A2E;
$c-dark-200:        #22223A;

$c-gray-400:        #A0A0AD;
$c-gray-500:        #6B6B7B;
$c-gray-900:        #1C1C1E;
$c-gray-f5:         #F0F0F5;

$c-green-400:       #22C55E;
$c-green-700:       #15803D;
$c-green-800:       #16A34A;
$c-green-light:     #86EFAC;

$c-blue-400:        #3B82F6;
$c-blue-700:        #1D4ED8;
$c-blue-light:      #93C5FD;

$c-red-400:         #EF4444;
$c-red-700:         #B91C1C;
$c-red-light:       #FCA5A5;
```

---

### 2.2 Tokens semánticos por tema — `_themes.scss`

Los componentes solo usan estas variables. Nunca las de `_colors.scss`.

```scss
// styles/tokens/_themes.scss
@use 'colors' as c;

// ── TEMA CLARO ────────────────────────────────────────
:root,
[data-theme="light"] {
  // Marca (igual en ambos temas)
  --color-sidebar-bg:       #{c.$c-brand-dark};
  --color-sidebar-hover:    #{c.$c-brand-dark-2};
  --color-brand-gold:       #{c.$c-gold};
  --color-brand-gold-light: #{c.$c-gold-light};
  --color-brand-gold-dark:  #{c.$c-gold-dark};
  --color-brand-gold-muted: rgba(201, 168, 76, 0.12);
  --color-brand-gold-border:rgba(201, 168, 76, 0.25);

  // Superficies
  --color-page-bg:          #{c.$c-warm-50};
  --color-card-bg:          #{c.$c-white};
  --color-card-hover:       #{c.$c-warm-100};
  --color-topbar-bg:        #{c.$c-white};
  --color-metrics-bg:       #{c.$c-white};
  --color-cal-cell-bg:      #{c.$c-white};
  --color-cal-alt-bg:       #{c.$c-warm-50};

  // Texto
  --color-text-1:           #{c.$c-gray-900};
  --color-text-2:           #{c.$c-gray-500};
  --color-text-3:           #{c.$c-gray-400};

  // Bordes
  --color-border:           rgba(0, 0, 0, 0.08);
  --color-border-strong:    rgba(0, 0, 0, 0.14);

  // Citas — fondo/texto para tema claro
  --cita-activa-bg:         rgba(201, 168, 76, 0.12);
  --cita-activa-border:     #{c.$c-gold};
  --cita-activa-text:       #{c.$c-gold-dark};
  --cita-activa-subtext:    #{c.$c-gold};

  --cita-pendiente-bg:      rgba(59, 130, 246, 0.08);
  --cita-pendiente-border:  #{c.$c-blue-400};
  --cita-pendiente-text:    #{c.$c-blue-700};
  --cita-pendiente-subtext: #{c.$c-blue-400};

  --cita-completada-bg:     rgba(34, 197, 94, 0.09);
  --cita-completada-border: #{c.$c-green-400};
  --cita-completada-text:   #{c.$c-green-700};
  --cita-completada-subtext:#{c.$c-green-800};

  --cita-cancelada-bg:      rgba(239, 68, 68, 0.08);
  --cita-cancelada-border:  #{c.$c-red-400};
  --cita-cancelada-text:    #{c.$c-red-700};

  // Estado profesional
  --color-status-active:    #{c.$c-green-400};
  --color-status-waiting:   #{c.$c-gold};
  --color-status-off:       rgba(0, 0, 0, 0.15);

  // PrimeNG bridge
  --surface-0:              #{c.$c-white};
  --surface-50:             #{c.$c-warm-50};
  --surface-100:            #{c.$c-warm-100};
  --surface-200:            #{c.$c-warm-200};
  --text-color:             #{c.$c-gray-900};
  --text-color-secondary:   #{c.$c-gray-500};
}

// ── TEMA OSCURO ───────────────────────────────────────
[data-theme="dark"] {
  // Marca
  --color-sidebar-bg:       #{c.$c-brand-dark-deep};
  --color-sidebar-hover:    #{c.$c-brand-dark};
  --color-brand-gold:       #{c.$c-gold};
  --color-brand-gold-light: #{c.$c-gold-light};
  --color-brand-gold-dark:  #{c.$c-gold-dark};
  --color-brand-gold-muted: rgba(201, 168, 76, 0.15);
  --color-brand-gold-border:rgba(201, 168, 76, 0.20);

  // Superficies
  --color-page-bg:          #{c.$c-dark-50};
  --color-card-bg:          #{c.$c-dark-100};
  --color-card-hover:       #{c.$c-dark-200};
  --color-topbar-bg:        #{c.$c-dark-100};
  --color-metrics-bg:       #{c.$c-dark-100};
  --color-cal-cell-bg:      #{c.$c-dark-100};
  --color-cal-alt-bg:       #{c.$c-dark-50};

  // Texto
  --color-text-1:           #{c.$c-gray-f5};
  --color-text-2:           rgba(240, 240, 245, 0.50);
  --color-text-3:           rgba(240, 240, 245, 0.25);

  // Bordes
  --color-border:           rgba(255, 255, 255, 0.06);
  --color-border-strong:    rgba(255, 255, 255, 0.10);

  // Citas — colores más luminosos para fondos oscuros
  --cita-activa-bg:         rgba(201, 168, 76, 0.15);
  --cita-activa-border:     #{c.$c-gold};
  --cita-activa-text:       #{c.$c-gold-light};
  --cita-activa-subtext:    #{c.$c-gold};

  --cita-pendiente-bg:      rgba(59, 130, 246, 0.13);
  --cita-pendiente-border:  #{c.$c-blue-400};
  --cita-pendiente-text:    #{c.$c-blue-light};
  --cita-pendiente-subtext: #{c.$c-blue-400};

  --cita-completada-bg:     rgba(34, 197, 94, 0.13);
  --cita-completada-border: #{c.$c-green-400};
  --cita-completada-text:   #{c.$c-green-light};
  --cita-completada-subtext:#{c.$c-green-400};

  --cita-cancelada-bg:      rgba(239, 68, 68, 0.13);
  --cita-cancelada-border:  #{c.$c-red-400};
  --cita-cancelada-text:    #{c.$c-red-light};

  // Estado profesional
  --color-status-active:    #{c.$c-green-400};
  --color-status-waiting:   #{c.$c-gold};
  --color-status-off:       rgba(255, 255, 255, 0.12);

  // PrimeNG bridge
  --surface-0:              #{c.$c-dark-100};
  --surface-50:             #{c.$c-dark-50};
  --surface-100:            #{c.$c-brand-dark-deep};
  --surface-200:            #050508;
  --text-color:             #{c.$c-gray-f5};
  --text-color-secondary:   rgba(240,240,245,0.5);
}
```

---

### 2.3 Tipografía — `_typography.scss`

```scss
// styles/tokens/_typography.scss

// Familias — SOLO aquí se nombran las fuentes
$font-display: 'Playfair Display', serif;
$font-body:    'Inter', sans-serif;
$font-mono:    'JetBrains Mono', monospace;

:root {
  --font-display: #{$font-display};
  --font-body:    #{$font-body};
  --font-mono:    #{$font-mono};

  // Escala tipográfica (base 13px)
  --text-xs:   10px;   // etiquetas de hora, badges
  --text-sm:   11px;   // texto de citas, subtexto
  --text-base: 13px;   // navegación, labels, body principal
  --text-md:   15px;   // nombre del negocio, botones
  --text-lg:   18px;   // títulos de sección
  --text-xl:   22px;   // título de página, métricas grandes
  --text-2xl:  28px;   // pantalla de turnos
  --text-3xl:  36px;   // cliente en atención (display)

  // Pesos — solo dos
  --font-regular: 400;
  --font-medium:  500;

  // Interlineado
  --leading-tight:  1.2;
  --leading-normal: 1.5;
  --leading-loose:  1.7;

  // Espaciado de letras
  --tracking-tight: -0.01em;
  --tracking-wide:  0.05em;
  --tracking-wider: 0.08em;   // para labels uppercase
}
```

---

### 2.4 Espaciado — `_spacing.scss`

```scss
// styles/tokens/_spacing.scss
// Escala de 4px. Nunca usar valores fuera de esta escala.

:root {
  --space-1:  4px;
  --space-2:  8px;
  --space-3:  12px;
  --space-4:  16px;
  --space-5:  20px;
  --space-6:  24px;
  --space-8:  32px;
  --space-10: 40px;
  --space-12: 48px;

  // Layout fijo
  --sidebar-width:   200px;
  --topbar-height:    56px;
  --metrics-height:   60px;

  // Radios
  --radius-sm:   4px;
  --radius-md:   8px;
  --radius-lg:  12px;
  --radius-xl:  16px;
  --radius-full: 9999px;
}
```

---

### 2.5 Animaciones — `_animation.scss`

```scss
// styles/tokens/_animation.scss

:root {
  --duration-instant: 80ms;
  --duration-fast:   150ms;
  --duration-normal: 250ms;
  --duration-slow:   400ms;

  --easing-default: ease;
  --easing-in:      ease-in;
  --easing-out:     ease-out;
  --easing-spring:  cubic-bezier(0.34, 1.56, 0.64, 1);
}
```

---

### 2.6 Tokens TypeScript — `design-tokens.ts`

Para los casos donde TypeScript necesita los valores (FullCalendar, WebSocket events, tests), los tokens se exportan como constantes:

```typescript
// src/shared/tokens/design-tokens.ts
// Lee los valores desde variables CSS en runtime → siempre sincronizado con el tema activo

function token(name: string): string {
  return getComputedStyle(document.documentElement)
    .getPropertyValue(name).trim();
}

export const DesignTokens = {
  get cita() {
    return {
      EN_ATENCION:  { bg: token('--cita-activa-bg'),    border: token('--cita-activa-border'),    text: token('--cita-activa-text') },
      PENDIENTE:    { bg: token('--cita-pendiente-bg'), border: token('--cita-pendiente-border'), text: token('--cita-pendiente-text') },
      CONFIRMADA:   { bg: token('--cita-pendiente-bg'), border: token('--cita-pendiente-border'), text: token('--cita-pendiente-text') },
      COMPLETADA:   { bg: token('--cita-completada-bg'),border: token('--cita-completada-border'),text: token('--cita-completada-text') },
      CANCELADA:    { bg: token('--cita-cancelada-bg'), border: token('--cita-cancelada-border'), text: token('--cita-cancelada-text') },
      REPROGRAMADA: { bg: token('--cita-cancelada-bg'), border: token('--cita-cancelada-border'), text: token('--cita-cancelada-text') },
      NO_ASISTIO:   { bg: token('--cita-cancelada-bg'), border: token('--cita-cancelada-border'), text: token('--cita-cancelada-text') },
    };
  },
  get brand() {
    return {
      gold:  token('--color-brand-gold'),
      dark:  token('--color-sidebar-bg'),
    };
  }
} as const;

// Uso en cita-rules.utils.ts — sin ningún string de color:
colorCalendario: (estado: EstadoCita) => DesignTokens.cita[estado]?.border
```

> **Por qué `get` y no objeto estático:** el tema puede cambiar en runtime. El `get` lee el valor CSS al momento del acceso, siempre reflejando el tema activo.

---

### Uso del color — regla de oro

> El dorado (`--color-brand-gold`) es el único color de acento. No se añade un segundo color de marca. Los demás colores son funcionales (estados, alertas).

---

## 3. Tipografía

### Fuentes

```html
<!-- En index.html -->
<link rel="preconnect" href="https://fonts.googleapis.com">
<link href="https://fonts.googleapis.com/css2?family=Playfair+Display:wght@500;600&family=Inter:wght@400;500&display=swap" rel="stylesheet">
```

```scss
// _typography.scss
$font-display: 'Playfair Display', serif;   // títulos de sección, nombre del negocio
$font-body:    'Inter', sans-serif;          // todo lo demás
```

### Escala tipográfica

| Elemento | Fuente | Tamaño | Peso |
|---------|--------|--------|------|
| Nombre del negocio (sidebar) | Playfair Display | 15px | 500 |
| Título de página | Playfair Display | 22px | 500 |
| Título de sección | Inter | 13px | 500 — uppercase, letter-spacing 0.8px |
| Nombre de profesional | Inter | 13px | 500 |
| Métrica principal | Inter | 24px | 500 |
| Texto de cita (calendario) | Inter | 11px | 500 (cliente) / 400 (servicio) |
| Etiqueta de hora | Inter | 10px | 400 |
| Navegación sidebar | Inter | 13px | 400 |
| Labels de input | Inter | 13px | 500 |

---

## 4. Espaciado y Layout

```scss
// Escala de espaciado (múltiplos de 4px)
$space-1: 4px;
$space-2: 8px;
$space-3: 12px;
$space-4: 16px;
$space-5: 20px;
$space-6: 24px;
$space-8: 32px;
$space-10: 40px;

// Layout principal
$sidebar-width:    200px;
$topbar-height:    56px;
$metrics-height:   60px;
$content-padding:  20px;

// Radios de borde
$radius-sm: 4px;
$radius-md: 8px;
$radius-lg: 12px;
$radius-full: 9999px;  // pills y avatares
```

---

## 5. Estructura de Layout

```
┌─────────────────────────────────────────────────────┐
│  SIDEBAR (200px · fondo oscuro #1C1C2E)             │
│  ┌──────────────────────────────────────────────┐   │
│  │  [Logo mark] Stilum Pro                      │   │
│  │  ─────────────────────────                   │   │
│  │  PRINCIPAL                                   │   │
│  │  > Dashboard          (item activo = dorado) │   │
│  │    Citas                                     │   │
│  │    Profesionales                             │   │
│  │    Servicios                                 │   │
│  │  REPORTES                                    │   │
│  │    Ingresos                                  │   │
│  │    Historial                                 │   │
│  │  ─────────────────────────                   │   │
│  │  [Chip] Peluquería Styles   ← tenant activo │   │
│  └──────────────────────────────────────────────┘   │
│                                                      │
│  MAIN CONTENT (flex: 1 · fondo #F8F7F4)             │
│  ┌──────────────────────────────────────────────┐   │
│  │  TOPBAR (56px · blanco)                      │   │
│  │  [← →] Lunes 8 jun   [Día|Semana] [+ Cita]  │   │
│  ├──────────────────────────────────────────────┤   │
│  │  MÉTRICAS (60px · blanco)                    │   │
│  │  12 citas · 5 completadas · $480.000         │   │
│  ├──────────────────────────────────────────────┤   │
│  │  CALENDARIO (flex: 1 · scroll)               │   │
│  │  [hora] [Carlos] [Ana] [Luis]                │   │
│  │  09:00  [████████] [        ] [███]          │   │
│  │  09:30  [        ] [████████] [   ]          │   │
│  │  10:00  [████████] [        ] [███]          │   │
│  └──────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────┘
```

---

## 6. Componentes

### Sidebar

```scss
.sidebar {
  width: $sidebar-width;
  background: var(--color-brand-dark);
  display: flex;
  flex-direction: column;
  height: 100vh;
  position: sticky;
  top: 0;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 20px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.55);
  border-right: 2px solid transparent;
  transition: all 0.15s ease;

  &:hover {
    color: rgba(255, 255, 255, 0.85);
    background: rgba(255, 255, 255, 0.04);
  }

  &.active {
    color: var(--color-brand-gold);
    background: rgba(201, 168, 76, 0.08);
    border-right-color: var(--color-brand-gold);
  }
}

.nav-section-label {
  font-size: 9px;
  color: rgba(255, 255, 255, 0.3);
  letter-spacing: 1.5px;
  text-transform: uppercase;
  padding: 8px 20px 4px;
}
```

### Botón primario (dorado)

```scss
.btn-primary {
  background: var(--color-brand-gold);
  color: var(--color-brand-dark);
  border: none;
  border-radius: $radius-md;
  padding: 8px 16px;
  font-size: 13px;
  font-weight: 500;
  font-family: $font-body;
  cursor: pointer;
  transition: background 0.15s;

  &:hover { background: var(--color-brand-gold-light); }
  &:active { transform: scale(0.98); }
}
```

### Tarjeta de profesional (cabecera de columna)

```scss
.prof-col-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  background: var(--color-surface-card);
  border-bottom: 1px solid var(--color-border-default);
  position: sticky;
  top: 0;
  z-index: 2;
}

.prof-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 500;
}
```

### Bloque de cita en calendario

```scss
.appointment-block {
  position: absolute;
  left: 3px;
  right: 3px;
  border-radius: 5px;
  padding: 4px 8px;
  border-left: 3px solid;
  overflow: hidden;
  cursor: pointer;
  transition: filter 0.1s;

  &:hover { filter: brightness(0.95); }

  .client-name { font-size: 11px; font-weight: 500; }
  .service-name { font-size: 10px; margin-top: 1px; }

  // Estados
  &.completada {
    background: rgba(34, 197, 94, 0.10);
    border-left-color: var(--color-cita-completada);
    .client-name { color: #15803D; }
    .service-name { color: #16A34A; }
  }
  &.en-atencion {
    background: var(--color-brand-gold-muted);
    border-left-color: var(--color-brand-gold);
    .client-name { color: #92600A; }
    .service-name { color: #A07828; }
  }
  &.pendiente {
    background: rgba(59, 130, 246, 0.08);
    border-left-color: var(--color-cita-pendiente);
    .client-name { color: #1D4ED8; }
    .service-name { color: #2563EB; }
  }
  &.cancelada {
    background: rgba(239, 68, 68, 0.08);
    border-left-color: var(--color-cita-cancelada);
    opacity: 0.6;
    .client-name { color: #B91C1C; text-decoration: line-through; }
  }
}
```

### Tarjetas de métricas

```scss
.metric-card {
  padding: 12px 24px 12px 0;
  margin-right: 24px;
  border-right: 0.5px solid var(--color-border-default);

  &:last-child { border-right: none; }

  .value {
    font-size: 22px;
    font-weight: 500;
    color: var(--color-text-primary);
    line-height: 1;

    &.highlight { color: var(--color-brand-gold); }
  }

  .label {
    font-size: 10px;
    color: var(--color-text-secondary);
    text-transform: uppercase;
    letter-spacing: 0.5px;
    margin-top: 4px;
  }
}
```

### Badge de estado

```scss
.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 3px 8px;
  border-radius: $radius-full;
  font-size: 11px;
  font-weight: 500;

  .dot { width: 6px; height: 6px; border-radius: 50%; }

  &.active   { background: rgba(34,197,94,0.1);  color: #15803D; .dot { background: #22C55E; } }
  &.waiting  { background: var(--color-brand-gold-muted); color: #92600A; .dot { background: var(--color-brand-gold); } }
  &.offline  { background: rgba(0,0,0,0.05); color: var(--color-text-secondary); .dot { background: #D1D1D6; } }
}
```

---

## 7. Temas Claro y Oscuro

### Tokens por tema

El sistema de temas se basa en CSS custom properties que cambian según el atributo `data-theme` en el `<html>`. El sidebar **siempre es oscuro** (es la marca), solo cambia el área de contenido principal.

```scss
// styles/_themes.scss

// ── TEMA CLARO (por defecto) ──────────────────────────
:root,
[data-theme="light"] {
  --color-page-bg:      #F8F7F4;
  --color-card-bg:      #FFFFFF;
  --color-card-hover:   #F2F0EB;
  --color-topbar-bg:    #FFFFFF;
  --color-metrics-bg:   #FFFFFF;
  --color-cal-cell-bg:  #FFFFFF;
  --color-cal-alt-bg:   #F8F7F4;
  --color-border:       rgba(0, 0, 0, 0.08);
  --color-border-strong:rgba(0, 0, 0, 0.14);
  --color-text-1:       #1C1C1E;
  --color-text-2:       #6B6B7B;
  --color-text-3:       #A0A0AD;

  // PrimeNG surfaces
  --surface-0:          #FFFFFF;
  --surface-50:         #F8F7F4;
  --surface-100:        #F2F0EB;
  --surface-200:        #E8E6DF;
  --text-color:         #1C1C1E;
  --text-color-secondary: #6B6B7B;
}

// ── TEMA OSCURO ───────────────────────────────────────
[data-theme="dark"] {
  --color-page-bg:      #0D0D1A;
  --color-card-bg:      #1A1A2E;
  --color-card-hover:   #22223A;
  --color-topbar-bg:    #1A1A2E;
  --color-metrics-bg:   #1A1A2E;
  --color-cal-cell-bg:  #1A1A2E;
  --color-cal-alt-bg:   #131325;
  --color-border:       rgba(255, 255, 255, 0.06);
  --color-border-strong:rgba(255, 255, 255, 0.10);
  --color-text-1:       #F0F0F5;
  --color-text-2:       rgba(240, 240, 245, 0.50);
  --color-text-3:       rgba(240, 240, 245, 0.25);

  // PrimeNG surfaces en oscuro
  --surface-0:          #1A1A2E;
  --surface-50:         #131325;
  --surface-100:        #0D0D1A;
  --surface-200:        #080810;
  --text-color:         #F0F0F5;
  --text-color-secondary: rgba(240,240,245,0.5);
}
```

### Colores de citas por tema

Los colores de estado se ajustan para mantener contraste en ambos fondos:

```scss
// Tema claro — fondos sutiles, texto oscuro
[data-theme="light"] {
  --cita-completada-bg:   rgba(34, 197, 94, 0.09);
  --cita-completada-text: #15803D;
  --cita-activa-bg:       rgba(201, 168, 76, 0.12);
  --cita-activa-text:     #92600A;
  --cita-pendiente-bg:    rgba(59, 130, 246, 0.08);
  --cita-pendiente-text:  #1D4ED8;
  --cita-cancelada-bg:    rgba(239, 68, 68, 0.08);
  --cita-cancelada-text:  #B91C1C;
}

// Tema oscuro — fondos más opacos, texto claro
[data-theme="dark"] {
  --cita-completada-bg:   rgba(34, 197, 94, 0.13);
  --cita-completada-text: #86EFAC;
  --cita-activa-bg:       rgba(201, 168, 76, 0.15);
  --cita-activa-text:     #E8D5A0;
  --cita-pendiente-bg:    rgba(59, 130, 246, 0.13);
  --cita-pendiente-text:  #93C5FD;
  --cita-cancelada-bg:    rgba(239, 68, 68, 0.13);
  --cita-cancelada-text:  #FCA5A5;
}
```

### ThemeService en Angular

Un servicio singleton que aplica el tema, lo persiste en `localStorage` y detecta la preferencia del sistema operativo la primera vez:

```typescript
// core/theme/theme.service.ts
export type Theme = 'light' | 'dark';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly STORAGE_KEY = 'stilum-theme';
  current = signal<Theme>(this.resolveInitialTheme());

  constructor() {
    this.apply(this.current());
  }

  toggle(): void {
    const next: Theme = this.current() === 'light' ? 'dark' : 'light';
    this.set(next);
  }

  set(theme: Theme): void {
    this.current.set(theme);
    this.apply(theme);
    localStorage.setItem(this.STORAGE_KEY, theme);
    this.swapPrimeNGTheme(theme);
  }

  private apply(theme: Theme): void {
    document.documentElement.setAttribute('data-theme', theme);
  }

  private resolveInitialTheme(): Theme {
    const saved = localStorage.getItem(this.STORAGE_KEY) as Theme | null;
    if (saved) return saved;
    // Usa la preferencia del sistema operativo si no hay nada guardado
    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
  }

  private swapPrimeNGTheme(theme: Theme): void {
    const link = document.getElementById('primeng-theme') as HTMLLinkElement;
    if (link) {
      link.href = theme === 'dark'
        ? 'assets/themes/lara-dark-indigo/theme.css'
        : 'assets/themes/lara-light-indigo/theme.css';
    }
  }
}
```

### Link dinámico de PrimeNG en index.html

```html
<!-- index.html — el id permite cambiar el CSS en runtime -->
<link id="primeng-theme"
      rel="stylesheet"
      href="assets/themes/lara-light-indigo/theme.css">
```

### Toggle en el sidebar

```typescript
// shared/components/theme-toggle/theme-toggle.component.ts
@Component({
  selector: 'app-theme-toggle',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <button class="theme-btn" (click)="themeService.toggle()" [title]="label()">
      @if (themeService.current() === 'light') {
        <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.3">
          <circle cx="8" cy="8" r="3"/>
          <path d="M8 1v1.5M8 13.5V15M1 8h1.5M13.5 8H15M3.2 3.2l1 1M11.8 11.8l1 1M11.8 3.2l-1 1M3.2 11.8l1-1"/>
        </svg>
      } @else {
        <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.3">
          <path d="M13.5 10A6 6 0 0 1 6 2.5a6 6 0 1 0 7.5 7.5z"/>
        </svg>
      }
      <span>{{ label() }}</span>
    </button>
  `
})
export class ThemeToggleComponent {
  themeService = inject(ThemeService);
  label = computed(() => this.themeService.current() === 'light' ? 'Modo oscuro' : 'Modo claro');
}
```

Posición en el sidebar: justo encima del chip del tenant activo, en la parte inferior.

### Temas de PrimeNG necesarios en assets

```bash
# Copiar ambos temas al directorio de assets en angular.json
"assets": [
  { "glob": "**/*", "input": "node_modules/primeng/resources/themes/lara-light-indigo", "output": "assets/themes/lara-light-indigo" },
  { "glob": "**/*", "input": "node_modules/primeng/resources/themes/lara-dark-indigo",  "output": "assets/themes/lara-dark-indigo" }
]
```

---

## 8. Configuración de PrimeNG

### Tema base y overrides

```scss
// styles/primeng-theme.scss

// Importar tema base de PrimeNG
@import 'primeng/resources/themes/lara-light-indigo/theme.css';
@import 'primeng/resources/primeng.css';

// Override de variables del tema
:root {
  // Color primario → dorado
  --primary-color:       #C9A84C;
  --primary-color-text:  #1C1C2E;
  --primary-50:          #FAEEDA;
  --primary-100:         #F5D99A;
  --primary-200:         #E8C97A;
  --primary-500:         #C9A84C;
  --primary-600:         #A07828;
  --primary-700:         #7A5A18;

  // Superficie
  --surface-0:   #FFFFFF;
  --surface-50:  #F8F7F4;
  --surface-100: #F2F0EB;
  --surface-200: #E8E6DF;

  // Radio de borde
  --border-radius: 8px;

  // Fuente
  --font-family: 'Inter', sans-serif;
}

// DataTable — filas
.p-datatable .p-datatable-thead > tr > th {
  background: var(--surface-50);
  font-size: 11px;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: var(--color-text-secondary);
  border-bottom: 1px solid var(--color-border-default);
}

// Botón primario
.p-button.p-button-primary {
  background: var(--color-brand-gold);
  border-color: var(--color-brand-gold);
  color: var(--color-brand-dark);
  font-weight: 500;
  &:hover { background: #B8943E; border-color: #B8943E; }
}

// Input focus
.p-inputtext:focus { border-color: var(--color-brand-gold); box-shadow: 0 0 0 2px rgba(201,168,76,0.2); }

// Dialog
.p-dialog .p-dialog-header { border-bottom: 0.5px solid var(--color-border-default); }

// FullCalendar dentro de PrimeNG
.fc .fc-button-primary { background: var(--color-brand-dark); border-color: var(--color-brand-dark); }
.fc .fc-button-primary:not(:disabled).fc-button-active { background: var(--color-brand-gold); border-color: var(--color-brand-gold); color: var(--color-brand-dark); }
.fc-timegrid-slot { height: 36px !important; }
.fc-now-indicator-line { border-color: #EF4444; border-width: 1.5px; }
.fc-now-indicator-arrow { border-top-color: #EF4444; }
.fc-col-header-cell { background: var(--surface-0); }
```

### angular.json — estilos globales

```json
"styles": [
  "node_modules/primeng/resources/themes/lara-light-indigo/theme.css",
  "node_modules/primeng/resources/primeng.css",
  "node_modules/primeicons/primeicons.css",
  "src/styles/primeng-theme.scss",
  "src/styles/variables.scss",
  "src/styles/typography.scss",
  "src/styles/utilities.scss",
  "src/styles.scss"
]
```

---

## 9. Pantalla de Turnos (Display)

La pantalla de turnos tiene su propio look: **fondo oscuro, letras grandes, alto contraste** — pensada para verse desde la sala de espera a varios metros.

```scss
// features/display/display.component.scss

:host {
  display: block;
  min-height: 100vh;
  background: var(--color-brand-dark);
  color: #fff;
  font-family: $font-body;
  padding: 32px;
}

.display-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 40px;

  .business-name {
    font-family: $font-display;
    font-size: 28px;
    font-weight: 500;
    color: var(--color-brand-gold);
  }
  .clock {
    font-size: 32px;
    font-weight: 500;
    color: rgba(255,255,255,0.9);
  }
}

.en-atencion-block {
  background: rgba(201, 168, 76, 0.12);
  border: 1px solid rgba(201, 168, 76, 0.3);
  border-radius: 16px;
  padding: 28px 32px;
  margin-bottom: 32px;

  .label { font-size: 11px; text-transform: uppercase; letter-spacing: 2px; color: var(--color-brand-gold); margin-bottom: 12px; }
  .client-name { font-size: 36px; font-weight: 500; }
  .service-name { font-size: 18px; color: rgba(255,255,255,0.6); margin-top: 4px; }
}

.turnos-list .turno-item {
  display: flex;
  align-items: center;
  padding: 16px 0;
  border-bottom: 0.5px solid rgba(255,255,255,0.08);
  font-size: 20px;

  .turno-num { color: rgba(255,255,255,0.3); width: 40px; }
  .turno-client { flex: 1; font-weight: 500; }
  .turno-service { color: rgba(255,255,255,0.5); font-size: 16px; }
  .turno-hour { color: var(--color-brand-gold); font-size: 16px; width: 80px; text-align: right; }
}
```

---

## 10. Principios de UX

**Menos clics, más claridad.** Las acciones más frecuentes (nueva cita, cambiar estado) siempre visibles, nunca en submenús de 3 niveles.

**El estado del negocio de un vistazo.** Al entrar al dashboard, en menos de 3 segundos el administrador sabe cuántas citas hay, quién está en atención y cuánto se ha facturado.

**El calendario como centro.** No hay vista separada de "lista de citas" en el día a día — el calendario las incluye todas. La lista detallada es para historial y reportes.

**Formularios cortos.** Nueva cita = 4 pasos en modal (profesional → servicio → fecha → hora). Sin campos innecesarios.

**Feedback inmediato.** Cada acción muestra resultado al instante: al hacer check-in QR, el bloque cambia de color en el calendario sin recargar.

---

*Documento de Diseño — Stilum Pro v1.0*
