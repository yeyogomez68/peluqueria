import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Profesional, CreateProfesionalRequest, UpdateProfesionalRequest } from '../../../core/models/profesional.models';

@Injectable({ providedIn: 'root' })
export class ProfesionalApiService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/profesionales`;

  listar(): Observable<Profesional[]> {
    return this.http.get<Profesional[]>(this.base);
  }

  listarActivos(): Observable<Profesional[]> {
    return this.http.get<Profesional[]>(`${this.base}/activos`);
  }

  crear(req: CreateProfesionalRequest): Observable<Profesional> {
    return this.http.post<Profesional>(this.base, req);
  }

  actualizar(id: string, req: UpdateProfesionalRequest): Observable<Profesional> {
    return this.http.put<Profesional>(`${this.base}/${id}`, req);
  }

  activar(id: string): Observable<Profesional> {
    return this.http.patch<Profesional>(`${this.base}/${id}/activar`, {});
  }

  desactivar(id: string): Observable<Profesional> {
    return this.http.patch<Profesional>(`${this.base}/${id}/desactivar`, {});
  }
}
