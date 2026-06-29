export interface Servicio {
  id: string;
  nombre: string;
  descripcion: string | null;
  duracionMin: number;
  precio: number;
  activo: boolean;
  createdAt: string;
}

export interface CreateServicioRequest {
  nombre: string;
  descripcion?: string;
  duracionMin: number;
  precio: number;
}

export interface UpdateServicioRequest {
  nombre?: string;
  descripcion?: string;
  duracionMin?: number;
  precio?: number;
}
