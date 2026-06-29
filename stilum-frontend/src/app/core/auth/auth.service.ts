import { Injectable, signal, computed, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { AuthUser, LoginRequest, LoginResponse, UserRol } from '../models/auth.models';
import { environment } from '../../../environments/environment';

const TOKEN_KEY = 'stilum-token';
const USER_KEY  = 'stilum-user';

/**
 * AuthService — gestiona autenticación con Angular Signals.
 * SK-F-01: Signals en vez de BehaviorSubject para estado local.
 * SK-F-04: Functional interceptors leen el token de aquí.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http   = inject(HttpClient);
  private readonly router = inject(Router);

  // ── Estado reactivo ───────────────────────────────────────────────────
  readonly currentUser = signal<AuthUser | null>(this.loadUser());
  readonly isAuthenticated = computed(() => this.currentUser() !== null);
  readonly isSuperAdmin = computed(() => this.currentUser()?.rol === 'SUPER_ADMIN');
  readonly isAdminTenant = computed(() => this.currentUser()?.rol === 'ADMIN_TENANT');

  // ── API ───────────────────────────────────────────────────────────────
  login(req: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${environment.apiUrl}/auth/login`, req).pipe(
      tap(res => {
        localStorage.setItem(TOKEN_KEY, res.token);
        const user: AuthUser = {
          userId:      res.userId,
          nombre:      res.nombre,
          email:       res.email,
          rol:         res.rol,
          tenantId:    res.tenantId,
          tenantNombre: res.tenantNombre
        };
        localStorage.setItem(USER_KEY, JSON.stringify(user));
        this.currentUser.set(user);
      })
    );
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this.currentUser.set(null);
    this.router.navigate(['/auth/login']);
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  hasRole(rol: UserRol): boolean {
    return this.currentUser()?.rol === rol;
  }

  /** Ruta destino tras login según rol del usuario. */
  getPostLoginRoute(): string {
    const rol = this.currentUser()?.rol;
    if (rol === 'SUPER_ADMIN') return '/admin/tenants';
    return '/dashboard';
  }

  // ── Privados ──────────────────────────────────────────────────────────
  private loadUser(): AuthUser | null {
    try {
      const raw = localStorage.getItem(USER_KEY);
      return raw ? JSON.parse(raw) : null;
    } catch {
      return null;
    }
  }
}
