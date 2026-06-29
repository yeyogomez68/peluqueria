import { Component, Input, ChangeDetectionStrategy } from '@angular/core';

export type SpinnerSize = 'sm' | 'md' | 'lg' | 'xl';

interface SpinnerDimensions {
  size: string;
  borderWidth: string;
}

@Component({
  selector: 'st-spinner',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="st-spinner__wrapper" [class.st-spinner__wrapper--overlay]="overlay">
      <div class="st-spinner__content">
        <div
          class="st-spinner__ring"
          [style.width]="spinnerDimensions.size"
          [style.height]="spinnerDimensions.size"
          [style.border-width]="spinnerDimensions.borderWidth"
        ></div>
        @if (label) {
          <span class="st-spinner__label">{{ label }}</span>
        }
      </div>
    </div>
  `,
  styles: [`
    :host { display: contents; }

    .st-spinner__wrapper {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: var(--space-3);
    }

    .st-spinner__wrapper--overlay {
      position: absolute;
      inset: 0;
      background: rgba(128, 128, 128, 0.15);
      backdrop-filter: blur(2px);
      z-index: 10;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .st-spinner__content {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: var(--space-3);
    }

    .st-spinner__ring {
      border-radius: 50%;
      border-style: solid;
      border-color: var(--color-border-strong);
      border-top-color: var(--color-accent);
      animation: st-spin 0.7s linear infinite;
      flex-shrink: 0;
    }

    .st-spinner__label {
      font-size: var(--text-sm);
      color: var(--color-text-2);
    }

    @keyframes st-spin {
      to { transform: rotate(360deg); }
    }
  `]
})
export class StSpinnerComponent {
  @Input() size: SpinnerSize = 'md';
  @Input() label = '';
  @Input() overlay = false;

  get spinnerDimensions(): SpinnerDimensions {
    const map: Record<SpinnerSize, SpinnerDimensions> = {
      sm: { size: '16px', borderWidth: '2px' },
      md: { size: '28px', borderWidth: '3px' },
      lg: { size: '44px', borderWidth: '4px' },
      xl: { size: '64px', borderWidth: '4px' },
    };
    return map[this.size];
  }
}
