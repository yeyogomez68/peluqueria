import {
  Component, Input, forwardRef, ChangeDetectionStrategy
} from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  ControlValueAccessor, NG_VALUE_ACCESSOR, ReactiveFormsModule
} from '@angular/forms';

@Component({
  selector: 'st-input',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, ReactiveFormsModule],
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => StInputComponent),
    multi: true
  }],
  template: `
    <div class="st-field" [class.st-field--error]="hasError">
      @if (label) {
        <label class="st-label">
          {{ label }}
          @if (required) { <span class="st-label__req">*</span> }
        </label>
      }
      <div class="st-input-wrap" [class.st-input-wrap--prefix]="icon">
        @if (icon) { <i [class]="'pi ' + icon + ' st-input-icon'"></i> }
        <input
          class="st-input"
          [type]="type"
          [placeholder]="placeholder"
          [disabled]="isDisabled"
          [value]="value"
          (input)="onInput($event)"
          (blur)="onTouched()"
        />
      </div>
      @if (hasError && errorMsg) {
        <span class="st-error">{{ errorMsg }}</span>
      }
    </div>
  `,
  styleUrls: ['./st-input.component.scss']
})
export class StInputComponent implements ControlValueAccessor {
  @Input() label       = '';
  @Input() placeholder = '';
  @Input() type        = 'text';
  @Input() icon        = '';
  @Input() required    = false;
  @Input() hasError    = false;
  @Input() errorMsg    = '';

  value = '';
  isDisabled = false;

  onChange: (v: string) => void = () => {};
  onTouched: () => void = () => {};

  onInput(e: Event): void {
    this.value = (e.target as HTMLInputElement).value;
    this.onChange(this.value);
  }

  writeValue(v: string): void { this.value = v ?? ''; }
  registerOnChange(fn: (v: string) => void): void { this.onChange = fn; }
  registerOnTouched(fn: () => void): void { this.onTouched = fn; }
  setDisabledState(isDisabled: boolean): void { this.isDisabled = isDisabled; }
}
