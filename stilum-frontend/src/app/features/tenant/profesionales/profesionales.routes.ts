import { Routes } from '@angular/router';

export const PROFESIONALES_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./profesionales-list.component').then(m => m.ProfesionalesListComponent)
  }
];
