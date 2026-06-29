import { Component, Input, ChangeDetectionStrategy } from '@angular/core';

export type BadgeVariant =
  | 'default' | 'success' | 'warning' | 'danger' | 'info'
  | 'pendiente' | 'confirmada' | 'en_curso' | 'completada' | 'cancelada' | 'no_show';

@Component({
  selector: 'st-badge',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<span [class]="'st-badge st-badge--' + variant">{{ label }}</span>`,
  styles: [`
    :host { display: contents; }

    .st-badge {
      display: inline-flex;
      align-items: center;
      padding: 0.2rem 0.6rem;
      border-radius: var(--radius-full);
      font-size: var(--text-xs);
      font-weight: var(--font-semibold);
      letter-spacing: var(--tracking-wide);
      text-transform: uppercase;
      white-space: nowrap;
    }
    .st-badge--default    { background: var(--color-bg-elevated); color: var(--color-text-2); }
    .st-badge--success    { background: var(--color-success-bg); color: var(--color-success); }
    .st-badge--warning    { background: var(--color-warning-bg); color: var(--color-warning); }
    .st-badge--danger     { background: var(--color-danger-bg); color: var(--color-danger); }
    .st-badge--info       { background: var(--color-info-bg); color: var(--color-info); }
    .st-badge--pendiente  { background: var(--cita-pendiente-bg); color: var(--cita-pendiente-text); }
    .st-badge--confirmada { background: var(--cita-confirmada-bg); color: var(--cita-confirmada-text); }
    .st-badge--en_curso   { background: var(--cita-en-curso-bg); color: var(--cita-en-curso-text); }
    .st-badge--completada { background: var(--cita-completada-bg); color: var(--cita-completada-text); }
    .st-badge--cancelada  { background: var(--cita-cancelada-bg); color: var(--cita-cancelada-text); }
    .st-badge--no_show    { background: var(--cita-no-show-bg); color: var(--cita-no-show-text); }
  `]
})
export class StBadgeComponent {
  @Input() label   = '';
  @Input() variant: BadgeVariant = 'default';
}
