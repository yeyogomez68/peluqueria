import { Component, OnInit, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { StButtonComponent } from '../../../shared/ui/button/st-button.component';
import { StInputComponent } from '../../../shared/ui/input/st-input.component';
import { StSelectComponent } from '../../../shared/ui/select/st-select.component';
import { StModalComponent } from '../../../shared/ui/modal/st-modal.component';
import { StBadgeComponent } from '../../../shared/ui/badge/st-badge.component';
import { ToastService } from '../../../shared/ui/toast/toast.service';
import { UserApiService } from './user-api.service';
import { User } from '../../../core/models/user.models';
import { UserRol } from '../../../core/models/auth.models';

@Component({
  selector: 'st-users-list',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule, ReactiveFormsModule, DatePipe,
    StButtonComponent, StInputComponent, StSelectComponent,
    StModalComponent, StBadgeComponent
  ],
  templateUrl: './users-list.component.html',
  styleUrls: ['./users-list.component.scss']
})
export class UsersListComponent implements OnInit {
  private readonly api   = inject(UserApiService);
  private readonly fb    = inject(FormBuilder);
  private readonly toast = inject(ToastService);

  readonly users    = signal<User[]>([]);
  readonly loading  = signal(false);
  readonly saving   = signal(false);
  readonly showForm = signal(false);

  readonly rolOptions: { label: string; value: UserRol }[] = [
    { label: 'Administrador', value: 'ADMIN_TENANT' },
    { label: 'Profesional',   value: 'PROFESIONAL' }
  ];

  readonly form = this.fb.nonNullable.group({
    nombre:   ['', [Validators.required, Validators.maxLength(100)]],
    email:    ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
    rol:      ['PROFESIONAL' as UserRol, Validators.required]
  });

  ngOnInit(): void { this.cargar(); }

  cargar(): void {
    this.loading.set(true);
    this.api.listar().subscribe({
      next: data => { this.users.set(data); this.loading.set(false); },
      error: ()   => this.loading.set(false)
    });
  }

  guardar(): void {
    if (this.form.invalid || this.saving()) return;
    this.saving.set(true);
    this.api.crear(this.form.getRawValue()).subscribe({
      next: () => {
        this.saving.set(false);
        this.showForm.set(false);
        this.toast.add({ severity: 'success', summary: 'Usuario creado' });
        this.cargar();
      },
      error: (err) => {
        this.saving.set(false);
        this.toast.add({ severity: 'error', summary: 'Error', detail: err?.error?.detail ?? 'No se pudo crear el usuario' });
      }
    });
  }

  toggleEstado(user: User): void {
    const call$ = user.activo ? this.api.desactivar(user.id) : this.api.activar(user.id);
    call$.subscribe({
      next: () => { this.toast.add({ severity: 'success', summary: 'Listo' }); this.cargar(); },
      error: ()  => this.toast.add({ severity: 'error', summary: 'Error al cambiar estado' })
    });
  }

  rolLabel(rol: UserRol): string {
    return this.rolOptions.find(r => r.value === rol)?.label ?? rol;
  }
}
