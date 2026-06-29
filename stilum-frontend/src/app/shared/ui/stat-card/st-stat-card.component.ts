import { Component, Input, ChangeDetectionStrategy } from '@angular/core';

export type StatVariant = 'default' | 'accent' | 'success' | 'warning' | 'info';

@Component({
  selector: 'st-stat-card',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="st-stat-card">
      <div class="st-stat-card__header">
        <span class="st-stat-card__label">{{ label }}</span>
        @if (icon) {
          <div [class]="'st-stat-card__icon st-stat-card__icon--' + variant">
            <i [class]="'pi pi-' + icon"></i>
          </div>
        }
      </div>

      <div class="st-stat-card__value">{{ value }}</div>

      @if (trend !== undefined) {
        <div [class]="'st-stat-card__trend st-stat-card__trend--' + (trend >= 0 ? 'up' : 'down')">
          <i [class]="'pi ' + (trend >= 0 ? 'pi-arrow-up' : 'pi-arrow-down')"></i>
          <span>{{ trend >= 0 ? '+' : '' }}{{ trend }}%</span>
          @if (trendLabel) {
            <span class="st-stat-card__trend-label">{{ trendLabel }}</span>
          }
        </div>
      }
    </div>
  `,
  styleUrls: ['./st-stat-card.component.scss']
})
export class StStatCardComponent {
  @Input() label = '';
  @Input() value: string | number = '';
  @Input() icon = '';
  @Input() trend?: number;
  @Input() trendLabel = '';
  @Input() variant: StatVariant = 'default';
}
