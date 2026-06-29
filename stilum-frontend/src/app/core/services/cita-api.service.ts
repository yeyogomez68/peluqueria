import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Cita, CreateCitaRequest, SlotDisponible } from '../models/cita.models';

@Injectable({ providedIn: 'root' })
export class CitaApiService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/citas`;

  listarPorFecha(fecha: string): Observable<Cita[]> {
    return this.http.get<Cita[]>(this.base, { params: { fecha } });
  }

  disponibilidad(profesionalId: string, fecha: string, duracionMin: number): Observable<SlotDisponible[]> {
    const params = new HttpParams()
      .set('profesionalId', profesionalId)
      .set('fecha', fecha)
      .set('duracionMin', duracionMin.toString());
    return this.http.get<SlotDisponible[]>(`${this.base}/disponibilidad`, { params });
  }

  crear(req: CreateCitaRequest): Observable<Cita> {
    return this.http.post<Cita>(this.base, req);
  }

  confirmar(id: string): Observable<Cita> {
    return this.http.patch<Cita>(`${this.base}/${id}/confirmar`, {});
  }

  iniciar(id: string): Observable<Cita> {
    return this.http.patch<Cita>(`${this.base}/${id}/iniciar`, {});
  }

  completar(id: string, precioCobrado?: number): Observable<Cita> {
    return this.http.patch<Cita>(`${this.base}/${id}/completar`,
      precioCobrado != null ? { precioCobrado } : {});
  }

  cancelar(id: string, motivo?: string, canceladoPor?: string): Observable<Cita> {
    return this.http.patch<Cita>(`${this.base}/${id}/cancelar`, { motivo, canceladoPor });
  }

  marcarNoShow(id: string): Observable<Cita> {
    return this.http.patch<Cita>(`${this.base}/${id}/no-show`, {});
  }

  registrarPago(citaId: string, body: { precioCobrado: number; metodoPago: string }): Observable<any> {
    return this.http.patch<any>(`${this.base}/${citaId}/pagar`, body);
  }
}
