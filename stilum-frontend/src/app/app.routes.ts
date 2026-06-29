import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  // Redirect raíz
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },

  // Auth (público)
  {
    path: 'auth',
    loadChildren: () => import('./features/auth/auth.routes').then(m => m.AUTH_ROUTES)
  },

  // Shell protegido
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./shell/app-shell.component').then(m => m.AppShellComponent),
    children: [
      // Dashboard (ADMIN_TENANT + PROFESIONAL)
      {
        path: 'dashboard',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN_TENANT', 'PROFESIONAL'] },
        loadChildren: () => import('./features/dashboard/dashboard.routes').then(m => m.DASHBOARD_ROUTES)
      },

      // Super Admin
      {
        path: 'admin',
        canActivate: [roleGuard],
        data: { roles: ['SUPER_ADMIN'] },
        loadChildren: () => import('./features/admin/admin.routes').then(m => m.ADMIN_ROUTES)
      },

      // Gestión de usuarios del tenant
      {
        path: 'usuarios',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN_TENANT'] },
        loadChildren: () => import('./features/tenant/tenant.routes').then(m => m.TENANT_ROUTES)
      },

      // Profesionales
      {
        path: 'profesionales',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN_TENANT'] },
        loadChildren: () =>
          import('./features/tenant/profesionales/profesionales.routes')
            .then(m => m.PROFESIONALES_ROUTES)
      },

      // Servicios
      {
        path: 'servicios',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN_TENANT'] },
        loadChildren: () =>
          import('./features/tenant/servicios/servicios.routes')
            .then(m => m.SERVICIOS_ROUTES)
      },

      // Contabilidad / Cierre de caja
      {
        path: 'contabilidad',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN_TENANT'] },
        loadChildren: () =>
          import('./features/contabilidad/contabilidad.routes')
            .then(m => m.CONTABILIDAD_ROUTES)
      },

      // Portal del profesional
      {
        path: 'mi-portal',
        canActivate: [roleGuard],
        data: { roles: ['PROFESIONAL', 'ADMIN_TENANT'] },
        loadChildren: () =>
          import('./features/profesional-portal/profesional-portal.routes')
            .then(m => m.PROFESIONAL_PORTAL_ROUTES)
      },

      // Design System (dev only)
      {
        path: 'design-system',
        loadComponent: () =>
          import('./features/design-system/design-system.component')
            .then(m => m.DesignSystemComponent)
      }
    ]
  },

  // Forbidden
  { path: 'forbidden', loadComponent: () => import('./shell/forbidden.component').then(m => m.ForbiddenComponent) },

  // 404
  { path: '**', redirectTo: '/dashboard' }
];
