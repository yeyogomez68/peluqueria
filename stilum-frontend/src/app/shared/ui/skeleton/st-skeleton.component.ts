import { Component, Input, ChangeDetectionStrategy } from '@angular/core';
import { NgStyle } from '@angular/common';

export type SkeletonVariant = 'text' | 'circle' | 'rect';

@Component({
  selector: 'st-skeleton',
  standalone: true,
  imports: [NgStyle],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (variant === 'text' && lines > 1) {
      @for (line of lineItems; track $index) {
        <div class="st-skeleton__block" [ngStyle]="getLineStyle($index)"></div>
      }
    } @else {
      <div class="st-skeleton__block" [ngStyle]="blockStyle"></div>
    }
  `,
  styleUrls: ['./st-skeleton.component.scss']
})
export class StSkeletonComponent {
  @Input() variant: SkeletonVariant = 'text';
  @Input() width = '100%';
  @Input() height = '1rem';
  @Input() lines = 1;
  @Input() borderRadius = '';

  get lineItems(): number[] {
    return Array.from({ length: this.lines }, (_, i) => i);
  }

  private get radius(): string {
    if (this.borderRadius) return this.borderRadius;
    if (this.variant === 'circle') return '50%';
    if (this.variant === 'rect')   return 'var(--radius-md)';
    return 'var(--radius-full)';
  }

  get blockStyle(): Record<string, string> {
    const isCircle = this.variant === 'circle';
    return {
      width:         this.width,
      height:        isCircle ? this.width : this.height,
      'border-radius': this.radius,
    };
  }

  getLineStyle(index: number): Record<string, string> {
    return {
      width:           index === this.lines - 1 ? '60%' : this.width,
      height:          this.height,
      'border-radius': this.radius,
    };
  }
}
