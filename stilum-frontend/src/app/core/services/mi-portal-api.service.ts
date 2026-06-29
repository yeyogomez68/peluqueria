import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface MisIngresos {
  inicio: string;
  fin: string;
  totalCitas: number;
  totalFacturado: number;
  totalComision: number;
  detalle: Array<{
    fecha: string;
    servicio: string;
    cliente: string;
    precio: number;
    comision: number;
    metodoPago: string;
  }>;
}

@Injectable({ providedIn: 'root' })
export class MiPortalApiService {
  private base = `${environment.apiUrl}/mi-portal`;
  constructor(private http: HttpClient) {}

  misCitas(profesionalId: string, fecha: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/mis-citas`, { params: { profesionalId, fecha } });
  }

  misIngresos(profesionalId: string, inicio: string, fin: string): Observable<MisIngresos> {
    return this.http.get<MisIngresos>(`${this.base}/ingresos`, { params: { profesionalId, inicio, fin } });
  }
}
