export type CitaEstado =
  | 'PENDIENTE'
  | 'CONFIRMADA'
  | 'EN_CURSO'
  | 'COMPLETADA'
  | 'CANCELADA'
  | 'NO_SHOW';

export interface CitaProfesional { id: string; nombre: string; colorAgenda: string | null; }
export interface CitaServicio    { id: string; nombre: string; duracionMin: number; precio: number; }
export interface CitaCliente     { id: string; nombre: string; telefono: string; }

export interface Cita {
  id: string;
  qrToken: string;
  estado: CitaEstado;
  fechaHoraInicio: string;   // ISO ZonedDateTime
  fechaHoraFin: string;
  duracionMin: number;
  precioCobrado: number | null;
  origen: string;
  notas: string | null;
  motivoCancelacion: string | null;
  canceladoPor: string | null;
  checkedInAt: string | null;
  profesional: CitaProfesional;
  servicio: CitaServicio;
  cliente: CitaCliente;
  createdAt: string;
}

export interface CreateCitaRequest {
  profesionalId: string;
  servicioId: string;
  clienteTelefono: string;
  clienteNombre?: string;
  fechaHoraInicio: string;   // ISO ZonedDateTime
  origen?: string;
  notas?: string;
}

export interface SlotDisponible {
  profesionalId: string;
  profesionalNombre: string;
  inicio: string;
  fin: string;
  duracionMin: number;
}

/** Labels y severidades de PrimeNG Tag para cada estado */
export const CITA_ESTADO_LABEL: Record<CitaEstado, string> = {
  PENDIENTE:  'Pendiente',
  CONFIRMADA: 'Confirmada',
  EN_CURSO:   'En curso',
  COMPLETADA: 'Completada',
  CANCELADA:  'Cancelada',
  NO_SHOW:    'No show'
};

export const CITA_ESTADO_SEVERITY: Record<CitaEstado, 'success' | 'secondary' | 'info' | 'warn' | 'danger' | 'contrast'> = {
  PENDIENTE:  'warn',
  CONFIRMADA: 'info',
  EN_CURSO:   'success',
  COMPLETADA: 'secondary',
  CANCELADA:  'danger',
  NO_SHOW:    'danger'
};
