import {
  Component, Input, Output, EventEmitter,
  ChangeDetectionStrategy
} from '@angular/core';
import { CommonModule } from '@angular/common';

export type BtnVariant = 'primary' | 'secondary' | 'ghost' | 'danger';
export type BtnSize    = 'sm' | 'md' | 'lg';

@Component({
  selector: 'st-button',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule],
  template: `
    <button
      [type]="type"
      [disabled]="disabled || loading"
      [class]="btnClass"
      (click)="onClick.emit($event)"
    >
      @if (loading) {
        <span class="btn-spinner"></span>
      } @else if (icon) {
        <i [class]="'pi ' + icon + ' btn-icon'"></i>
      }
      @if (label) {
        <span>{{ label }}</span>
      }
      <ng-content />
    </button>
  `,
  styleUrls: ['./st-button.component.scss']
})
export class StButtonComponent {
  @Input() label     = '';
  @Input() icon      = '';
  @Input() variant: BtnVariant = 'primary';
  @Input() size: BtnSize       = 'md';
  @Input() type: 'button' | 'submit' | 'reset' = 'button';
  @Input() disabled  = false;
  @Input() loading   = false;
  @Input() fullWidth = false;
  @Output() onClick  = new EventEmitter<MouseEvent>();

  get btnClass(): string {
    return [
      'st-btn',
      `st-btn--${this.variant}`,
      `st-btn--${this.size}`,
      this.fullWidth ? 'st-btn--full' : '',
      this.loading   ? 'st-btn--loading' : '',
      (!this.label && this.icon) ? 'st-btn--icon-only' : ''
    ].filter(Boolean).join(' ');
  }
}
