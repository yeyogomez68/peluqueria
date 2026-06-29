import {
  Component, Input, Output, EventEmitter,
  ChangeDetectionStrategy
} from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'st-paginator',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule],
  template: `
    <span class="st-pag__info">
      Mostrando {{ startRecord }}–{{ endRecord }} de {{ total }} registros
    </span>
    <div class="st-pag__controls">
      <button
        type="button"
        class="st-pag__btn st-pag__btn--nav"
        [class.st-pag__btn--disabled]="page === 1"
        [attr.disabled]="page === 1 ? true : null"
        (click)="goToPage(page - 1)"
      >
        <i class="pi pi-chevron-left"></i>
      </button>

      @for (p of visiblePages; track $index) {
        @if (p === null) {
          <span class="st-pag__ellipsis">…</span>
        } @else {
          <button
            type="button"
            class="st-pag__btn"
            [class.st-pag__btn--active]="p === page"
            (click)="goToPage(p)"
          >
            {{ p }}
          </button>
        }
      }

      <button
        type="button"
        class="st-pag__btn st-pag__btn--nav"
        [class.st-pag__btn--disabled]="page === totalPages"
        [attr.disabled]="page === totalPages ? true : null"
        (click)="goToPage(page + 1)"
      >
        <i class="pi pi-chevron-right"></i>
      </button>
    </div>
  `,
  styleUrls: ['./st-paginator.component.scss']
})
export class StPaginatorComponent {
  @Input() total = 0;
  @Input() page = 1;
  @Input() pageSize = 10;
  @Output() pageChange = new EventEmitter<number>();

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.total / this.pageSize));
  }

  get startRecord(): number {
    return this.total === 0 ? 0 : (this.page - 1) * this.pageSize + 1;
  }

  get endRecord(): number {
    return Math.min(this.page * this.pageSize, this.total);
  }

  get visiblePages(): (number | null)[] {
    const total = this.totalPages;
    const current = this.page;

    if (total <= 7) {
      return Array.from({ length: total }, (_, i) => i + 1);
    }

    const pages: (number | null)[] = [];

    const showLeftEllipsis = current > 3;
    const showRightEllipsis = current < total - 2;

    pages.push(1);

    if (showLeftEllipsis) {
      pages.push(null);
    }

    const rangeStart = showLeftEllipsis ? Math.max(2, current - 1) : 2;
    const rangeEnd = showRightEllipsis ? Math.min(total - 1, current + 1) : total - 1;

    for (let i = rangeStart; i <= rangeEnd; i++) {
      pages.push(i);
    }

    if (showRightEllipsis) {
      pages.push(null);
    }

    pages.push(total);

    return pages;
  }

  goToPage(p: number): void {
    if (p < 1 || p > this.totalPages) return;
    this.pageChange.emit(p);
  }
}
