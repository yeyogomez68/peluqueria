import { Routes } from '@angular/router';

export const TENANT_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./users/users-list.component').then(m => m.UsersListComponent)
  }
];
