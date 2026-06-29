import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Servicio, CreateServicioRequest, UpdateServicioRequest } from '../../../core/models/servicio.models';

@Injectable({ providedIn: 'root' })
export class ServicioApiService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/servicios`;

  listar(): Observable<Servicio[]> {
    return this.http.get<Servicio[]>(this.base);
  }

  listarActivos(): Observable<Servicio[]> {
    return this.http.get<Servicio[]>(`${this.base}/activos`);
  }

  crear(req: CreateServicioRequest): Observable<Servicio> {
    return this.http.post<Servicio>(this.base, req);
  }

  actualizar(id: string, req: UpdateServicioRequest): Observable<Servicio> {
    return this.http.put<Servicio>(`${this.base}/${id}`, req);
  }

  activar(id: string): Observable<Servicio> {
    return this.http.patch<Servicio>(`${this.base}/${id}/activar`, {});
  }

  desactivar(id: string): Observable<Servicio> {
    return this.http.patch<Servicio>(`${this.base}/${id}/desactivar`, {});
  }
}
