# Sistema de Citas para Peluquería — Documento de Arquitectura

**Versión:** 2.3 — Plataforma SaaS Multi-Tenant  
**Fecha:** 2026-06-07  
**Estado:** Borrador para revisión

---

## 1. Visión General — Plataforma SaaS

El sistema es una **plataforma SaaS multi-tenant**: un único sistema que sirve a múltiples negocios de peluquería de forma completamente independiente y aislada. Cada peluquería es un **tenant** que accede con sus propias credenciales, tiene su propio número de WhatsApp, sus propios profesionales y datos, y **nunca puede ver información de otro tenant**.

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    PLATAFORMA SAAS — SUPER ADMIN                        │
│          (Solo el dueño del sistema ve y gestiona todo)                 │
└──────────────────────┬──────────────────────────────────────────────────┘
                       │  Crea y gestiona tenants / suscripciones
         ┌─────────────┼─────────────────────────────────┐
         ▼             ▼                                 ▼
┌─────────────┐ ┌─────────────┐                 ┌─────────────┐
│  TENANT A   │ │  TENANT B   │       ...        │  TENANT N   │
│ Peluquería  │ │  Barbería   │                 │  Salón de   │
│  "Styles"   │ │  "El Corte" │                 │  Belleza    │
│             │ │             │                 │             │
│ 📱 WA propio│ │ 📱 WA propio│                 │ 📱 WA propio│
│ 👤 Profes.  │ │ 👤 Profes.  │                 │ 👤 Profes.  │
│ 📅 Citas    │ │ 📅 Citas    │                 │ 📅 Citas    │
│ 💰 Ingresos │ │ 💰 Ingresos │                 │ 💰 Ingresos │
└─────────────┘ └─────────────┘                 └─────────────┘
```

---

## 2. Stack Tecnológico

| Capa | Tecnología | Razón |
|------|-----------|-------|
| Backend | Java 21 + Spring Boot 3 | Multi-tenancy, seguridad robusta, Spring Security |
| Frontend Admin | Angular 21 + PrimeNG 21 | Última versión estable, PrimeNG tiene el componente de calendario (FullCalendar integrado), tablas, formularios y temas listos |
| Base de Datos | PostgreSQL | Row-Level Security nativa, schemas múltiples |
| Multi-tenancy DB | Shared DB + `tenant_id` (Row-level) | Más económico para SaaS MVP |
| IA del Agente | Anthropic Claude API (Haiku) | Bajo costo, excelente para conversación |
| WhatsApp | Meta Cloud API — 1 app, N números | Cada tenant registra su propio número |
| Email | JavaMailSender + Gmail SMTP / SendGrid | Gratuito para bajo volumen |
| QR Codes | ZXing (Java) | Open source |
| Auth | JWT con claim `tenantId` | Aislamiento automático por token |
| Despliegue | Render.com + Supabase + Netlify | Costo ~$0-10/mes para comenzar |

### Costo Operativo Estimado

| Ítem | Costo |
|------|-------|
| Backend (Render Starter) | $7/mes |
| PostgreSQL (Supabase free) | $0/mes |
| Frontend (Netlify) | $0/mes |
| WhatsApp API | $0/mes (1000 conv/mes gratis por tenant) |
| Claude Haiku | ~$1-5/mes según volumen |
| **Total** | **~$8-12/mes** |

> Los tenants pagan su suscripción mensual. El sistema puede autofinanciarse desde el primer cliente.

---

## 3. Arquitectura Multi-Tenant

### Estrategia: Base de datos compartida + `tenant_id` en todas las tablas

Todas las tablas de negocio incluyen una columna `tenant_id` (FK a `tenants`). Spring Boot aplica un **filtro global** en cada request que inyecta automáticamente el `tenant_id` del usuario autenticado, garantizando que ninguna query devuelva datos de otro tenant.

```
JWT del usuario incluye: { userId, tenantId, role }
                                      │
                          Spring Security Filter
                                      │
                              TenantContext.set(tenantId)
                                      │
                         Hibernate @Filter → WHERE tenant_id = ?
                                      │
                              Solo datos del tenant actual
```

### Dos Portales Distintos

```
┌─────────────────────────────┐    ┌──────────────────────────────┐
│      SUPER ADMIN PORTAL      │    │      TENANT PORTAL           │
│  admin.tuplataforma.com      │    │  app.tuplataforma.com        │
│                              │    │                              │
│  - Gestionar tenants         │    │  - Dashboard del negocio     │
│  - Activar / inactivar       │    │  - Gestionar profesionales   │
│  - Ver suscripciones         │    │  - Gestionar servicios       │
│  - Planes y facturación      │    │  - Ver y gestionar citas     │
│  - Métricas globales         │    │  - Configurar WhatsApp       │
│  - Crear credenciales        │    │  - Ver reportes propios      │
└─────────────────────────────┘    └──────────────────────────────┘
       Solo tú accedes aquí               Cada peluquería aquí
```

### Aislamiento del Agente WhatsApp

Un **único webhook** recibe todos los mensajes de WhatsApp. El sistema identifica a qué tenant pertenece el mensaje mediante el **`phone_number_id`** que Meta incluye en cada payload:

```
Meta Cloud API → POST /webhook/whatsapp
                        │
                   ¿phone_number_id en el payload?
                        │
              Buscar tenant por whatsapp_phone_number_id
                        │
              Si el tenant está ACTIVO → procesar
              Si está INACTIVO → ignorar o responder "servicio no disponible"
                        │
              Claude AI responde SOLO con datos de ese tenant
              (profesionales, servicios, horarios del tenant)
```

---

## 4. Módulo 0 — Gestión de Tenants y Suscripciones (Super Admin)

### Entidades

**TENANTS (negocios)**
```
id (UUID)
nombre_negocio
email_contacto
telefono_contacto
ciudad / pais
logo_url
activo (boolean)          ← Activar/inactivar acceso completo
fecha_creacion
fecha_inactivacion
```

**SUBSCRIPTIONS (suscripciones)**
```
id
tenant_id (FK)
plan_id (FK)
estado (ACTIVA | VENCIDA | CANCELADA | PRUEBA)
fecha_inicio
fecha_fin
precio_mensual
notas_pago
```

**PLANS (planes)**
```
id
nombre (BASICO | PRO | PREMIUM)
precio_mensual
max_profesionales
max_citas_mes
whatsapp_habilitado (boolean)
recordatorios_habilitados (boolean)
reportes_avanzados (boolean)
```

**TENANT_WHATSAPP_CONFIG**
```
id
tenant_id (FK, unique)
phone_number_id       ← ID del número en Meta
whatsapp_token        ← Token de acceso (cifrado en DB)
verify_token          ← Token de verificación del webhook
numero_whatsapp       ← El número visible (ej: +57300...)
activo (boolean)
```

**TENANT_USERS (usuarios del tenant)**
```
id
tenant_id (FK)
nombre
email (unique)
password_hash
rol (ADMIN_TENANT | PROFESIONAL)
activo (boolean)
```

### Flujo de Onboarding de un Nuevo Tenant

```
1. Super Admin crea el tenant en el portal
2. Super Admin elige el plan y activa la suscripción
3. Sistema crea usuario administrador para el tenant (email + contraseña temporal)
4. Se envía email al dueño del negocio con sus credenciales de acceso
5. El dueño ingresa al Tenant Portal y configura:
   a. Datos del negocio (nombre, logo)
   b. Número de WhatsApp Business (phone_number_id + token)
   c. Profesionales y servicios
   d. Horarios de atención
6. Sistema activa el agente WhatsApp para ese número
```

---

## 5. Módulo 1 — Agente WhatsApp con IA (por Tenant)

### Aislamiento del Contexto

El agente SOLO consulta datos del tenant al que pertenece el número de WhatsApp. Las herramientas (Tool Use) de Claude reciben siempre el `tenant_id` como parámetro obligatorio:

```java
// Ejemplo de tool call que Claude puede ejecutar
get_professionals(tenant_id, service_id)
get_availability(tenant_id, professional_id, date)
create_appointment(tenant_id, ...)
```

### Flujo de Conversación

```
Cliente envía WhatsApp al número de "Peluquería Styles"
        │
        ▼
Webhook recibe → identifica tenant por phone_number_id
        │
        ▼
¿Tenant activo? ¿Suscripción vigente?
        │ Sí
        ▼
Claude AI carga contexto de conversación del cliente
        │
        ▼
[INICIO] → Saludo personalizado con nombre del negocio
        │
        ▼
[SELECCIÓN SERVICIO] → Claude identifica servicio del catálogo del tenant
        │
        ▼
[MOSTRAR PROFESIONALES] → Solo profesionales activos de ese tenant
  "Te recomendamos:
   • Carlos López — Especialista en cortes fade y diseños
   • Ana Torres — 5 años en colorimetría y alisados"
        │
        ▼
[SELECCIÓN FECHA] → Disponibilidad del profesional del tenant
        │
        ▼
[SELECCIÓN HORA] → Horas libres ese día
        │
        ▼
[CONFIRMACIÓN] → Resumen de la cita
        │
        ▼
[CITA CONFIRMADA] → Email con QR, datos del negocio específico
```

### Estados del Agente

```
INICIO → SELECCION_SERVICIO → MOSTRAR_PROFESIONALES →
SELECCION_PROFESIONAL → SELECCION_FECHA → SELECCION_HORA →
CONFIRMACION → COMPLETADO

En cualquier punto: CANCELAR, REPROGRAMAR, HABLAR_CON_HUMANO
```

---

## 6. Módulo 2 — Aplicación Principal (por Tenant)

### Frontend Angular — Tenant Portal

| Sección | Componente PrimeNG principal | Funcionalidad |
|---------|--------------------------|--------------|
| Dashboard | `p-fullcalendar` (ResourceTimeGrid) | Calendario operativo por profesional en tiempo real |
| Profesionales | `p-datatable`, `p-card`, `p-togglebutton` | CRUD, foto, descripción, habilitar/deshabilitar |
| Disponibilidad | `p-calendar`, `p-inputswitch`, `p-timeline` | Horarios regulares, bloqueo de fechas |
| Servicios | `p-datatable`, `p-chip`, `p-orderlist` | Catálogo con precio, duración, asignación a profesionales |
| Citas | `p-fullcalendar` + `p-datatable` | Vista calendario + lista, filtros, cambio de estado |
| Clientes | `p-datatable`, `p-panel` | Registro, historial de citas por cliente |
| Reportes | `p-chart` (Chart.js), `p-datatable` | Ingresos, citas por estado, servicios populares |
| Configuración | `p-tabview`, `p-fileupload` | Negocio, logo, WhatsApp, usuarios, notificaciones |

### Dashboard — Calendario Operativo

El dashboard es la pantalla principal del tenant. Funciona como un **calendario tipo agenda de salón**: cada profesional es una columna y cada franja horaria es una fila. Las citas aparecen como bloques de color con la duración real del servicio, igual que Google Calendar pero orientado a la operación diaria del negocio.

#### Implementación con PrimeNG + FullCalendar

PrimeNG 21 incluye el componente `<p-fullcalendar>` que integra FullCalendar con soporte nativo para Angular. Se usará la vista `resourceTimeGridDay` (columnas por profesional) y `resourceTimeGridWeek` (vista semanal), que es exactamente el layout de salón que se necesita.

```bash
npm install primeng@21 @fullcalendar/core @fullcalendar/resource-timegrid @fullcalendar/interaction
```

Cada profesional es un **recurso** de FullCalendar y cada cita es un **evento** coloreado según su estado. El drag & drop para reprogramar viene incluido con el plugin `@fullcalendar/interaction`.

---

#### Vista Principal — Calendario Diario por Profesional

```
┌────────────────────────────────────────────────────────────────────────────┐
│  🏪 Peluquería Styles        ◀ Lun 8 Jun 2026 ▶       [ Día | Semana ]    │
├──────────────────────────────────────────────────────────────────────────  │
│  ┌──────────┐  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐  │
│  │  HORA    │  │  Carlos López   │  │   Ana Torres    │  │ Luis Herrera│  │
│  │          │  │  🟢 En atención │  │  🟡 Disponible  │  │ ⚫ Inactivo │  │
│  ├──────────┤  ├─────────────────┤  ├─────────────────┤  ├─────────────┤  │
│  │  09:00   │  │ ████████████    │  │                 │  │             │  │
│  │          │  │ Juan Pérez      │  │                 │  │             │  │
│  │  09:30   │  │ Corte + Barba   │  │ ████████████    │  │             │  │
│  │          │  │ ✅ Completado   │  │ María Torres    │  │             │  │
│  │  10:00   │  │                 │  │ Colorimetría    │  │             │  │
│  │          │  │ ████████████    │  │ 🟢 En atención  │  │             │  │
│  │  10:30   │  │ Pedro Gómez     │  │                 │  │             │  │
│  │          │  │ Corte           │  │                 │  │             │  │
│  │  11:00   │  │ ⏳ Pendiente    │  │ ████████████    │  │             │  │
│  │          │  │                 │  │ Laura Díaz      │  │             │  │
│  │  11:30   │  │ ████████████    │  │ Alisado         │  │             │  │
│  │          │  │ Carlos Ruiz     │  │ ⏳ Pendiente    │  │             │  │
│  │  12:00   │  │ Corte + Barba   │  │                 │  │             │  │
│  └──────────┘  └─────────────────┘  └─────────────────┘  └─────────────┘  │
├────────────────────────────────────────────────────────────────────────────┤
│  Citas: 12  │  Completadas: 5  │  Pendientes: 7  │  Ingresos: $180.000     │
└────────────────────────────────────────────────────────────────────────────┘
```

#### Comportamiento de los Bloques de Cita

Cada bloque ocupa visualmente la duración exacta del servicio (ej: un corte de 45 min ocupa 1.5 franjas de 30 min). El color del bloque indica el estado:

| Color | Estado |
|-------|--------|
| 🟩 Verde | Completada (check-in QR hecho) |
| 🟦 Azul | En atención ahora |
| 🟨 Amarillo | Pendiente / confirmada |
| 🟥 Rojo | Cancelada o no asistió |

Al hacer clic en un bloque se abre un panel lateral con el detalle completo de la cita y acciones: cancelar, reprogramar, marcar no asistió.

#### Vista Semanal

Cambiando al modo **Semana**, el calendario muestra los 7 días en columnas y las citas de todos los profesionales agrupadas por día. Útil para planificación. Al hacer clic en un día, vuelve a la vista diaria de ese día.

#### Franja de Hora Actual

Una línea roja horizontal marca la hora actual y avanza en tiempo real, igual que Google Calendar, permitiendo ver de un vistazo qué está ocurriendo ahora vs. qué viene después.

#### Barra de Resumen (pie del calendario)

Fija en la parte inferior: total de citas del día, completadas, pendientes e ingresos del día. Se actualiza en tiempo real vía WebSocket.

#### Cabecera de Profesional

Cada columna muestra nombre, foto en miniatura y estado actual con semáforo:

| Ícono | Estado |
|-------|--------|
| 🟢 | En atención |
| 🟡 | Disponible |
| 🔵 | En pausa |
| ⚫ | Inactivo hoy |

#### Acciones desde el Calendario

- **Clic en franja vacía** → abrir formulario de nueva cita para ese profesional y hora
- **Clic en bloque existente** → ver detalle / acciones
- **Arrastrar bloque** → reprogramar la cita (drag & drop) con confirmación
- **Botón [+] en cabecera de profesional** → agendar nueva cita para ese profesional

#### Actualización en Tiempo Real

El calendario recibe eventos WebSocket (mismo canal que la pantalla de turnos). Cuando un check-in QR ocurre, el bloque cambia de amarillo a azul/verde sin recargar.

#### Endpoints

```
GET /api/tenant/dashboard/citas?fecha=2026-06-08
    Response: {
      fecha,
      profesionales: [ { id, nombre, foto, estado } ],
      citas: [ { id, profesionalId, clienteNombre, servicio, horaInicio,
                 horaFin, duracionMinutos, estado, color } ]
    }

GET /api/tenant/dashboard/citas?desde=2026-06-08&hasta=2026-06-14
    → Vista semanal: misma estructura, rango de fechas

GET /api/tenant/dashboard/resumen?fecha=2026-06-08
    Response: { total, completadas, pendientes, canceladas, ingresos_dia, ingresos_mes }
```

---

### Frontend Angular — Super Admin Portal

| Sección | Funcionalidad |
|---------|--------------|
| Tenants | Listado, crear, activar/inactivar, ver detalle |
| Suscripciones | Estado, plan, vencimiento, renovar, cancelar |
| Planes | Crear/editar planes y precios |
| Métricas | Total tenants activos, conversaciones WhatsApp, citas globales |
| Usuarios | Gestión de usuarios de cada tenant |

### Backend Spring Boot — Estructura

```
com.peluqueria
├── config/
│   ├── SecurityConfig        # JWT + roles
│   ├── TenantFilter          # Inyecta tenantId en contexto
│   ├── HibernateFilterConfig # @Filter global tenant_id
│   └── WebConfig             # CORS
├── controller/
│   ├── superadmin/           # Solo accesible con rol SUPER_ADMIN
│   │   ├── TenantController
│   │   ├── SubscriptionController
│   │   └── PlanController
│   └── tenant/               # Accesible con rol ADMIN_TENANT
│       ├── ProfesionalController
│       ├── ServicioController
│       ├── CitaController
│       ├── ClienteController
│       ├── ReporteController
│       └── ConfiguracionController
├── webhook/
│   └── WhatsAppWebhookController
├── agent/
│   ├── WhatsAppService
│   ├── ClaudeAgentService
│   ├── TenantResolver         # Identifica tenant por phone_number_id
│   └── ConversationStateService
├── scheduler/
│   └── ReminderScheduler
└── util/
    ├── QRGenerator
    ├── EmailService
    └── TenantContextHolder    # ThreadLocal con tenantId actual
```

---

## 7. Módulo 3 — Gestión de Servicios (Catálogo)

### Descripción

Cada tenant administra su propio catálogo de servicios: los tipos de trabajo que ofrece su negocio, su duración y precio. Este catálogo es la base que el agente WhatsApp usa para recomendar profesionales y el sistema usa para calcular disponibilidad y registrar ingresos.

### Entidad Servicio

```sql
servicios (
  id          UUID PK,
  tenant_id   UUID FK,        -- aislamiento multi-tenant
  nombre      VARCHAR,        -- "Corte de cabello", "Colorimetría"
  descripcion TEXT,           -- descripción para mostrar en WhatsApp
  duracion_minutos INT,       -- para calcular franjas de disponibilidad
  precio      DECIMAL(10,2),  -- precio base del servicio
  activo      BOOLEAN,        -- ocultar sin borrar
  creado_en   TIMESTAMP
)
```

### Relación con Profesionales

Un profesional puede ofrecer uno o varios servicios. Esta relación determina a quién el agente puede recomendar para una solicitud:

```sql
profesional_servicios (
  profesional_id UUID FK,
  servicio_id    UUID FK,
  PRIMARY KEY (profesional_id, servicio_id)
)
```

### Funcionalidades del Módulo

| Función | Descripción |
|---------|-------------|
| Crear servicio | Nombre, descripción, duración, precio |
| Editar servicio | Cambiar precio o duración sin perder historial |
| Activar / Inactivar | El servicio deja de ofrecerse sin borrarse; las citas pasadas se conservan |
| Asignar a profesionales | Marcar qué profesionales realizan cada servicio |
| Orden de presentación | El tenant puede ordenar los servicios como desee que aparezcan |

### Endpoints REST

```
GET    /api/tenant/servicios                  → lista todos los servicios del tenant
POST   /api/tenant/servicios                  → crear servicio
PUT    /api/tenant/servicios/{id}             → editar
PATCH  /api/tenant/servicios/{id}/estado      → activar/inactivar
DELETE /api/tenant/servicios/{id}             → solo si no tiene citas asociadas
GET    /api/tenant/servicios/{id}/profesionales → profesionales que lo ofrecen
POST   /api/tenant/servicios/{id}/profesionales → asignar profesional
DELETE /api/tenant/servicios/{id}/profesionales/{profId} → desasignar
```

### Lógica de Negocio

- **Duración** es clave: si un servicio dura 60 min, el sistema bloquea esa franja en la agenda del profesional para que no se dupliquen citas.
- **Precio base** puede ser sobrescrito en la cita concreta (para descuentos o ajustes manuales).
- **Inactivar** un servicio no cancela citas ya agendadas; solo evita nuevas reservas.
- El agente WhatsApp presenta los servicios activos en orden configurado por el tenant.

### Vista en el Panel Angular

```
Servicios
├── Listado con tarjetas (nombre, precio, duración, estado)
├── Botón "Nuevo servicio" → formulario
├── Toggle activo/inactivo por servicio
├── Gestión de profesionales asignados (chips seleccionables)
└── Filtro por estado (Activos / Inactivos / Todos)
```

---

## 8. Módulo 4 — Sistema de Recordatorios y Notificaciones

### 4.1 Recordatorios a Clientes

Un job `@Scheduled` corre cada hora y envía por WhatsApp + email:
- **24 horas antes:** recordatorio con opción de confirmar, cancelar o reprogramar
- **2 horas antes:** recordatorio final por WhatsApp

Cada mensaje usa el número WhatsApp y el nombre del tenant correspondiente.

---

### 4.2 Notificaciones a Profesionales

Los profesionales también reciben avisos por WhatsApp al número registrado en su perfil.

#### Resumen diario de agenda (programado)

Cada día, a la hora configurada por el tenant (ej: 7:00 AM), cada profesional activo recibe un resumen de sus citas del día:

```
Buenos días, Carlos 👋

Tus citas para hoy — Lunes 8 de junio:

1. 09:00 AM → Juan Pérez      | Corte + Barba
2. 11:00 AM → María Torres    | Colorimetría
3. 02:00 PM → Pedro Gómez     | Corte
4. 04:30 PM → Laura Díaz      | Alisado

Total: 4 citas · Ingresos estimados: $120.000

¡Que tengas un excelente día! ✂️
— Peluquería Styles
```

Si el profesional no tiene citas ese día, no se envía el mensaje.

La hora de envío se configura por tenant en el panel admin (ej: "Enviar resumen a las 07:00 AM").

#### Aviso de próximo cliente (en tiempo real)

Cuando el cliente anterior completa su check-in QR (su cita pasa a `EN_ATENCION`), el sistema envía automáticamente un WhatsApp al profesional cuya siguiente cita es en los próximos **30 minutos**:

```
✂️ Tu próximo cliente llega pronto:

👤 Pedro Gómez
💈 Servicio: Corte de cabello
⏰ Hora: 02:00 PM (en ~25 minutos)

— Peluquería Styles
```

Este aviso también se dispara cuando el cliente escanea su QR en recepción, confirmando que ya llegó al local.

#### Configuración por Tenant

```sql
tenant_notification_config (
  tenant_id             UUID FK,
  hora_resumen_diario   TIME,         -- ej: 07:00:00
  aviso_proximo_cliente BOOLEAN,      -- activar/desactivar
  minutos_anticipacion  INT           -- cuántos min antes avisar (default 30)
)
```

#### Endpoints

```
GET  /api/tenant/configuracion/notificaciones
PUT  /api/tenant/configuracion/notificaciones
POST /api/tenant/notificaciones/resumen-manual  → enviar resumen ahora (prueba)
```

---

## 9. Módulo 5 — Sistema QR de Check-in y Activación de Turno

### Flujo Completo

El QR no solo confirma la asistencia — es el disparador de toda la cadena de eventos en tiempo real:

```
Cliente llega al local y muestra su QR
        │
        ▼
Profesional / recepcionista escanea el QR
        │
        ▼
Sistema valida: token HMAC + tenant_id + cita no vencida
        │
        ▼
Cita pasa a estado EN_ATENCION
        │
        ├──► Pantalla de turnos se actualiza en tiempo real (WebSocket)
        │    Cliente pasa de "Próximos" a "En atención ahora"
        │
        ├──► Aviso WhatsApp al profesional de su siguiente cliente
        │    (si hay otro turno en los próximos 30 min)
        │
        └──► Registro de hora exacta de inicio del servicio
```

### Generación del QR

- Librería: **ZXing** (`com.google.zxing`)
- Se genera al confirmar la cita y se adjunta al email de confirmación
- Token firmado: `HMAC-SHA256(citaId + tenantId + fecha, secret_key)`
- Válido solo para la fecha de la cita (expira a medianoche)

### Página de Check-in (Escáner)

Ruta pública Angular: `/checkin/:token`
- Optimizada para móvil (el profesional la usa desde su teléfono)
- Muestra: nombre del cliente, servicio, precio
- Botón "Confirmar inicio de servicio"
- Al confirmar: dispara el WebSocket hacia la pantalla de turnos

### Endpoint

```
POST /api/checkin/{qrToken}   → valida y activa la cita
     Response: { cliente, servicio, profesional, horaInicio }
```

El QR incluye `tenant_id` en el token firmado para garantizar que el check-in solo funcione para citas del propio tenant.

---

## 10. Modelo de Datos Completo

### Tablas del Sistema (sin tenant_id — globales)

```sql
tenants (id, nombre_negocio, email, telefono, activo, ...)
plans (id, nombre, precio, max_profesionales, ...)
subscriptions (id, tenant_id, plan_id, estado, fecha_inicio, fecha_fin, ...)
tenant_whatsapp_config (id, tenant_id, phone_number_id, token_cifrado, ...)
tenant_users (id, tenant_id, nombre, email, password_hash, rol, activo)
```

### Tablas de Negocio (todas con tenant_id)

```sql
profesionales (id, tenant_id, nombre, email, numero_whatsapp, descripcion, activo, ...)
                                              ↑ para recibir avisos y resumen diario
servicios (id, tenant_id, nombre, duracion_minutos, precio, activo, ...)
profesional_servicios (profesional_id, servicio_id)
disponibilidad (id, tenant_id, profesional_id, dia_semana, hora_inicio, ...)
clientes (id, tenant_id, nombre, email, numero_whatsapp, ...)
citas (id, tenant_id, cliente_id, profesional_id, servicio_id, fecha, estado, qr_token,
       hora_inicio_real, ...)   ← registra cuándo se hizo el check-in real
conversation_state (id, tenant_id, numero_whatsapp, estado_actual, contexto_json, ...)
tenant_notification_config (tenant_id, hora_resumen_diario, aviso_proximo_cliente,
                             minutos_anticipacion)
```

### Estados de Cita

```
PENDIENTE → CONFIRMADA → COMPLETADA
                      ↘ CANCELADA
                      ↘ NO_ASISTIO
                      ↘ REPROGRAMADA
```

---

## 11. Seguridad y Aislamiento

### JWT por Rol

```json
// Token de tenant admin
{
  "sub": "user-uuid",
  "tenantId": "tenant-uuid",
  "role": "ADMIN_TENANT",
  "exp": 1234567890
}

// Token de super admin (sin tenantId)
{
  "sub": "superadmin-uuid",
  "role": "SUPER_ADMIN",
  "exp": 1234567890
}
```

### Filtro Automático de Tenant

```java
// Hibernate @Filter inyectado en cada request
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")

// Spring interceptor que lee el JWT y activa el filtro
public class TenantRequestInterceptor implements HandlerInterceptor {
    public boolean preHandle(HttpServletRequest req, ...) {
        String tenantId = jwtService.extractTenantId(req);
        TenantContextHolder.setTenantId(tenantId);
        // Hibernate activa el @Filter automáticamente
    }
}
```

### Reglas de Acceso

| Ruta | Roles permitidos |
|------|-----------------|
| `/api/superadmin/**` | SUPER_ADMIN |
| `/api/tenant/**` | ADMIN_TENANT (solo su tenant) |
| `/api/profesional/**` | PROFESIONAL (solo su agenda) |
| `/webhook/whatsapp` | Público (verificado por Meta) |
| `/checkin/{token}` | Público (verificado por HMAC) |

---

## 12. Estrategia de Despliegue

### Arquitectura de Producción

```
Internet
   │
   ├── app.tuplataforma.com (Angular Tenant) → Netlify [Gratis]
   ├── admin.tuplataforma.com (Angular SuperAdmin) → Netlify [Gratis]
   │
   └── api.tuplataforma.com (Spring Boot) → Render.com [$7/mes]
                │
                └── PostgreSQL → Supabase [Gratis hasta 500MB]
```

### Variables de Entorno

```properties
# Base de datos
SPRING_DATASOURCE_URL=jdbc:postgresql://supabase.../peluqueria
SPRING_DATASOURCE_USERNAME=...
SPRING_DATASOURCE_PASSWORD=...

# JWT
JWT_SECRET=...
JWT_EXPIRATION=86400000

# WhatsApp (webhook único para todos los tenants)
WHATSAPP_APP_SECRET=...       # Para verificar firma de Meta
WHATSAPP_VERIFY_TOKEN=...     # Para verificar el webhook al registrarlo

# Claude API
ANTHROPIC_API_KEY=...
ANTHROPIC_MODEL=claude-haiku-4-5-20251001

# Email
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=notificaciones@tudominio.com
MAIL_PASSWORD=...

# QR
QR_HMAC_SECRET=...
APP_BASE_URL=https://app.tuplataforma.com
```

> **Nota:** Los tokens de WhatsApp de cada tenant se almacenan **cifrados en base de datos** (AES-256). El servidor los descifra en runtime para hacer llamadas a la API de Meta.

---

## 13. Planes de Suscripción (Propuesta)

| Feature | Básico | Pro | Premium |
|---------|--------|-----|---------|
| Profesionales | hasta 3 | hasta 10 | Ilimitados |
| Citas/mes | hasta 100 | hasta 500 | Ilimitadas |
| Agente WhatsApp | ✅ | ✅ | ✅ |
| Recordatorios | ✅ | ✅ | ✅ |
| QR Check-in | ❌ | ✅ | ✅ |
| Reportes avanzados | ❌ | ✅ | ✅ |
| Soporte prioritario | ❌ | ❌ | ✅ |
| **Precio sugerido** | **$15/mes** | **$35/mes** | **$65/mes** |

---

## 14. Roadmap de Desarrollo

> **Prioridad:** Aplicación web completa primero → Agente WhatsApp en segunda etapa.

---

### Fase 1 — Cimientos Backend (2-3 semanas)

Objetivo: API funcional y segura sobre la que se construye todo lo demás.

**Semana 1: Setup y Multi-Tenancy**
- [ ] Proyecto Spring Boot 3 + Java 21 con estructura Clean Architecture
- [ ] Flyway: migraciones V1 (tablas base: tenants, plans, subscriptions, users)
- [ ] Multi-tenancy: TenantFilter + Hibernate @Filter global
- [ ] Auth JWT con roles (SUPER_ADMIN, ADMIN_TENANT, PROFESIONAL)
- [ ] GlobalExceptionHandler + jerarquía de excepciones de dominio
- [ ] OpenAPI / Swagger habilitado

**Semana 2: Dominio de Negocio**
- [ ] Flyway: migraciones V2 (profesionales, servicios, disponibilidad, clientes, citas)
- [ ] Entidades de dominio con reglas encapsuladas (RN-CITA-*, RN-PROF-*, RN-SERV-*)
- [ ] Specifications: CitaDisponibleSpec, LimitePlanSpec, HorarioProfesionalSpec
- [ ] Use Cases: AgendarCita, CancelarCita, GestionarProfesional, GestionarServicio
- [ ] Repositories con filtro de tenant automático

**Semana 3: APIs REST completas**
- [ ] CRUD Tenants + Suscripciones (Super Admin)
- [ ] CRUD Profesionales, Servicios, Disponibilidad (Tenant)
- [ ] Endpoints de Citas: agendar, cancelar, reprogramar, cambiar estado
- [ ] Endpoint de disponibilidad: horas libres por profesional y fecha
- [ ] Endpoints de Dashboard: resumen del día, citas por rango de fechas

---

### Fase 2 — Frontend Web (5-6 semanas)

Objetivo: Panel administrador completamente funcional para gestionar el negocio sin el agente WhatsApp.

**Semana 4: Setup Angular + Autenticación**
- [ ] Proyecto Angular 21 con estructura feature-based
- [ ] PrimeNG 21 + tema personalizado
- [ ] Login con JWT, guards de ruta, interceptor JWT
- [ ] Layout principal (sidebar, header) con navegación por rol
- [ ] Pantalla de login y redirección por rol (SUPER_ADMIN / ADMIN_TENANT)

**Semana 5: Super Admin Portal**
- [ ] Listado y gestión de tenants (p-datatable con filtros)
- [ ] Crear tenant: formulario con credenciales iniciales
- [ ] Activar / inactivar tenant con confirmación
- [ ] Gestión de planes y suscripciones
- [ ] Métricas globales básicas

**Semana 6: Gestión de Profesionales y Servicios**
- [ ] CRUD Profesionales con foto (p-fileupload), descripción, toggle activo
- [ ] Gestión de disponibilidad semanal (p-calendar + horarios por día)
- [ ] Bloqueo de fechas específicas
- [ ] CRUD Servicios con duración, precio y asignación a profesionales (p-orderlist)

**Semana 7: Dashboard Calendario**
- [ ] Calendario operativo con p-fullcalendar (ResourceTimeGrid)
- [ ] Vista diaria: columnas por profesional, bloques por duración real
- [ ] Vista semanal: columnas por día
- [ ] Línea de hora actual, colores por estado de cita
- [ ] Clic en bloque: panel lateral de detalle con acciones
- [ ] Clic en franja vacía: modal nueva cita
- [ ] Drag & drop para reprogramar
- [ ] Tarjetas de resumen (total, completadas, ingresos del día)

**Semana 8: Gestión de Citas y Clientes**
- [ ] Formulario de nueva cita: selección profesional → servicio → fecha → hora
- [ ] Cambio de estado manual desde el panel (cancelar, marcar no asistió)
- [ ] Historial de citas con filtros (fecha, profesional, estado)
- [ ] Registro y listado de clientes con historial por cliente

**Semana 9: Pantalla de Turnos + QR**
- [ ] Pantalla display de turnos (ruta pública, WebSocket)
- [ ] Generación de QR al crear/confirmar cita
- [ ] Página de check-in QR (mobile-friendly)
- [ ] Conexión check-in → actualización en tiempo real del calendario y display
- [ ] Email de confirmación con QR adjunto

---

### Fase 3 — Reportes y Notificaciones web (2 semanas)

Objetivo: Completar el ciclo de información y alertas sin depender del agente.

**Semana 10:**
- [ ] Reportes de ingresos por período (p-chart línea + tabla)
- [ ] Reporte de servicios más solicitados (p-chart barra)
- [ ] Reporte de desempeño por profesional
- [ ] Resumen de métricas del mes en dashboard

**Semana 11:**
- [ ] Sistema de recordatorios: job automático 24h y 2h antes (email)
- [ ] Resumen diario por email a profesionales
- [ ] Notificaciones de suscripción próxima a vencer (Super Admin)
- [ ] Onboarding guiado para nuevos tenants (stepper de configuración)

---

### Fase 4 — Agente WhatsApp (3-4 semanas)

Objetivo: Añadir el canal de atención automática sobre la plataforma web ya estable.

**Semana 12:**
- [ ] Configurar Meta Cloud API (webhook único multi-tenant)
- [ ] TenantResolver: identificar tenant por phone_number_id
- [ ] Guardar/recuperar estado de conversación por número de WhatsApp
- [ ] Tool Use de Claude: get_services, get_professionals, get_availability

**Semana 13:**
- [ ] Flujo completo: selección servicio → profesional → fecha → hora → confirmación
- [ ] Manejo de cancelar / reprogramar desde WhatsApp
- [ ] Integración con el mismo Use Case de AgendarCita (sin duplicar lógica)
- [ ] Pruebas con números reales en sandbox de Meta

**Semana 14:**
- [ ] Recordatorios por WhatsApp (24h y 2h antes)
- [ ] Resumen diario a profesionales por WhatsApp
- [ ] Aviso de próximo cliente al profesional tras check-in QR
- [ ] Configuración de WhatsApp por tenant en el panel Angular

**Semana 15 (buffer):**
- [ ] Pruebas end-to-end del flujo completo WhatsApp → cita → QR → display
- [ ] Ajustes de prompts del agente Claude
- [ ] Manejo de errores y casos bordes de conversación

---

## 15. Módulo 6 — Pantalla de Turnos (Display de Sala de Espera)

### Descripción

Una pantalla proyectable en el establecimiento que muestra en tiempo real los turnos del día: quién está siendo atendido, quiénes siguen y cuánto tiempo estimado falta. Es de solo lectura, no requiere login, y se accede desde cualquier dispositivo con navegador (TV, tablet, computador conectado a proyector).

### Acceso sin Login — Display Token

Cada tenant tiene una URL única de pantalla. No pide contraseña para facilitar la proyección, pero usa un **token opaco** que solo el tenant puede ver y regenerar desde su panel:

```
https://display.tuplataforma.com/{display-token}

Ejemplo:
https://display.tuplataforma.com/a3f8c2e1-styles-peluqueria
```

El token se almacena en la tabla `tenants` y puede regenerarse si se quiere revocar el acceso a una pantalla anterior.

### Actualizaciones en Tiempo Real — WebSocket

La pantalla se conecta por **WebSocket** al backend y recibe actualizaciones instantáneas cada vez que cambia el estado de una cita. No necesita recargar la página.

```
TV / Proyector (navegador)
        │
        │  WebSocket ws://api.tuplataforma.com/ws/display/{display-token}
        │
Spring Boot WebSocket (STOMP)
        │
        │  Emite evento cuando:
        │   - Una cita pasa a EN_ATENCION (profesional hizo check-in QR)
        │   - Una cita pasa a COMPLETADA
        │   - Se cancela una cita del día
        │   - Se agrega una cita nueva para hoy
        ▼
Pantalla se actualiza automáticamente sin recargar
```

### Diseño de la Pantalla

```
┌──────────────────────────────────────────────────────────────┐
│  🏪 PELUQUERÍA STYLES                    🕐 10:32 AM         │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│   ✂️  EN ATENCIÓN AHORA                                      │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  👤 JUAN PÉREZ          💈 Corte + Barba             │   │
│  │  Profesional: Carlos    ⏱ Inició: 10:15 AM           │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                              │
│   📋  PRÓXIMOS TURNOS                                        │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  1.  MARÍA TORRES       Colorimetría    10:45 AM     │   │
│  │  2.  PEDRO GÓMEZ        Corte           11:00 AM     │   │
│  │  3.  LAURA DÍAZ         Alisado         11:30 AM     │   │
│  │  4.  CARLOS RUIZ        Corte + Barba   12:00 PM     │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                              │
│   ✅  ATENDIDOS HOY: 8       ⏳ Espera promedio: 12 min      │
└──────────────────────────────────────────────────────────────┘
```

### Configuración por Tenant

El tenant puede personalizar la pantalla desde su panel:

| Opción | Descripción |
|--------|-------------|
| Mostrar nombre del cliente | Completo, solo primer nombre, o iniciales (privacidad) |
| Mostrar servicio | Sí / No |
| Mostrar profesional asignado | Sí / No |
| Número de turnos a mostrar | 3, 5, 10 próximos |
| Color de fondo / tema | Claro u oscuro |
| Logo del negocio | Se muestra en la cabecera |
| Mensaje personalizado | Texto libre (ej: "¡Bienvenido! Gracias por su espera") |

### Estados de los Turnos en Pantalla

```
PENDIENTE    → Aparece en lista "Próximos turnos" (gris)
EN_ATENCION  → Aparece en bloque destacado "En atención ahora" (verde/dorado)
COMPLETADA   → Desaparece de la pantalla
CANCELADA    → Desaparece de la pantalla
NO_ASISTIO   → Desaparece de la pantalla
```

El estado `EN_ATENCION` se activa cuando el profesional escanea el QR del cliente (Módulo 5), lo que dispara automáticamente la actualización en la pantalla proyectada.

### Endpoints y WebSocket

```
# REST
GET  /api/display/{displayToken}/turnos-hoy   → estado actual del día (carga inicial)
POST /api/tenant/configuracion/display         → guardar config de pantalla
GET  /api/tenant/configuracion/display         → obtener config actual
POST /api/tenant/configuracion/display/regenerar-token → generar nuevo display token

# WebSocket (STOMP)
SUBSCRIBE /topic/display/{displayToken}        → recibe eventos en tiempo real
```

### Evento WebSocket (ejemplo)

```json
{
  "tipo": "CITA_EN_ATENCION",
  "cita": {
    "id": "uuid",
    "clienteNombre": "Juan Pérez",
    "servicio": "Corte + Barba",
    "profesional": "Carlos López",
    "horaInicio": "10:15"
  }
}
```

### Implementación Frontend

La pantalla de display es una **ruta pública en Angular** (`/display/:token`) optimizada para pantallas grandes:
- Fuentes grandes, alto contraste
- Sin menús ni navegación
- Modo pantalla completa automático
- Reconexión automática al WebSocket si se cae la conexión

---

## 16. Próximos Pasos Inmediatos

1. **Aprobar** este documento de arquitectura
2. **Definir nombre comercial** de la plataforma y dominio
3. **Crear cuenta** en Meta for Developers (app única para todos los tenants)
4. **Crear cuentas:** Render.com, Supabase, Netlify, Anthropic
5. **Iniciar Fase 1** — setup del proyecto Spring Boot multi-tenant

---

*Documento generado — Sistema de Citas SaaS Peluquería v2.0*
