import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User, CreateUserRequest } from '../../../core/models/user.models';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class UserApiService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/users`;

  listar(): Observable<User[]>                   { return this.http.get<User[]>(this.base); }
  crear(req: CreateUserRequest): Observable<User>{ return this.http.post<User>(this.base, req); }
  activar(id: string): Observable<User>          { return this.http.patch<User>(`${this.base}/${id}/activar`, {}); }
  desactivar(id: string): Observable<User>       { return this.http.patch<User>(`${this.base}/${id}/desactivar`, {}); }
}
