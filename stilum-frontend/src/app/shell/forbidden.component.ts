import { Component, ChangeDetectionStrategy } from '@angular/core';
import { RouterLink } from '@angular/router';
import { StButtonComponent } from '../shared/ui/button/st-button.component';

@Component({
  selector: 'st-forbidden',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, StButtonComponent],
  template: `
    <div class="forbidden">
      <span class="forbidden__code">403</span>
      <h1>Acceso denegado</h1>
      <p>No tienes permisos para ver esta página.</p>
      <st-button label="Volver al inicio" variant="secondary" routerLink="/dashboard" />
    </div>
  `,
  styles: [`
    .forbidden {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      min-height: 60vh;
      gap: var(--space-4);
      text-align: center;
    }
    .forbidden__code {
      font-family: var(--font-display);
      font-size: 6rem;
      font-weight: var(--font-bold);
      color: var(--color-accent);
      line-height: 1;
    }
    h1 { color: var(--color-text-1); }
    p  { color: var(--color-text-2); }
  `]
})
export class ForbiddenComponent {}
