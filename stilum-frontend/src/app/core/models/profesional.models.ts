export interface HorarioItem {
  id: string;
  diaSemana: number;   // ISO: 1=Lunes … 7=Domingo
  horaInicio: string;  // HH:mm
  horaFin: string;
  activo: boolean;
}

export interface Profesional {
  id: string;
  nombre: string;
  especialidad: string | null;
  bio: string | null;
  fotoUrl: string | null;
  colorAgenda: string | null;
  activo: boolean;
  horarios: HorarioItem[];
  createdAt: string;
}

export interface CreateProfesionalRequest {
  nombre: string;
  email: string;
  password: string;
  especialidad?: string;
  bio?: string;
  colorAgenda?: string;
  horarios?: HorarioRequest[];
}

export interface UpdateProfesionalRequest {
  nombre: string;
  especialidad?: string;
  bio?: string;
  fotoUrl?: string;
  colorAgenda?: string;
  horarios?: HorarioRequest[];
}

export interface HorarioRequest {
  diaSemana: number;
  horaInicio: string;
  horaFin: string;
}

export const DIA_SEMANA_LABEL: Record<number, string> = {
  1: 'Lunes', 2: 'Martes', 3: 'Miércoles',
  4: 'Jueves', 5: 'Viernes', 6: 'Sábado', 7: 'Domingo'
};
