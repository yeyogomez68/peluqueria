import { Component, OnInit, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { StButtonComponent } from '../../../shared/ui/button/st-button.component';
import { StInputComponent } from '../../../shared/ui/input/st-input.component';
import { StModalComponent } from '../../../shared/ui/modal/st-modal.component';
import { StBadgeComponent } from '../../../shared/ui/badge/st-badge.component';
import { ToastService } from '../../../shared/ui/toast/toast.service';
import { ServicioApiService } from './servicio-api.service';
import { Servicio } from '../../../core/models/servicio.models';

/**
 * ServiciosListComponent — catálogo de servicios con create/edit inline.
 * SK-F-01: Signals. SK-F-02: Smart component.
 */
@Component({
  selector: 'st-servicios-list',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule, CurrencyPipe, ReactiveFormsModule,
    StButtonComponent, StInputComponent, StModalComponent, StBadgeComponent
  ],
  templateUrl: './servicios-list.component.html',
  styleUrls: ['./servicios-list.component.scss']
})
export class ServiciosListComponent implements OnInit {
  private readonly api   = inject(ServicioApiService);
  private readonly fb    = inject(FormBuilder);
  private readonly toast = inject(ToastService);

  readonly servicios   = signal<Servicio[]>([]);
  readonly loading     = signal(false);
  readonly saving      = signal(false);
  readonly showForm    = signal(false);
  readonly editTarget  = signal<Servicio | null>(null);

  readonly form = this.fb.nonNullable.group({
    nombre:      ['', [Validators.required, Validators.maxLength(100)]],
    descripcion: [''],
    duracionMin: [30, [Validators.required, Validators.min(1)]],
    precio:      [0, [Validators.required, Validators.min(0)]]
  });

  ngOnInit(): void { this.cargar(); }

  cargar(): void {
    this.loading.set(true);
    this.api.listar().subscribe({
      next: data => { this.servicios.set(data); this.loading.set(false); },
      error: ()   => this.loading.set(false)
    });
  }

  abrirCrear(): void {
    this.editTarget.set(null);
    this.form.reset({ duracionMin: 30, precio: 0 });
    this.showForm.set(true);
  }

  abrirEditar(s: Servicio): void {
    this.editTarget.set(s);
    this.form.patchValue({
      nombre:      s.nombre,
      descripcion: s.descripcion ?? '',
      duracionMin: s.duracionMin,
      precio:      s.precio
    });
    this.showForm.set(true);
  }

  guardar(): void {
    if (this.form.invalid || this.saving()) return;
    this.saving.set(true);
    const raw = this.form.getRawValue();
    const payload = {
      nombre:      raw.nombre,
      descripcion: raw.descripcion || undefined,
      duracionMin: raw.duracionMin,
      precio:      raw.precio
    };

    const target = this.editTarget();
    const call$ = target
        ? this.api.actualizar(target.id, payload)
        : this.api.crear(payload);

    call$.subscribe({
      next: () => {
        this.saving.set(false);
        this.showForm.set(false);
        this.toast.add({ severity: 'success', summary: target ? 'Servicio actualizado' : 'Servicio creado' });
        this.cargar();
      },
      error: (err) => {
        this.saving.set(false);
        this.toast.add({
          severity: 'error', summary: 'Error',
          detail: err?.error?.detail ?? 'No se pudo guardar el servicio'
        });
      }
    });
  }

  toggleEstado(s: Servicio): void {
    const call$ = s.activo ? this.api.desactivar(s.id) : this.api.activar(s.id);
    call$.subscribe({
      next: () => {
        this.toast.add({ severity: 'success', summary: 'Estado actualizado' });
        this.cargar();
      },
      error: (err) => this.toast.add({
        severity: 'error', summary: 'Error',
        detail: err?.error?.detail ?? 'No se pudo cambiar el estado'
      })
    });
  }

  get dialogHeader(): string {
    return this.editTarget() ? 'Editar Servicio' : 'Nuevo Servicio';
  }
}
