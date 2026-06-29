import {
  Component, Input, Output, EventEmitter,
  ChangeDetectionStrategy, HostListener
} from '@angular/core';
import { CommonModule } from '@angular/common';

export type ConfirmVariant = 'danger' | 'warning' | 'info';

@Component({
  selector: 'st-confirm-dialog',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule],
  template: `
    @if (visible) {
      <div class="st-confirm-overlay" (click)="onOverlayClick($event)">
        <div class="st-confirm-dialog" role="alertdialog" [attr.aria-label]="title">
          <div class="st-confirm-icon" [class]="'st-confirm-icon--' + variant">
            <i [class]="'pi ' + iconClass"></i>
          </div>
          <h2 class="st-confirm-title">{{ title }}</h2>
          @if (message) {
            <p class="st-confirm-message">{{ message }}</p>
          }
          <div class="st-confirm-actions">
            <button
              type="button"
              class="st-confirm-btn st-confirm-btn--cancel"
              (click)="cancel()"
            >
              {{ cancelLabel }}
            </button>
            <button
              type="button"
              class="st-confirm-btn"
              [class]="'st-confirm-btn--' + variant"
              [disabled]="loading"
              (click)="confirm()"
            >
              @if (loading) {
                <span class="st-confirm-spinner"></span>
              } @else {
                {{ confirmLabel }}
              }
            </button>
          </div>
        </div>
      </div>
    }
  `,
  styleUrls: ['./st-confirm-dialog.component.scss']
})
export class StConfirmDialogComponent {
  @Input() visible = false;
  @Input() title = '¿Confirmar acción?';
  @Input() message = '';
  @Input() confirmLabel = 'Confirmar';
  @Input() cancelLabel = 'Cancelar';
  @Input() variant: ConfirmVariant = 'danger';
  @Input() loading = false;
  @Output() visibleChange = new EventEmitter<boolean>();
  @Output() confirmed = new EventEmitter<void>();
  @Output() cancelled = new EventEmitter<void>();

  get iconClass(): string {
    return this.variant === 'info' ? 'pi-info-circle' : 'pi-exclamation-triangle';
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    if (this.visible) this.cancel();
  }

  cancel(): void {
    this.visibleChange.emit(false);
    this.cancelled.emit();
  }

  confirm(): void {
    this.confirmed.emit();
  }

  onOverlayClick(e: MouseEvent): void {
    if (e.target === e.currentTarget) this.cancel();
  }
}
