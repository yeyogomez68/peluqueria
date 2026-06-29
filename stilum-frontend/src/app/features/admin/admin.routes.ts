import { Routes } from '@angular/router';

export const ADMIN_ROUTES: Routes = [
  { path: '', redirectTo: 'tenants', pathMatch: 'full' },
  {
    path: 'tenants',
    loadComponent: () => import('./tenants/tenants-list.component').then(m => m.TenantsListComponent)
  }
];
