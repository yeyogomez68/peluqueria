import { CanActivateFn, Router, ActivatedRouteSnapshot } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../auth/auth.service';
import { UserRol } from '../models/auth.models';

/**
 * roleGuard — protege rutas según el rol del usuario.
 * Uso en routes: canActivate: [authGuard, roleGuard], data: { roles: ['SUPER_ADMIN'] }
 * SK-F-09: Guard funcional.
 */
export const roleGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const auth   = inject(AuthService);
  const router = inject(Router);

  const requiredRoles: UserRol[] = route.data?.['roles'] ?? [];
  const userRol = auth.currentUser()?.rol;

  if (!userRol || (requiredRoles.length > 0 && !requiredRoles.includes(userRol))) {
    router.navigate(['/forbidden']);
    return false;
  }

  return true;
};
