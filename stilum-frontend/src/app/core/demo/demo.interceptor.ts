import { HttpInterceptorFn, HttpResponse } from '@angular/common/http';
import { of } from 'rxjs';

const DEMO_KEY = 'stilum-demo';

export function isDemoMode(): boolean {
  return localStorage.getItem(DEMO_KEY) === 'true';
}
export function enableDemo(): void  { localStorage.setItem(DEMO_KEY, 'true'); }
export function disableDemo(): void { localStorage.removeItem(DEMO_KEY); }

// ── Mock data ──────────────────────────────────────────────────────────

const PROFESIONALES = [
  { id: 'p1', nombre: 'María García', especialidad: 'Colorista', bio: 'Especialista en coloración y mechas.', fotoUrl: null, colorAgenda: '#E85D4A', activo: true, horarios: [{ id: 'h1', diaSemana: 1, horaInicio: '09:00', horaFin: '18:00', activo: true }, { id: 'h2', diaSemana: 2, horaInicio: '09:00', horaFin: '18:00', activo: true }, { id: 'h3', diaSemana: 3, horaInicio: '09:00', horaFin: '18:00', activo: true }, { id: 'h4', diaSemana: 4, horaInicio: '09:00', horaFin: '18:00', activo: true }, { id: 'h5', diaSemana: 5, horaInicio: '09:00', horaFin: '17:00', activo: true }], createdAt: '2024-01-10T10:00:00Z' },
  { id: 'p2', nombre: 'Carlos López', especialidad: 'Barbero', bio: 'Experto en cortes masculinos y barba.', fotoUrl: null, colorAgenda: '#3B82F6', activo: true, horarios: [{ id: 'h6', diaSemana: 1, horaInicio: '10:00', horaFin: '19:00', activo: true }, { id: 'h7', diaSemana: 2, horaInicio: '10:00', horaFin: '19:00', activo: true }, { id: 'h8', diaSemana: 6, horaInicio: '10:00', horaFin: '16:00', activo: true }], createdAt: '2024-02-05T10:00:00Z' },
  { id: 'p3', nombre: 'Ana Torres', especialidad: 'Estilista', bio: 'Tratamientos capilares y peinados.', fotoUrl: null, colorAgenda: '#22C55E', activo: true, horarios: [{ id: 'h9', diaSemana: 3, horaInicio: '09:00', horaFin: '18:00', activo: true }, { id: 'h10', diaSemana: 5, horaInicio: '09:00', horaFin: '18:00', activo: true }], createdAt: '2024-03-01T10:00:00Z' },
];

const SERVICIOS = [
  { id: 's1', nombre: 'Corte de cabello', descripcion: 'Corte clásico o moderno.', duracionMin: 45, precio: 35000, activo: true, createdAt: '2024-01-01T00:00:00Z' },
  { id: 's2', nombre: 'Coloración completa', descripcion: 'Tinte de raíz a puntas.', duracionMin: 120, precio: 120000, activo: true, createdAt: '2024-01-01T00:00:00Z' },
  { id: 's3', nombre: 'Mechas', descripcion: 'Mechas californianas o balayage.', duracionMin: 150, precio: 180000, activo: true, createdAt: '2024-01-01T00:00:00Z' },
  { id: 's4', nombre: 'Tratamiento hidratante', descripcion: 'Hidratación profunda con keratina.', duracionMin: 60, precio: 80000, activo: true, createdAt: '2024-01-01T00:00:00Z' },
  { id: 's5', nombre: 'Corte + Barba', descripcion: 'Corte masculino con perfilado de barba.', duracionMin: 60, precio: 55000, activo: true, createdAt: '2024-01-01T00:00:00Z' },
  { id: 's6', nombre: 'Peinado especial', descripcion: 'Para eventos o celebraciones.', duracionMin: 90, precio: 95000, activo: false, createdAt: '2024-01-01T00:00:00Z' },
];

function todayAt(h: number, m = 0): string {
  const d = new Date();
  d.setHours(h, m, 0, 0);
  return d.toISOString();
}

const CITAS_HOY = [
  { id: 'c1', qrToken: 'demo-qr-1', estado: 'CONFIRMADA', fechaHoraInicio: todayAt(9, 0), fechaHoraFin: todayAt(9, 45), duracionMin: 45, precioCobrado: null, origen: 'MANUAL', notas: null, motivoCancelacion: null, canceladoPor: null, checkedInAt: null, profesional: { id: 'p1', nombre: 'María García', colorAgenda: '#E85D4A' }, servicio: { id: 's1', nombre: 'Corte de cabello', duracionMin: 45, precio: 35000 }, cliente: { id: 'cl1', nombre: 'Laura Martínez', telefono: '3101234567' }, createdAt: todayAt(8) },
  { id: 'c2', qrToken: 'demo-qr-2', estado: 'EN_CURSO', fechaHoraInicio: todayAt(10, 0), fechaHoraFin: todayAt(12, 0), duracionMin: 120, precioCobrado: null, origen: 'MANUAL', notas: 'Preferencia por tonos cálidos', motivoCancelacion: null, canceladoPor: null, checkedInAt: todayAt(10), profesional: { id: 'p1', nombre: 'María García', colorAgenda: '#E85D4A' }, servicio: { id: 's2', nombre: 'Coloración completa', duracionMin: 120, precio: 120000 }, cliente: { id: 'cl2', nombre: 'Andrea Pérez', telefono: '3201234567' }, createdAt: todayAt(9) },
  { id: 'c3', qrToken: 'demo-qr-3', estado: 'PENDIENTE', fechaHoraInicio: todayAt(11, 0), fechaHoraFin: todayAt(12, 0), duracionMin: 60, precioCobrado: null, origen: 'MANUAL', notas: null, motivoCancelacion: null, canceladoPor: null, checkedInAt: null, profesional: { id: 'p2', nombre: 'Carlos López', colorAgenda: '#3B82F6' }, servicio: { id: 's5', nombre: 'Corte + Barba', duracionMin: 60, precio: 55000 }, cliente: { id: 'cl3', nombre: 'Juan Rodríguez', telefono: '3151234567' }, createdAt: todayAt(9) },
  { id: 'c4', qrToken: 'demo-qr-4', estado: 'COMPLETADA', fechaHoraInicio: todayAt(8, 0), fechaHoraFin: todayAt(8, 45), duracionMin: 45, precioCobrado: 35000, origen: 'MANUAL', notas: null, motivoCancelacion: null, canceladoPor: null, checkedInAt: todayAt(8), profesional: { id: 'p3', nombre: 'Ana Torres', colorAgenda: '#22C55E' }, servicio: { id: 's1', nombre: 'Corte de cabello', duracionMin: 45, precio: 35000 }, cliente: { id: 'cl4', nombre: 'Sofía Gómez', telefono: '3001234567' }, createdAt: todayAt(7) },
  { id: 'c5', qrToken: 'demo-qr-5', estado: 'PENDIENTE', fechaHoraInicio: todayAt(14, 0), fechaHoraFin: todayAt(14, 45), duracionMin: 45, precioCobrado: null, origen: 'MANUAL', notas: null, motivoCancelacion: null, canceladoPor: null, checkedInAt: null, profesional: { id: 'p1', nombre: 'María García', colorAgenda: '#E85D4A' }, servicio: { id: 's1', nombre: 'Corte de cabello', duracionMin: 45, precio: 35000 }, cliente: { id: 'cl5', nombre: 'Valentina Cruz', telefono: '3181234567' }, createdAt: todayAt(8) },
  { id: 'c6', qrToken: 'demo-qr-6', estado: 'PENDIENTE', fechaHoraInicio: todayAt(15, 30), fechaHoraFin: todayAt(17, 0), duracionMin: 90, precioCobrado: null, origen: 'MANUAL', notas: 'Para boda el sábado', motivoCancelacion: null, canceladoPor: null, checkedInAt: null, profesional: { id: 'p3', nombre: 'Ana Torres', colorAgenda: '#22C55E' }, servicio: { id: 's6', nombre: 'Peinado especial', duracionMin: 90, precio: 95000 }, cliente: { id: 'cl6', nombre: 'Isabella Moreno', telefono: '3121234567' }, createdAt: todayAt(8) },
];

function generateSlots(profesionalId: string, duracionMin: number): any[] {
  const slots = [];
  for (let h = 9; h < 18; h += Math.ceil(duracionMin / 60)) {
    const inicio = todayAt(h);
    const fin = new Date(new Date(inicio).getTime() + duracionMin * 60000).toISOString();
    slots.push({ profesionalId, profesionalNombre: PROFESIONALES.find(p => p.id === profesionalId)?.nombre ?? '', inicio, fin, duracionMin });
  }
  return slots;
}

const USERS = [
  { id: 'u1', nombre: 'Diego Salinas', email: 'admin@stilum.com', rol: 'ADMIN_TENANT', activo: true, ultimoLogin: new Date(Date.now() - 3600000).toISOString(), createdAt: '2024-01-01T00:00:00Z' },
  { id: 'u2', nombre: 'María García', email: 'maria@stilum.com', rol: 'PROFESIONAL', activo: true, ultimoLogin: new Date(Date.now() - 7200000).toISOString(), createdAt: '2024-01-10T00:00:00Z' },
  { id: 'u3', nombre: 'Carlos López', email: 'carlos@stilum.com', rol: 'PROFESIONAL', activo: true, ultimoLogin: null, createdAt: '2024-02-05T00:00:00Z' },
];

const TENANTS = [
  { id: 't1', nombreNegocio: 'Stilum Pro Bogotá', emailContacto: 'admin@stilum.com', ciudad: 'Bogotá', activo: true },
  { id: 't2', nombreNegocio: 'Salón Élite Medellín', emailContacto: 'admin@elite.com', ciudad: 'Medellín', activo: true },
  { id: 't3', nombreNegocio: 'Barber Club Cali', emailContacto: 'admin@barberclub.com', ciudad: 'Cali', activo: false },
];

const PLANES = [
  { id: 'plan1', nombre: 'Básico', precioMensual: 59000, maxProfesionales: 3, maxCitasMes: 200, whatsappHabilitado: false, recordatoriosHabilitados: false, reportesAvanzados: false, activo: true },
  { id: 'plan2', nombre: 'Profesional', precioMensual: 129000, maxProfesionales: 10, maxCitasMes: 1000, whatsappHabilitado: true, recordatoriosHabilitados: true, reportesAvanzados: false, activo: true },
  { id: 'plan3', nombre: 'Enterprise', precioMensual: 299000, maxProfesionales: 999, maxCitasMes: 999999, whatsappHabilitado: true, recordatoriosHabilitados: true, reportesAvanzados: true, activo: true },
];

const DEMO_TOKEN = 'demo.eyJ1c2VySWQiOiJ1MSIsInJvbCI6IkFETUlOX1RFTkFOVCJ9.demo';

const LOGIN_RESPONSE = {
  token: DEMO_TOKEN,
  userId: 'u1',
  nombre: 'Diego Salinas',
  email: 'admin@stilum.com',
  rol: 'ADMIN_TENANT',
  tenantId: 't1',
  tenantNombre: 'Stilum Pro Bogotá'
};

// ── Interceptor ────────────────────────────────────────────────────────

function mockResponse(body: unknown, status = 200) {
  return of(new HttpResponse({ status, body }));
}

export const demoInterceptor: HttpInterceptorFn = (req, next) => {
  if (!isDemoMode()) return next(req);

  const url = req.url;
  const method = req.method;

  // Auth
  if (url.includes('/auth/login') && method === 'POST') return mockResponse(LOGIN_RESPONSE);

  // Citas
  if (url.includes('/citas/disponibilidad') && method === 'GET') {
    const profId = req.params.get('profesionalId') ?? 'p1';
    const duracion = parseInt(req.params.get('duracionMin') ?? '45', 10);
    return mockResponse(generateSlots(profId, duracion));
  }
  if (url.match(/\/citas\/[^/]+\/confirmar/) && method === 'PATCH')  return mockResponse({ ...CITAS_HOY[0], estado: 'CONFIRMADA' });
  if (url.match(/\/citas\/[^/]+\/iniciar/)   && method === 'PATCH')  return mockResponse({ ...CITAS_HOY[0], estado: 'EN_CURSO' });
  if (url.match(/\/citas\/[^/]+\/completar/) && method === 'PATCH')  return mockResponse({ ...CITAS_HOY[0], estado: 'COMPLETADA' });
  if (url.match(/\/citas\/[^/]+\/cancelar/)  && method === 'PATCH')  return mockResponse({ ...CITAS_HOY[0], estado: 'CANCELADA' });
  if (url.match(/\/citas\/[^/]+\/no-show/)   && method === 'PATCH')  return mockResponse({ ...CITAS_HOY[0], estado: 'NO_SHOW' });
  if (url.includes('/citas') && method === 'POST') {
    const body: any = req.body;
    const newCita = { ...CITAS_HOY[0], id: `c-demo-${Date.now()}`, fechaHoraInicio: body?.fechaHoraInicio ?? todayAt(16), fechaHoraFin: todayAt(17), estado: 'PENDIENTE', cliente: { id: 'cl-new', nombre: body?.clienteNombre ?? 'Cliente Demo', telefono: body?.clienteTelefono ?? '3000000000' } };
    return mockResponse(newCita, 201);
  }
  if (url.includes('/citas') && method === 'GET') return mockResponse(CITAS_HOY);

  // Profesionales
  if (url.includes('/profesionales/activos')) return mockResponse(PROFESIONALES.filter(p => p.activo));
  if (url.match(/\/profesionales\/[^/]+\/activar/)   && method === 'PATCH') return mockResponse({});
  if (url.match(/\/profesionales\/[^/]+\/desactivar/) && method === 'PATCH') return mockResponse({});
  if (url.includes('/profesionales') && method === 'POST') return mockResponse({ ...PROFESIONALES[0], id: `p-${Date.now()}`, nombre: (req.body as any)?.nombre ?? 'Nuevo Profesional' }, 201);
  if (url.includes('/profesionales') && method === 'GET') return mockResponse(PROFESIONALES);

  // Servicios
  if (url.includes('/servicios/activos')) return mockResponse(SERVICIOS.filter(s => s.activo));
  if (url.match(/\/servicios\/[^/]+\/activar/)   && method === 'PATCH') return mockResponse({});
  if (url.match(/\/servicios\/[^/]+\/desactivar/) && method === 'PATCH') return mockResponse({});
  if (url.includes('/servicios') && method === 'POST') return mockResponse({ ...SERVICIOS[0], id: `s-${Date.now()}` }, 201);
  if (url.match(/\/servicios\/[^/]+/) && method === 'PUT') return mockResponse({ ...SERVICIOS[0] });
  if (url.includes('/servicios') && method === 'GET') return mockResponse(SERVICIOS);

  // Usuarios
  if (url.match(/\/usuarios\/[^/]+\/activar/)   && method === 'PATCH') return mockResponse({});
  if (url.match(/\/usuarios\/[^/]+\/desactivar/) && method === 'PATCH') return mockResponse({});
  if (url.includes('/usuarios') && method === 'POST') return mockResponse({ ...USERS[0], id: `u-${Date.now()}`, nombre: (req.body as any)?.nombre ?? 'Nuevo Usuario' }, 201);
  if (url.includes('/usuarios') && method === 'GET') return mockResponse(USERS);

  // Tenants
  if (url.match(/\/tenants\/[^/]+\/activar/)   && method === 'PATCH') return mockResponse({});
  if (url.match(/\/tenants\/[^/]+\/inactivar/) && method === 'PATCH') return mockResponse({});
  if (url.includes('/tenants') && method === 'POST') return mockResponse({ ...TENANTS[0], id: `t-${Date.now()}` }, 201);
  if (url.includes('/tenants') && method === 'GET') return mockResponse(TENANTS);

  // Planes
  if (url.includes('/planes') && method === 'GET') return mockResponse(PLANES);

  // Fallback — pasar la request real
  return next(req);
};
