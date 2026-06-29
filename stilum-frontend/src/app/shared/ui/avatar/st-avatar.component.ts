import { Component, Input, ChangeDetectionStrategy } from '@angular/core';

export type AvatarSize = 'xs' | 'sm' | 'md' | 'lg' | 'xl';

const SIZE_MAP: Record<AvatarSize, { box: string; font: string }> = {
  xs: { box: '24px', font: '10px' },
  sm: { box: '32px', font: '12px' },
  md: { box: '40px', font: '14px' },
  lg: { box: '52px', font: '18px' },
  xl: { box: '72px', font: '24px' },
};

@Component({
  selector: 'st-avatar',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div [style]="containerStyle">
      @if (src && !imgError) {
        <img
          [src]="src"
          [alt]="name"
          (error)="imgError = true"
          [style]="imgStyle"
        />
      } @else {
        <span [style]="initialsStyle">{{ initials }}</span>
      }
    </div>
  `,
  styles: [':host { display: contents; }'],
})
export class StAvatarComponent {
  private readonly AVATAR_COLORS = [
    '#E85D4A', '#3B82F6', '#22C55E', '#8B5CF6',
    '#F59E0B', '#06B6D4', '#EC4899', '#14B8A6',
  ];

  @Input() name = '';
  @Input() src = '';
  @Input() size: AvatarSize = 'md';
  @Input() color = '';

  imgError = false;

  get initials(): string {
    return this.name
      .split(/\s+/)
      .filter(Boolean)
      .slice(0, 2)
      .map(w => w[0].toUpperCase())
      .join('');
  }

  get bgColor(): string {
    if (this.color) return this.color;
    const idx = this.name.charCodeAt(0) % this.AVATAR_COLORS.length;
    return this.AVATAR_COLORS[isNaN(idx) ? 0 : idx];
  }

  get containerStyle(): Record<string, string> {
    const { box } = SIZE_MAP[this.size];
    return {
      width: box,
      height: box,
      minWidth: box,
      minHeight: box,
      borderRadius: 'var(--radius-full)',
      background: this.bgColor,
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      overflow: 'hidden',
    };
  }

  get imgStyle(): Record<string, string> {
    return {
      width: '100%',
      height: '100%',
      objectFit: 'cover',
      borderRadius: 'var(--radius-full)',
    };
  }

  get initialsStyle(): Record<string, string> {
    const { font } = SIZE_MAP[this.size];
    return {
      fontSize: font,
      fontFamily: 'var(--font-body)',
      fontWeight: 'var(--font-semibold)',
      color: '#fff',
      lineHeight: '1',
      userSelect: 'none',
    };
  }
}
