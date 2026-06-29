import { Component, Input, forwardRef, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

@Component({
  selector: 'st-toggle',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule],
  providers: [{ provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => StToggleComponent), multi: true }],
  template: `
    <label
      class="st-toggle-label"
      [class.st-toggle-label--disabled]="disabled || isDisabled"
      [class.st-toggle-label--left]="labelPosition === 'left'"
      (click)="toggle()">

      @if (labelPosition === 'left' && label) {
        <span class="st-toggle-content">
          <span class="st-toggle-text">{{ label }}</span>
          @if (description) {
            <span class="st-toggle-desc">{{ description }}</span>
          }
        </span>
      }

      <input
        type="checkbox"
        class="st-toggle-native"
        [checked]="value"
        [disabled]="disabled || isDisabled"
        (click)="$event.preventDefault()" />

      <span
        class="st-toggle__pill"
        [class.st-toggle__pill--on]="value"
        [class.st-toggle__pill--sm]="size === 'sm'">
        <span class="st-toggle__thumb" [class.st-toggle__thumb--sm]="size === 'sm'"></span>
      </span>

      @if (labelPosition === 'right' && label) {
        <span class="st-toggle-content">
          <span class="st-toggle-text">{{ label }}</span>
          @if (description) {
            <span class="st-toggle-desc">{{ description }}</span>
          }
        </span>
      }
    </label>
  `,
  styleUrls: ['./st-toggle.component.scss']
})
export class StToggleComponent implements ControlValueAccessor {
  @Input() label = '';
  @Input() description = '';
  @Input() labelPosition: 'left' | 'right' = 'right';
  @Input() disabled = false;
  @Input() size: 'sm' | 'md' = 'md';

  value = false;
  isDisabled = false;

  onChange: (v: boolean) => void = () => {};
  onTouched: () => void = () => {};

  constructor(private cdr: ChangeDetectorRef) {}

  toggle(): void {
    if (this.disabled || this.isDisabled) return;
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
