import {
  Component, Input, Output, EventEmitter,
  ChangeDetectionStrategy, HostListener
} from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'st-modal',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule],
  template: `
    @if (visible) {
      <div class="st-modal-overlay" (click)="onOverlayClick($event)">
        <div class="st-modal-box" [style.width]="width" role="dialog" [attr.aria-label]="header">
          @if (header) {
            <div class="st-modal-header">
              <h2 class="st-modal-title">{{ header }}</h2>
              <button class="st-modal-close" type="button" (click)="onClose()">
                <i class="pi pi-times"></i>
              </button>
            </div>
          }
          <div class="st-modal-body">
            <ng-content />
          </div>
        </div>
      </div>
    }
  `,
  styleUrls: ['./st-modal.component.scss']
})
export class StModalComponent {
  @Input() visible = false;
  @Input() header  = '';
  @Input() width   = '520px';
  @Input() closeOnOverlay = true;
  @Output() visibleChange = new EventEmitter<boolean>();
  @Output() closed        = new EventEmitter<void>();

  @HostListener('document:keydown.escape')
  onEscape(): void { if (this.visible) this.onClose(); }

  onClose(): void {
    this.visibleChange.emit(false);
    this.closed.emit();
  }

  onOverlayClick(e: MouseEvent): void {
    if (this.closeOnOverlay && e.target === e.currentTarget) this.onClose();
  }
}
