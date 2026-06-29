import { Component, Input, ChangeDetectionStrategy } from '@angular/core';

export type ProgressVariant = 'default' | 'success' | 'warning' | 'danger';
export type ProgressSize    = 'sm' | 'md' | 'lg';

@Component({
  selector: 'st-progress-bar',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (label || showValue) {
      <div class="st-progress__header">
        @if (label) { <span class="st-progress__label">{{ label }}</span> }
        @if (showValue) { <span class="st-progress__value">{{ clampedValue }}%</span> }
      </div>
    }
    <div [class]="'st-progress__track st-progress__track--' + size">
      <div
        [class]="fillClass"
        [style.width.%]="clampedValue"
      ></div>
    </div>
  `,
  styleUrls: ['./st-progress-bar.component.scss']
})
export class StProgressBarComponent {
  @Input() value = 0;
  @Input() label = '';
  @Input() showValue = true;
  @Input() variant: ProgressVariant = 'default';
  @Input() size: ProgressSize = 'md';
  @Input() animated = false;

  get clampedValue(): number {
    return Math.min(100, Math.max(0, this.value));
  }

  get fillClass(): string {
    return [
      'st-progress__fill',
      `st-progress__fill--${this.variant}`,
      this.animated ? 'st-progress__fill--animated' : ''
    ].filter(Boolean).join(' ');
  }
}
