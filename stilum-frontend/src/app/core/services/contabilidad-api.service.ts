import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface ResumenDia {
  fecha: string;
  totalCitas: number;
  totalVendido: number;
  totalComisiones: number;
  totalNegocio: number;
  porProfesional: Array<{ nombre: string; citas: number; totalVendido: number; comision: number; comisionPorcentaje: number }>;
  porServicio: Array<{ nombre: string; cantidad: number; totalVendido: number }>;
  porMetodoPago: Array<{ metodo: string; cantidad: number; total: number }>;
}

@Injectable({ providedIn: 'root' })
export class ContabilidadApiService {
  private base = `${environment.apiUrl}/contabilidad`;
  constructor(private http: HttpClient) {}

  resumenDia(fecha?: string): Observable<ResumenDia> {
    const params: Record<string, string> = fecha ? { fecha } : {};
    return this.http.get<ResumenDia>(`${this.base}/resumen-dia`, { params });
  }
}
