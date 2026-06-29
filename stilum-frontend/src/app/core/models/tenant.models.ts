export interface TenantSummary {
  id: string;
  nombreNegocio: string;
  emailContacto: string;
  ciudad: string;
  activo: boolean;
}

export interface Tenant {
  id: string;
  nombreNegocio: string;
  emailContacto: string;
  telefonoContacto: string | null;
  ciudad: string | null;
  pais: string;
  logoUrl: string | null;
  activo: boolean;
  createdAt: string;
  fechaInactivacion: string | null;
}

export interface CreateTenantRequest {
  nombreNegocio: string;
  emailContacto: string;
  telefonoContacto?: string;
  ciudad?: string;
  pais?: string;
  planId: string;
  adminNombre: string;
  adminPassword: string;
}

export interface UpdateTenantRequest {
  nombreNegocio?: string;
  telefonoContacto?: string;
  ciudad?: string;
  pais?: string;
  logoUrl?: string;
}
