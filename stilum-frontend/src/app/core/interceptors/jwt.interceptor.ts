import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../auth/auth.service';

/**
 * JwtInterceptor — adjunta el token JWT a cada request hacia /api.
 * SK-F-04: Interceptor funcional (no basado en clase).
 */
export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const auth  = inject(AuthService);
  const token = auth.getToken();

  // Solo adjuntar token en requests a nuestra API
  if (token && req.url.includes('/api')) {
    req = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });
  }

  return next(req);
};
