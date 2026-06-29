import { Component, Input, Output, EventEmitter, ChangeDetectionStrategy } from '@angular/core';

export type AlertSeverity = 'success' | 'error' | 'warning' | 'info';

const ALERT_ICONS: Record<AlertSeverity, string> = {
  success: 'pi-check-circle',
  error:   'pi-times-circle',
  warning: 'pi-exclamation-triangle',
  info:    'pi-info-circle',
};

@Component({
  selector: 'st-alert',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (!closed) {
      <div [class]="'st-alert st-alert--' + severity">
        <i [class]="'pi ' + icon + ' st-alert__icon'"></i>
        <div class="st-alert__body">
          @if (title) { <span class="st-alert__title">{{ title }}</span> }
          <span class="st-alert__message">{{ message }}</span>
        </div>
        @if (dismissible) {
          <button class="st-alert__dismiss" type="button" (click)="dismiss()">
            <i class="pi pi-times"></i>
          </button>
        }
      </div>
    }
  `,
  styleUrls: ['./st-alert.component.scss']
})
export class StAlertComponent {
  @Input() severity: AlertSeverity = 'info';
  @Input() title = '';
  @Input() message = '';
  @Input() dismissible = false;
  @Output() dismissed = new EventEmitter<void>();

  closed = false;

  get icon(): string { return ALERT_ICONS[this.severity]; }

  dismiss(): void { this.closed = true; this.dismissed.emit(); }
}
