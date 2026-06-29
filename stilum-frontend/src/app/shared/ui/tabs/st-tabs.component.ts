import {
  Component, Input, Output, EventEmitter,
  ChangeDetectionStrategy
} from '@angular/core';
import { CommonModule } from '@angular/common';

export interface StTab {
  label: string;
  value: string;
  icon?: string;
  badge?: number;
  disabled?: boolean;
}

@Component({
  selector: 'st-tabs',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule],
  template: `
    <div [class]="containerClass">
      @for (tab of tabs; track tab.value) {
        <button
          type="button"
          [class]="getTabClass(tab)"
          [attr.aria-selected]="tab.value === active"
          [attr.disabled]="tab.disabled ? true : null"
          (click)="select(tab)"
        >
          @if (tab.icon) {
            <i [class]="'pi pi-' + tab.icon"></i>
          }
          <span>{{ tab.label }}</span>
          @if (tab.badge && tab.badge > 0) {
            <span class="st-tab__badge">{{ tab.badge }}</span>
          }
        </button>
      }
    </div>
  `,
  styleUrls: ['./st-tabs.component.scss']
})
export class StTabsComponent {
  @Input() tabs: StTab[] = [];
  @Input() active = '';
  @Output() activeChange = new EventEmitter<string>();
  @Input() variant: 'line' | 'pills' = 'line';

  get containerClass(): string {
    return `st-tabs st-tabs--${this.variant}`;
  }

  getTabClass(tab: StTab): string {
    return [
      'st-tab',
      `st-tab--${this.variant}`,
      tab.value === this.active ? 'st-tab--active' : '',
      tab.disabled ? 'st-tab--disabled' : '',
      tab.icon ? 'st-tab--has-icon' : ''
    ].filter(Boolean).join(' ');
  }

  select(tab: StTab): void {
    if (tab.disabled) return;
    this.activeChange.emit(tab.value);
  }
}
