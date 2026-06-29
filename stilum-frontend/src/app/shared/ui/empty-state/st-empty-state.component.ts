import { Component, Input, Output, EventEmitter, ChangeDetectionStrategy } from '@angular/core';

@Component({
  selector: 'st-empty-state',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="st-empty-state__icon">
      <i class="pi" [class]="'pi-' + icon"></i>
    </div>
    <span class="st-empty-state__title">{{ title }}</span>
    @if (message) {
      <p class="st-empty-state__message">{{ message }}</p>
    }
    @if (actionLabel) {
      <button class="st-empty-action" type="button" (click)="action.emit()">
        {{ actionLabel }}
      </button>
    }
  `,
  styles: [`
    :host {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: var(--space-4);
      padding: var(--space-16);
      text-align: center;
    }

    .st-empty-state__icon {
      width: 72px;
      height: 72px;
      background: var(--color-bg-elevated);
      border-radius: var(--radius-full);
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .st-empty-state__icon .pi {
      font-size: 2rem;
      color: var(--color-text-3);
    }

    .st-empty-state__title {
      font-family: var(--font-display);
      font-size: var(--text-xl);
      color: var(--color-text-1);
      font-weight: var(--font-semibold);
      letter-spacing: var(--tracking-tight);
    }

    .st-empty-state__message {
      font-size: var(--text-sm);
      color: var(--color-text-2);
      max-width: 360px;
      line-height: var(--leading-normal);
      margin: 0;
    }

    .st-empty-action {
      background: var(--color-accent);
      color: #fff;
      border: none;
      padding: var(--space-3) var(--space-6);
      border-radius: var(--radius-md);
      font-family: var(--font-body);
      font-size: var(--text-sm);
      font-weight: var(--font-medium);
      cursor: pointer;
      letter-spacing: var(--tracking-wide);
      text-transform: uppercase;
    }

    .st-empty-action:hover {
      background: var(--color-accent-subtle);
    }
  `]
})
export class StEmptyStateComponent {
  @Input() icon = 'inbox';
  @Input() title = 'Sin resultados';
  @Input() message = '';
  @Input() actionLabel = '';
  @Output() action = new EventEmitter<void>();
}
