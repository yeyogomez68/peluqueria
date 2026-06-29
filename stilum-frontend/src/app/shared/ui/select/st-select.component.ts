import {
  Component, Input, Output, EventEmitter, forwardRef,
  ChangeDetectionStrategy, ChangeDetectorRef
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

export interface SelectOption { label: string; value: string | number; }

@Component({
  selector: 'st-select',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule],
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => StSelectComponent),
    multi: true
  }],
  template: `
    <div class="st-field">
      @if (label) {
        <label class="st-label">
          {{ label }}
          @if (required) { <span class="st-label__req">*</span> }
        </label>
      }
      <div class="st-select-wrap">
        <select
          class="st-select"
          [disabled]="isDisabled"
          (change)="onSelect($event)"
        >
          @if (placeholder) {
            <option value="" [selected]="!value" disabled>{{ placeholder }}</option>
          }
          @for (opt of options; track opt.value) {
            <option [value]="opt.value" [selected]="opt.value == value">
              {{ opt.label }}
            </option>
          }
        </select>
        <i class="pi pi-chevron-down st-select-icon"></i>
      </div>
    </div>
  `,
  styleUrls: ['./st-select.component.scss']
})
export class StSelectComponent implements ControlValueAccessor {
  @Input() label       = '';
  @Input() placeholder = 'Seleccionar...';
  @Input() options: SelectOption[] = [];
  @Input() required    = false;
  @Output() selectionChange = new EventEmitter<string | number>();

  value: string | number = '';
  isDisabled = false;

  onChange: (v: string | number) => void = () => {};
  onTouched: () => void = () => {};

  constructor(private cdr: ChangeDetectorRef) {}

  onSelect(e: Event): void {
    const raw = (e.target as HTMLSelectElement).value;
    // Preserve number type if the matching option is a number
    const matched = this.options.find(o => String(o.value) === raw);
    this.value = matched ? matched.value : raw;
    this.onChange(this.value);
    this.selectionChange.emit(this.value);
    this.onTouched();
  }

  writeValue(v: string | number): void {
    this.value = v ?? '';
    this.cdr.markForCheck();
  }
  registerOnChange(fn: (v: string | number) => void): void { this.onChange = fn; }
  registerOnTouched(fn: () => void): void { this.onTouched = fn; }
  setDisabledState(d: boolean): void { this.isDisabled = d; this.cdr.markForCheck(); }
}
