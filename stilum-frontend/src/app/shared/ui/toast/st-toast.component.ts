import { Component, inject, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService } from './toast.service';

@Component({
  selector: 'st-toast',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule],
  template: `
    <div class="st-toast-container">
      @for (msg of toast.messages(); track msg.id) {
        <div class="st-toast st-toast--{{ msg.severity }}" (click)="toast.remove(msg.id)">
          <i [class]="'pi ' + iconFor(msg.severity) + ' st-toast-icon'"></i>
          <div class="st-toast-content">
            <span class="st-toast-summary">{{ msg.summary }}</span>
            @if (msg.detail) {
              <span class="st-toast-detail">{{ msg.detail }}</span>
            }
          </div>
          <button class="st-toast-close" type="button" (click)="toast.remove(msg.id)">
            <i class="pi pi-times"></i>
          </button>
        </div>
      }
    </div>
  `,
  styles: [`
    .st-toast-container {
      position: fixed;
      bottom: 1.5rem;
      right: 1.5rem;
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
      z-index: 9999;
      pointer-events: none;
    }

    .st-toast {
      display: flex;
      align-items: flex-start;
      gap: 0.75rem;
      padding: 0.875rem 1rem;
      border-radius: var(--radius-lg);
      border: 1px solid var(--color-border);
      background: var(--color-bg-card);
      box-shadow: var(--shadow-lg);
      cursor: pointer;
      min-width: 280px;
      max-width: 380px;
      animation: toastIn 0.25s var(--easing-spring);
      pointer-events: all;
    }

    .st-toast--success { border-left: 3px solid var(--color-success); }
    .st-toast--success .st-toast-icon { color: var(--color-success); }

    .st-toast--error { border-left: 3px solid var(--color-danger); }
    .st-toast--error .st-toast-icon { color: var(--color-danger); }

    .st-toast--warning { border-left: 3px solid var(--color-warning); }
    .st-toast--warning .st-toast-icon { color: var(--color-warning); }

    .st-toast--info { border-left: 3px solid var(--color-info); }
    .st-toast--info .st-toast-icon { color: var(--color-info); }

    .st-toast-content {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 2px;
    }

    .st-toast-summary {
      font-weight: var(--font-semibold);
      font-size: var(--text-sm);
      color: var(--color-text-1);
    }

    .st-toast-detail {
      font-size: var(--text-xs);
      color: var(--color-text-2);
    }

    .st-toast-close {
      background: none;
      border: none;
      cursor: pointer;
      color: var(--color-text-3);
      padding: 2px;
      display: flex;
      align-items: center;
      flex-shrink: 0;
    }

    @keyframes toastIn {
      from { opacity: 0; transform: translateX(16px); }
      to   { opacity: 1; transform: translateX(0); }
    }
  `]
})
export class StToastComponent {
  readonly toast = inject(ToastService);

  iconFor(severity: string): string {
    const map: Record<string, string> = {
      success: 'pi-check-circle',
      error:   'pi-times-circle',
      warning: 'pi-exclamation-triangle',
      info:    'pi-info-circle'
    };
    return map[severity] ?? 'pi-info-circle';
  }
}
