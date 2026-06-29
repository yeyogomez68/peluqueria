export type UserRol = 'SUPER_ADMIN' | 'ADMIN_TENANT' | 'PROFESIONAL';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  userId: string;
  nombre: string;
  email: string;
  rol: UserRol;
  tenantId: string | null;
  tenantNombre: string | null;
}

export interface AuthUser {
  userId: string;
  nombre: string;
  email: string;
  rol: UserRol;
  tenantId: string | null;
  tenantNombre: string | null;
}
