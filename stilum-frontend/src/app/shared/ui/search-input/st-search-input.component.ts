import {
  Component, Input, Output, EventEmitter, ChangeDetectionStrategy,
  ChangeDetectorRef, forwardRef
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

@Component({
  selector: 'st-search-input',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule],
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => StSearchInputComponent),
    multi: true
  }],
  template: `
    <div class="st-search" [class.st-search--has-value]="value">
      <i class="pi pi-search st-search__icon"></i>
      <input
        class="st-search__input"
        type="text"
        [placeholder]="placeholder"
        [value]="value"
        [disabled]="isDisabled"
        (input)="onInput($event)"
        (blur)="onTouched()"
      />
      @if (value) {
        <button
          type="button"
          class="st-search__clear"
          (click)="clear()"
          tabindex="-1"
        >
          <i class="pi pi-times"></i>
        </button>
      }
    </div>
  `,
  styleUrls: ['./st-search-input.component.scss']
})
export class StSearchInputComponent implements ControlValueAccessor {
  @Input() placeholder = 'Buscar...';
  @Input() debounceMs = 300;
  @Output() search = new EventEmitter<string>();

  value = '';
  isDisabled = false;

  private debounceTimer: ReturnType<typeof setTimeout> | null = null;

  onChange: (v: string) => void = () => {};
  onTouched: () => void = () => {};

  constructor(private cdr: ChangeDetectorRef) {}

  onInput(e: Event): void {
    this.value = (e.target as HTMLInputElement).value;

    if (this.debounceTimer !== null) {
      clearTimeout(this.debounceTimer);
    }

    this.debounceTimer = setTimeout(() => {
      this.debounceTimer = null;
      this.onChange(this.value);
      this.search.emit(this.value);
    }, this.debounceMs);
  }

  clear(): void {
    this.value = '';
    if (this.debounceTimer !== null) {
      clearTimeout(this.debounceTimer);
      this.debounceTimer = null;
    }
    this.onChange('');
    this.search.emit('');
    this.cdr.markForCheck();
  }

  writeValue(v: string): void {
    this.value = v ?? '';
    this.cdr.markForCheck();
  }

  registerOnChange(fn: (v: string) => void): void { this.onChange = fn; }
  registerOnTouched(fn: () => void): void { this.onTouched = fn; }
  setDisabledState(isDisabled: boolean): void {
    this.isDisabled = isDisabled;
    this.cdr.markForCheck();
  }
}
