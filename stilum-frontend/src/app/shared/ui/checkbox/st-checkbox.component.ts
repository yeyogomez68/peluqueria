import { Component, Input, forwardRef, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

@Component({
  selector: 'st-checkbox',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule],
  providers: [{ provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => StCheckboxComponent), multi: true }],
  template: `
    <label class="st-checkbox-label" [class.st-checkbox-label--disabled]="disabled" (click)="toggle()">
      <input
        type="checkbox"
        class="st-checkbox-native"
        [checked]="value"
        [disabled]="disabled"
        (click)="$event.preventDefault()" />
      <span class="st-checkbox__box" [class.st-checkbox__box--checked]="value">
        @if (value) {
          <i class="pi pi-check st-checkbox__icon"></i>
        }
      </span>
      <span class="st-checkbox-content">
        <span class="st-checkbox-text">
          {{ label }}
          @if (required) { <span class="st-label__req">*</span> }
        </span>
        @if (description) {
          <span class="st-checkbox-desc">{{ description }}</span>
        }
      </span>
    </label>
  `,
  styleUrls: ['./st-checkbox.component.scss']
})
export class StCheckboxComponent implements ControlValueAccessor {
  @Input() label = '';
  @Input() description = '';
  @Input() required = false;
  @Input() disabled = false;

  value = false;
  isDisabled = false;

  onChange: (v: boolean) => void = () => {};
  onTouched: () => void = () => {};

  constructor(private cdr: ChangeDetectorRef) {}

  toggle(): void {
    if (this.isDisabled || this.disabled) return;
    this.value = !this.value;
    this.onChange(this.value);
    this.onTouched();
    this.cdr.markForCheck();
  }

  writeValue(v: boolean): void {
    this.value = !!v;
    this.cdr.markForCheck();
  }

  registerOnChange(fn: (v: boolean) => void): void { this.onChange = fn; }
  registerOnTouched(fn: () => void): void { this.onTouched = fn; }
  setDisabledState(d: boolean): void { this.isDisabled = d; this.cdr.markForCheck(); }
}
