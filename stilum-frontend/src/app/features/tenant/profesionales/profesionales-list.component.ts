import { Component, OnInit, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators, FormArray, FormGroup } from '@angular/forms';
import { StButtonComponent } from '../../../shared/ui/button/st-button.component';
import { StInputComponent } from '../../../shared/ui/input/st-input.component';
import { StSelectComponent } from '../../../shared/ui/select/st-select.component';
import { StModalComponent } from '../../../shared/ui/modal/st-modal.component';
import { StBadgeComponent } from '../../../shared/ui/badge/st-badge.component';
import { ToastService } from '../../../shared/ui/toast/toast.service';
import { ProfesionalApiService } from './profesional-api.service';
import { Profesional, DIA_SEMANA_LABEL, HorarioRequest } from '../../../core/models/profesional.models';

@Component({
  selector: 'st-profesionales-list',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule, ReactiveFormsModule,
    StButtonComponent, StInputComponent, StSelectComponent, StModalComponent, StBadgeComponent
  ],
  templateUrl: './profesionales-list.component.html',
  styleUrls: ['./profesionales-list.component.scss']
})
export class ProfesionalesListComponent implements OnInit {
  private readonly api   = inject(ProfesionalApiService);
  private readonly fb    = inject(FormBuilder);
  private readonly toast = inject(ToastService);

  readonly profesionales    = signal<Profesional[]>([]);
  readonly loading          = signal(false);
  readonly saving           = signal(false);
  readonly showForm         = signal(false);
  readonly profesionalEditando = signal<Profesional | null>(null);

  readonly diasSemana = [1, 2, 3, 4, 5, 6, 7].map(d => ({
    label: DIA_SEMANA_LABEL[d], value: d
  }));

  readonly form = this.fb.nonNullable.group({
    nombre:       ['', [Validators.required, Validators.maxLength(100)]],
    email:        ['', [Validators.required, Validators.email, Validators.maxLength(150)]],
    password:     ['', [Validators.minLength(6), Validators.maxLength(100)]],
    especialidad: [''],
    bio:          [''],
    colorAgenda:  ['#6366f1', [Validators.pattern(/^#[0-9A-Fa-f]{6}$/)]],
    horarios:     this.fb.array<FormGroup>([])
  });

  // Rango rápido de días
  readonly rangoForm = this.fb.nonNullable.group({
    diaDesde:   [1],
    diaHasta:   [5],
    horaInicio: ['09:00'],
    horaFin:    ['18:00']
  });

  get horariosArray(): FormArray { return this.form.controls.horarios as FormArray; }
  get modoEdicion(): boolean { return this.profesionalEditando() !== null; }

  ngOnInit(): void { this.cargar(); }

  cargar(): void {
    this.loading.set(true);
    this.api.listar().subscribe({
      next: data => { this.profesionales.set(data); this.loading.set(false); },
      error: ()   => this.loading.set(false)
    });
  }

  abrirFormulario(): void {
    this.profesionalEditando.set(null);
    this.form.reset({ colorAgenda: '#6366f1' });
    this.form.controls.email.enable();
    this.form.controls.password.setValidators([Validators.required, Validators.minLength(6)]);
    this.form.controls.password.updateValueAndValidity();
    this.horariosArray.clear();
    this.agregarHorario();
    this.showForm.set(true);
  }

  abrirEdicion(p: Profesional): void {
    this.profesionalEditando.set(p);
    this.form.patchValue({
      nombre:       p.nombre,
      email:        p.id, // placeholder, campo deshabilitado en edición
      password:     '',
      especialidad: p.especialidad ?? '',
      bio:          p.bio ?? '',
      colorAgenda:  p.colorAgenda ?? '#6366f1'
    });
    // Email no editable (usuario ya creado), password opcional en edición
    this.form.controls.email.disable();
    this.form.controls.password.clearValidators();
    this.form.controls.password.updateValueAndValidity();

    this.horariosArray.clear();
    (p.horarios ?? []).filter(h => h.activo).forEach(h => {
      this.horariosArray.push(this.fb.nonNullable.group({
        diaSemana:  [h.diaSemana, Validators.required],
        horaInicio: [h.horaInicio, Validators.required],
        horaFin:    [h.horaFin, Validators.required]
      }));
    });
    if (this.horariosArray.length === 0) this.agregarHorario();
    this.showForm.set(true);
  }

  agregarHorario(): void {
    this.horariosArray.push(this.fb.nonNullable.group({
      diaSemana:  [1, Validators.required],
      horaInicio: ['09:00', Validators.required],
      horaFin:    ['18:00', Validators.required]
    }));
  }

  aplicarRango(): void {
    const r = this.rangoForm.getRawValue();
    const desde = Math.min(r.diaDesde, r.diaHasta);
    const hasta = Math.max(r.diaDesde, r.diaHasta);
    for (let dia = desde; dia <= hasta; dia++) {
      const yaExiste = this.horariosArray.controls.some(
        ctrl => ctrl.get('diaSemana')?.value === dia
      );
      if (!yaExiste) {
        this.horariosArray.push(this.fb.nonNullable.group({
          diaSemana:  [dia, Validators.required],
          horaInicio: [r.horaInicio, Validators.required],
          horaFin:    [r.horaFin, Validators.required]
        }));
      }
    }
  }

  eliminarHorario(i: number): void { this.horariosArray.removeAt(i); }

  guardar(): void {
    if (this.form.invalid || this.saving()) return;
    this.saving.set(true);
    const raw = this.form.getRawValue();
    const horarios = raw.horarios as HorarioRequest[];

    const editando = this.profesionalEditando();
    const call$ = editando
      ? this.api.actualizar(editando.id, {
          nombre:       raw.nombre,
          especialidad: raw.especialidad || undefined,
          bio:          raw.bio          || undefined,
          colorAgenda:  raw.colorAgenda  || undefined,
          horarios
        })
      : this.api.crear({
          nombre:       raw.nombre,
          email:        raw.email,
          password:     raw.password,
          especialidad: raw.especialidad || undefined,
          bio:          raw.bio          || undefined,
          colorAgenda:  raw.colorAgenda  || undefined,
          horarios
        });

    call$.subscribe({
      next: () => {
        this.saving.set(false);
        this.showForm.set(false);
        this.toast.add({ severity: 'success', summary: editando ? 'Profesional actualizado' : 'Profesional creado' });
        this.cargar();
      },
      error: (err) => {
        this.saving.set(false);
        this.toast.add({
          severity: 'error', summary: 'Error',
          detail: err?.error?.detail ?? 'No se pudo guardar el profesional'
        });
      }
    });
  }

  toggleEstado(p: Profesional): void {
    const call$ = p.activo ? this.api.desactivar(p.id) : this.api.activar(p.id);
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

  horariosResumen(p: Profesional): string {
    if (!p.horarios?.length) return '—';
    return p.horarios
      .filter(h => h.activo)
      .map(h => `${DIA_SEMANA_LABEL[h.diaSemana].slice(0, 3)} ${h.horaInicio}–${h.horaFin}`)
      .join(', ');
  }

  diaLabel(d: number): string { return DIA_SEMANA_LABEL[d] ?? `Día ${d}`; }
}
