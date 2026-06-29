import { Routes } from '@angular/router';

export const PROFESIONAL_PORTAL_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./profesional-portal.component').then(m => m.ProfesionalPortalComponent)
  }
];
