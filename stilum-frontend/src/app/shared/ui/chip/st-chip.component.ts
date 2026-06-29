import { Component, Input, Output, EventEmitter, ChangeDetectionStrategy } from '@angular/core';

export type ChipVariant = 'default' | 'accent' | 'success' | 'warning' | 'danger' | 'info';
export type ChipSize    = 'sm' | 'md';

interface ChipColors {
  background: string;
  color: string;
  border?: string;
}

const VARIANT_COLORS: Record<ChipVariant, ChipColors> = {
  default: { background: 'var(--color-bg-elevated)', color: 'var(--color-text-2)', border: '1px solid var(--color-border)' },
  accent:  { background: 'var(--color-accent-subtle)', color: 'var(--color-accent)' },
  success: { background: 'var(--color-success-bg)',   color: 'var(--color-success)' },
  warning: { background: 'var(--color-warning-bg)',   color: 'var(--color-warning)' },
  danger:  { background: 'var(--color-danger-bg)',    color: 'var(--color-danger)' },
  info:    { background: 'var(--color-info-bg)',      color: 'var(--color-info)' },
};

@Component({
  selector: 'st-chip',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <span [style]="chipStyle()">
      @if (icon) {
        <i [class]="'pi pi-' + icon" [style]="iconStyle"></i>
      }
      {{ label }}
      @if (removable) {
        <button
          type="button"
          [style]="removeButtonStyle"
          (click)="removed.emit()"
          (mouseenter)="removeHovered = true"
          (mouseleave)="removeHovered = false"
        >
          <i class="pi pi-times" [style]="removeIconStyle"></i>
        </button>
      }
    </span>
  `,
  styles: [':host { display: contents; }'],
})
export class StChipComponent {
  @Input() label = '';
  @Input() icon = '';
  @Input() removable = false;
  @Input() variant: ChipVariant = 'default';
  @Input() size: ChipSize = 'md';
  @Output() removed = new EventEmitter<void>();

  removeHovered = false;

  chipStyle(): Record<string, string> {
    const colors = VARIANT_COLORS[this.variant];
    const padding = this.size === 'sm'
      ? '2px var(--space-3)'
      : 'var(--space-1) var(--space-4)';
    const fontSize = this.size === 'sm' ? 'var(--text-xs)' : 'var(--text-sm)';

    return {
      display: 'inline-flex',
      alignItems: 'center',
      gap: 'var(--space-2)',
      borderRadius: 'var(--radius-full)',
      fontFamily: 'var(--font-body)',
      fontWeight: 'var(--font-medium)',
      whiteSpace: 'nowrap',
      padding,
      fontSize,
      background: colors.background,
      color: colors.color,
      ...(colors.border ? { border: colors.border } : {}),
    };
  }

  get iconStyle(): Record<string, string> {
    return {
      fontSize: '0.7em',
      opacity: '0.8',
    };
  }

  get removeButtonStyle(): Record<string, string> {
    return {
      background: 'none',
      border: 'none',
      cursor: 'pointer',
      padding: '0',
      color: 'inherit',
      display: 'flex',
      alignItems: 'center',
    };
  }

  get removeIconStyle(): Record<string, string> {
    return {
      fontSize: '0.7em',
      opacity: this.removeHovered ? '1' : '0.6',
      transition: 'opacity var(--duration-fast)',
    };
  }
}
