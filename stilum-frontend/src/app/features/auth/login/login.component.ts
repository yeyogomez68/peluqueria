import { Component, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/auth/auth.service';
import { StButtonComponent } from '../../../shared/ui/button/st-button.component';
import { StInputComponent } from '../../../shared/ui/input/st-input.component';
import { enableDemo, disableDemo, isDemoMode } from '../../../core/demo/demo.interceptor';

@Component({
  selector: 'st-login',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, ReactiveFormsModule, StButtonComponent, StInputComponent],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  private readonly fb     = inject(FormBuilder);
  private readonly auth   = inject(AuthService);
  private readonly router = inject(Router);

  readonly loading     = signal(false);
  readonly demoLoading = signal(false);
  readonly errorMsg    = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    email:    ['', [Validators.required, Validators.email]],
    password: ['', Validators.required]
  });

  onSubmit(): void {
    if (this.form.invalid || this.loading()) return;
    disableDemo();
    this.errorMsg.set(null);
    this.loading.set(true);
    this.auth.login(this.form.getRawValue()).subscribe({
      next: () => { this.loading.set(false); this.router.navigate([this.auth.getPostLoginRoute()]); },
      error: (err: any) => {
        this.loading.set(false);
        this.errorMsg.set(err?.error?.detail ?? 'Credenciales inválidas. Intenta de nuevo.');
      }
    });
  }

  enterDemo(): void {
    this.demoLoading.set(true);
    enableDemo();
    this.auth.login({ email: 'demo@stilum.com', password: 'demo' }).subscribe({
      next: () => { this.demoLoading.set(false); this.router.navigate(['/dashboard']); },
      error: () => { this.demoLoading.set(false); }
    });
  }
}
