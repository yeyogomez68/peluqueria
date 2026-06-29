import { UserRol } from './auth.models';

export interface User {
  id: string;
  nombre: string;
  email: string;
  rol: UserRol;
  activo: boolean;
  ultimoLogin: string | null;
  createdAt: string;
}

export interface CreateUserRequest {
  nombre: string;
  email: string;
  password: string;
  rol: UserRol;
}
