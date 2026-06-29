import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Tenant, TenantSummary, CreateTenantRequest, UpdateTenantRequest } from '../../../core/models/tenant.models';
import { Plan } from '../../../core/models/plan.models';
import { environment } from '../../../../environments/environment';

/**
 * TenantApiService — acceso a los endpoints de tenants y planes.
 * SK-F-02: Service en la feature, no en core (específico de esta feature).
 */
@Injectable({ providedIn: 'root' })
export class TenantApiService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/admin/tenants`;
  private readonly planesBase = `${environment.apiUrl}/planes`;

  listar(): Observable<TenantSummary[]> {
    return this.http.get<TenantSummary[]>(this.base);
  }

  obtener(id: string): Observable<Tenant> {
    return this.http.get<Tenant>(`${this.base}/${id}`);
  }

  crear(req: CreateTenantRequest): Observable<Tenant> {
    return this.http.post<Tenant>(this.base, req);
  }

  actualizar(id: string, req: UpdateTenantRequest): Observable<Tenant> {
    return this.http.put<Tenant>(`${this.base}/${id}`, req);
  }

  activar(id: string): Observable<Tenant> {
    return this.http.patch<Tenant>(`${this.base}/${id}/activar`, {});
  }

  inactivar(id: string): Observable<Tenant> {
    return this.http.patch<Tenant>(`${this.base}/${id}/inactivar`, {});
  }

  listarPlanes(): Observable<Plan[]> {
    return this.http.get<Plan[]>(this.planesBase);
  }
}
