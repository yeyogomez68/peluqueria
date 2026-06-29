import {
  Component, OnInit, inject, signal, computed,
  ChangeDetectionStrategy, Input, Output, EventEmitter, OnChanges, SimpleChanges
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { StModalComponent } from '../../shared/ui/modal/st-modal.component';
import { StButtonComponent } from '../../shared/ui/button/st-button.component';
import { StInputComponent } from '../../shared/ui/input/st-input.component';
import { StSelectComponent } from '../../shared/ui/select/st-select.component';
import { ToastService } from '../../shared/ui/toast/toast.service';
import { CitaApiService } from '../../core/services/cita-api.service';
import { ProfesionalApiService } from '../tenant/profesionales/profesional-api.service';
import { ServicioApiService } from '../tenant/servicios/servicio-api.service';
import { Profesional } from '../../core/models/profesional.models';
import { Servicio } from '../../core/models/servicio.models';
import { Cita, SlotDisponible } from '../../core/models/cita.models';

/**
 * NuevaCitaModalComponent — modal de alta de cita.
 *
 * Flujo:
 *  1. Seleccionar profesional
 *  2. Seleccionar servicio → deduce duracionMin
 *  3. Seleccionar slot de disponibilidad (GET /api/citas/disponibilidad)
 *  4. Ingresar datos del cliente (teléfono + nombre)
 *  5. Confirmar → POST /api/citas
 *
 * Los inputs profesionalId, servicioId y fechaHoraInicio permiten pre-cargarlo
 * desde el clic en un slot del calendario ResourceTimeGrid.
 */
@Component({
  selector: 'st-nueva-cita-modal',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule, ReactiveFormsModule,
    StModalComponent, StButtonComponent, StInputComponent, StSelectComponent
  ],
  templateUrl: './nueva-cita-modal.component.html',
  styleUrls: ['./nueva-cita-modal.component.scss']
})
export class NuevaCitaModalComponent implements OnInit, OnChanges {
  private readonly citaApi       = inject(CitaApiService);
  private readonly profesionalApi = inject(ProfesionalApiService);
  private readonly servicioApi   = inject(ServicioApiService);
  private readonly fb            = inject(FormBuilder);
  private readonly toast         = inject(ToastService);

  @Input() visible = false;
  @Input() preProfesionalId: string | null = null;
  @Input() preFechaHoraInicio: string | null = null;
  @Output() visibleChange = new EventEmitter<boolean>();
  @Output() citaCreada    = new EventEmitter<Cita>();

  readonly profesionales   = signal<Profesional[]>([]);
  readonly servicios       = signal<Servicio[]>([]);
  readonly slots           = signal<SlotDisponible[]>([]);
  readonly loadingSlots    = signal(false);
  readonly saving          = signal(false);

  readonly form = this.fb.nonNullable.group({
    profesionalId:    ['', Validators.required],
    servicioId:       ['', Validators.required],
    slot:             ['', Validators.required],    // ISO string del inicio del slot
    clienteTelefono:  ['', [Validators.required, Validators.maxLength(20)]],
    clienteNombre:    ['', Validators.maxLength(100)],
    notas:            ['']
  });

  // Labels para los selects
  readonly profesionalOptions = computed(() =>
    this.profesionales().map(p => ({ label: p.nombre, value: p.id }))
  );

  readonly servicioOptions = computed(() =>
    this.servicios().map(s => ({
      label: `${s.nombre} (${s.duracionMin} min — ${s.precio.toLocaleString('es-CO')} COP)`,
      value: s.id
    }))
  );

  readonly slotOptions = computed(() =>
    this.slots().map(sl => ({
      label: `${new Date(sl.inicio).toLocaleTimeString('es-CO', { hour: '2-digit', minute: '2-digit' })} → ${new Date(sl.fin).toLocaleTimeString('es-CO', { hour: '2-digit', minute: '2-digit' })}`,
      value: sl.inicio
    }))
  );

  ngOnInit(): void {
    this.cargarProfesionales();
    this.cargarServicios();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['visible']?.currentValue === true) {
      this.form.reset();
      this.slots.set([]);
      // Pre-cargar desde el clic en calendario
      if (this.preProfesionalId) {
        this.form.patchValue({ profesionalId: this.preProfesionalId });
      }
      if (this.preFechaHoraInicio) {
        this.form.patchValue({ slot: this.preFechaHoraInicio });
      }
      // Si hay profesional y fecha pre-cargados, consultar disponibilidad
      if (this.preProfesionalId && this.preFechaHoraInicio) {
        this.buscarDisponibilidad();
      }
    }
  }

  private cargarProfesionales(): void {
    this.profesionalApi.listarActivos().subscribe(data => this.profesionales.set(data));
  }

  private cargarServicios(): void {
    this.servicioApi.listarActivos().subscribe(data => this.servicios.set(data));
  }

  onProfesionalChange(): void {
    this.form.patchValue({ slot: '' });
    this.slots.set([]);
    this.buscarDisponibilidadSiPosible();
  }

  onServicioChange(): void {
    this.form.patchValue({ slot: '' });
    this.slots.set([]);
    this.buscarDisponibilidadSiPosible();
  }

  private buscarDisponibilidadSiPosible(): void {
    const profesionalId = this.form.value.profesionalId;
    const servicioId    = this.form.value.servicioId;
    if (!profesionalId || !servicioId) return;
    this.buscarDisponibilidad();
  }

  private buscarDisponibilidad(): void {
    const profesionalId = this.form.value.profesionalId ?? this.preProfesionalId;
    const servicioId    = this.form.value.servicioId;
    if (!profesionalId) return;

    const servicio = this.servicios().find(s => s.id === servicioId);
    const duracion = servicio?.duracionMin ?? 30;

    const fechaBase = this.preFechaHoraInicio
        ? new Date(this.preFechaHoraInicio).toISOString().slice(0, 10)
        : new Date().toISOString().slice(0, 10);

    this.loadingSlots.set(true);
    this.citaApi.disponibilidad(profesionalId, fechaBase, duracion).subscribe({
      next: data => { this.slots.set(data); this.loadingSlots.set(false); },
      error: ()   => this.loadingSlots.set(false)
    });
  }

  guardar(): void {
    if (this.form.invalid || this.saving()) return;
    this.saving.set(true);
    const raw = this.form.getRawValue();

    this.citaApi.crear({
      profesionalId:   raw.profesionalId,
      servicioId:      raw.servicioId,
      clienteTelefono: raw.clienteTelefono,
      clienteNombre:   raw.clienteNombre  || undefined,
      fechaHoraInicio: raw.slot,
      notas:           raw.notas          || undefined
    }).subscribe({
      next: cita => {
        this.saving.set(false);
        this.toast.add({ severity: 'success', summary: 'Cita agendada' });
        this.citaCreada.emit(cita);
        this.cerrar();
      },
      error: (err) => {
        this.saving.set(false);
        this.toast.add({
          severity: 'error', summary: 'Error al agendar',
          detail: err?.error?.detail ?? 'No se pudo crear la cita'
        });
      }
    });
  }

  cerrar(): void {
    this.visibleChange.emit(false);
  }
}
