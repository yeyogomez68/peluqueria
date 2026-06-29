import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { Router } from '@angular/router';
import { AuthService } from '../auth/auth.service';

/**
 * ErrorInterceptor — maneja errores HTTP globalmente.
 * 401 → logout + redirige a login.
 * SK-F-04: Interceptor funcional.
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const auth   = inject(AuthService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      // 401 = token expirado/ausente  |  403 = acceso denegado por Spring Security
      if (error.status === 401 || error.status === 403) {
        const isLoginCall = req.url.includes('/auth/login');
        if (!isLoginCall) {
          auth.logout();
          router.navigate(['/auth/login']);
        }
      }
      return throwError(() => error);
    })
  );
};
