import { Component, OnInit, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { StButtonComponent } from '../../../shared/ui/button/st-button.component';
import { StInputComponent } from '../../../shared/ui/input/st-input.component';
import { StSelectComponent } from '../../../shared/ui/select/st-select.component';
import { StModalComponent } from '../../../shared/ui/modal/st-modal.component';
import { StBadgeComponent } from '../../../shared/ui/badge/st-badge.component';
import { ToastService } from '../../../shared/ui/toast/toast.service';
import { TenantApiService } from './tenant-api.service';
import { TenantSummary } from '../../../core/models/tenant.models';
import { Plan } from '../../../core/models/plan.models';

/**
 * TenantsListComponent — gestión de tenants para SUPER_ADMIN.
 * SK-F-02: Smart component.
 * SK-F-01: Signals para estado local.
 */
@Component({
  selector: 'st-tenants-list',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule, ReactiveFormsModule,
    StButtonComponent, StInputComponent, StSelectComponent, StModalComponent, StBadgeComponent
  ],
  templateUrl: './tenants-list.component.html',
  styleUrls: ['./tenants-list.component.scss']
})
export class TenantsListComponent implements OnInit {
  private readonly api     = inject(TenantApiService);
  private readonly fb      = inject(FormBuilder);
  private readonly toast   = inject(ToastService);

  readonly tenants  = signal<TenantSummary[]>([]);
  readonly planes   = signal<Plan[]>([]);
  readonly loading  = signal(false);
  readonly saving   = signal(false);
  readonly showForm = signal(false);

  readonly form = this.fb.nonNullable.group({
    nombreNegocio:    ['', [Validators.required, Validators.maxLength(100)]],
    emailContacto:    ['', [Validators.required, Validators.email]],
    telefonoContacto: [''],
    ciudad:           [''],
    pais:             ['Colombia'],
    planId:           ['', Validators.required],
    adminNombre:      ['', Validators.required],
    adminPassword:    ['', [Validators.required, Validators.minLength(8)]]
  });

  ngOnInit(): void {
    this.cargar();
    this.cargarPlanes();
  }

  cargar(): void {
    this.loading.set(true);
    this.api.listar().subscribe({
      next: data => { this.tenants.set(data); this.loading.set(false); },
      error: ()   => { this.loading.set(false); }
    });
  }

  cargarPlanes(): void {
    this.api.listarPlanes().subscribe({
      next: data => this.planes.set(data)
    });
  }

  abrirFormulario(): void {
    this.form.reset({ pais: 'Colombia' });
    this.showForm.set(true);
  }

  guardar(): void {
    if (this.form.invalid || this.saving()) return;
    this.saving.set(true);

    this.api.crear(this.form.getRawValue()).subscribe({
      next: () => {
        this.saving.set(false);
        this.showForm.set(false);
        this.toast.add({ severity: 'success', summary: 'Tenant creado', detail: 'El negocio fue registrado correctamente' });
        this.cargar();
      },
      error: (err) => {
        this.saving.set(false);
        this.toast.add({ severity: 'error', summary: 'Error', detail: err?.error?.detail ?? 'No se pudo crear el tenant' });
      }
    });
  }

  toggleEstado(tenant: TenantSummary): void {
    const accion = tenant.activo ? 'inactivar' : 'activar';
    const call$ = tenant.activo ? this.api.inactivar(tenant.id) : this.api.activar(tenant.id);
    call$.subscribe({
      next: () => {
        this.toast.add({ severity: 'success', summary: 'Listo', detail: `Tenant ${accion}do correctamente` });
        this.cargar();
      },
      error: () => this.toast.add({ severity: 'error', summary: 'Error', detail: 'No se pudo realizar la acción' })
    });
  }

  planOptions() {
    return this.planes().map(p => ({ label: `${p.nombre} — $${p.precioMensual.toLocaleString('es-CO')}/mes`, value: p.id }));
  }
}
