import { Component, Input, forwardRef, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ControlValueAccessor, NG_VALUE_ACCESSOR, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'st-textarea',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, ReactiveFormsModule],
  providers: [{ provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => StTextareaComponent), multi: true }],
  template: `
    <div class="st-field" [class.st-field--error]="hasError">
      @if (label) {
        <label class="st-label">
          {{ label }}
          @if (required) { <span class="st-label__req">*</span> }
        </label>
      }
      <textarea
        class="st-textarea"
        [placeholder]="placeholder"
        [rows]="rows"
        [attr.maxlength]="maxLength ?? null"
        [disabled]="isDisabled"
        (input)="onInput($event)"
        (blur)="onTouched()">{{ value }}</textarea>
      <div class="st-textarea-footer">
        @if (hasError && errorMsg) {
          <span class="st-error">{{ errorMsg }}</span>
        } @else {
          <span></span>
        }
        @if (maxLength !== undefined) {
          <span class="st-counter" [class.st-counter--warn]="value.length >= maxLength * 0.9">
            {{ value.length }} / {{ maxLength }}
          </span>
        }
      </div>
    </div>
  `,
  styleUrls: ['./st-textarea.component.scss']
})
export class StTextareaComponent implements ControlValueAccessor {
  @Input() label = '';
  @Input() placeholder = '';
  @Input() rows = 4;
  @Input() required = false;
  @Input() hasError = false;
  @Input() errorMsg = '';
  @Input() maxLength?: number;

  value = '';
  isDisabled = false;

  onChange: (v: string) => void = () => {};
  onTouched: () => void = () => {};

  constructor(private cdr: ChangeDetectorRef) {}

  onInput(e: Event): void {
    this.value = (e.target as HTMLTextAreaElement).value;
    this.onChange(this.value);
    this.cdr.markForCheck();
  }

  writeValue(v: string): void {
    this.value = v ?? '';
    this.cdr.markForCheck();
  }

  registerOnChange(fn: (v: string) => void): void { this.onChange = fn; }
  registerOnTouched(fn: () => void): void { this.onTouched = fn; }
  setDisabledState(d: boolean): void { this.isDisabled = d; this.cdr.markForCheck(); }
}
