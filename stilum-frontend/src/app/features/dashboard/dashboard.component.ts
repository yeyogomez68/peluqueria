import {
  Component, inject, signal, computed,
  ChangeDetectionStrategy, OnInit, OnDestroy
} from '@angular/core';
import { CommonModule, DatePipe, CurrencyPipe } from '@angular/common';
import { FullCalendarModule } from '@fullcalendar/angular';
import { StButtonComponent } from '../../shared/ui/button/st-button.component';
import { StBadgeComponent, BadgeVariant } from '../../shared/ui/badge/st-badge.component';
import { NuevaCitaModalComponent } from './nueva-cita-modal.component';
import { RegistrarPagoModalComponent } from './registrar-pago-modal.component';
import { CalendarOptions, EventClickArg, DateSelectArg, EventInput } from '@fullcalendar/core';
import resourceTimeGridPlugin from '@fullcalendar/resource-timegrid';
import interactionPlugin from '@fullcalendar/interaction';
import esLocale from '@fullcalendar/core/locales/es';
import { AuthService } from '../../core/auth/auth.service';
import { CitaApiService } from '../../core/services/cita-api.service';
import { ProfesionalApiService } from '../tenant/profesionales/profesional-api.service';
import { Cita, CitaEstado, CITA_ESTADO_LABEL, CITA_ESTADO_SEVERITY } from '../../core/models/cita.models';
import { Profesional } from '../../core/models/profesional.models';
import { Subject, takeUntil } from 'rxjs';

/**
 * DashboardComponent — Vista principal con:
 *  - Cards de resumen del día
 *  - FullCalendar ResourceTimeGrid con profesionales como columnas
 *  - Modal lateral al hacer clic en una cita
 *
 * SK-F-01: Signals para estado local.
 * SK-F-02: Smart component — obtiene datos y coordina hijos.
 */
@Component({
  selector: 'st-dashboard',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, FullCalendarModule, DatePipe, CurrencyPipe, NuevaCitaModalComponent, StButtonComponent, StBadgeComponent, RegistrarPagoModalComponent],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit, OnDestroy {
  readonly auth          = inject(AuthService);
  readonly citaApi       = inject(CitaApiService);
  readonly profesionalApi = inject(ProfesionalApiService);

  private readonly destroy$ = new Subject<void>();

  readonly hoy = new Date();
  readonly fechaFormateada = this.hoy.toLocaleDateString('es-CO', {
    weekday: 'long', year: 'numeric', month: 'long', day: 'numeric'
  });

  // Estado
  readonly fechaActual   = signal<Date>(new Date());
  readonly citas         = signal<Cita[]>([]);
  readonly profesionales = signal<Profesional[]>([]);
  readonly citaSeleccionada    = signal<Cita | null>(null);
  readonly loadingCitas        = signal(false);
  readonly mostrarNuevaCita    = signal(false);
  readonly preSlotProfesional  = signal<string | null>(null);
  readonly preSlotInicio       = signal<string | null>(null);

  pagoModalVisible = false;
  citaSeleccionadaId = '';
  precioSugerido = 0;
  comisionPorcentaje = 30;

  // Stats computadas
  readonly citasHoy    = computed(() => this.citas().length);
  readonly pendientes  = computed(() =>
    this.citas().filter(c => c.estado === 'PENDIENTE' || c.estado === 'CONFIRMADA').length);
  readonly completadas = computed(() =>
    this.citas().filter(c => c.estado === 'COMPLETADA').length);
  readonly ingresos    = computed(() =>
    this.citas()
      .filter(c => c.estado === 'COMPLETADA')
      .reduce((sum, c) => sum + (c.precioCobrado ?? c.servicio.precio), 0));

  // Próximas citas (pendientes y confirmadas, ordenadas)
  readonly citasProximas = computed(() =>
    this.citas()
      .filter(c => c.estado === 'PENDIENTE' || c.estado === 'CONFIRMADA' || c.estado === 'EN_CURSO')
      .sort((a, b) => a.fechaHoraInicio.localeCompare(b.fechaHoraInicio))
      .slice(0, 6)
  );

  // Opciones del calendario
  readonly calendarOptions = computed<CalendarOptions>(() => ({
    plugins: [resourceTimeGridPlugin, interactionPlugin],
    initialView: 'resourceTimeGridDay',
    locale: esLocale,
    headerToolbar: false, // navegación propia
    initialDate: this.fechaActual(),
    slotMinTime: '07:00:00',
    slotMaxTime: '22:00:00',
    slotDuration: '00:30:00',
    allDaySlot: false,
    nowIndicator: true,
    selectable: true,
    selectMirror: true,
    height: 'auto',
    expandRows: true,
    resources: this.profesionales().map(p => ({
      id: p.id,
      title: p.nombre,
      eventColor: p.colorAgenda ?? 'var(--color-brand-gold)'
    })),
    events: this.citasToEvents(this.citas()),
    eventClick: (info: EventClickArg) => this.onEventClick(info),
    select: (info: DateSelectArg) => this.onSlotSelect(info),
    eventContent: (arg: any) => {
      return {
        html: `<div class="fc-event-inner">
          <span class="fc-event-title">${arg.event.title}</span>
          <span class="fc-event-sub">${arg.event.extendedProps['servicio']}</span>
        </div>`
      };
    }
  }));

  readonly estadoLabel    = CITA_ESTADO_LABEL;
  readonly estadoSeverity = CITA_ESTADO_SEVERITY;

  ngOnInit(): void {
    this.cargarProfesionales();
    this.cargarCitas();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private cargarProfesionales(): void {
    this.profesionalApi.listarActivos()
      .pipe(takeUntil(this.destroy$))
      .subscribe(data => this.profesionales.set(data));
  }

  cargarCitas(): void {
    this.loadingCitas.set(true);
    const fecha = this.fechaActual().toISOString().slice(0, 10);
    this.citaApi.listarPorFecha(fecha)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: data => { this.citas.set(data); this.loadingCitas.set(false); },
        error: ()   => this.loadingCitas.set(false)
      });
  }

  irDiaAnterior(): void {
    const d = new Date(this.fechaActual());
    d.setDate(d.getDate() - 1);
    this.fechaActual.set(d);
    this.cargarCitas();
  }

  irDiaSiguiente(): void {
    const d = new Date(this.fechaActual());
    d.setDate(d.getDate() + 1);
    this.fechaActual.set(d);
    this.cargarCitas();
  }

  irHoy(): void {
    this.fechaActual.set(new Date());
    this.cargarCitas();
  }

  private onEventClick(info: EventClickArg): void {
    const citaId = info.event.id;
    const found = this.citas().find(c => c.id === citaId) ?? null;
    this.citaSeleccionada.set(found);
  }

  abrirNuevaCita(): void {
    this.preSlotProfesional.set(null);
    this.preSlotInicio.set(null);
    this.mostrarNuevaCita.set(true);
  }

  // Se emite al seleccionar un slot vacío en el calendario
  onSlotSelect(info: DateSelectArg): void {
    this.preSlotProfesional.set(info.resource?.id ?? null);
    this.preSlotInicio.set(info.startStr);
    this.mostrarNuevaCita.set(true);
  }

  onCitaCreada(cita: Cita): void {
    this.cargarCitas();
  }

  abrirModalPago(cita: any) {
    this.citaSeleccionadaId = cita.id;
    this.precioSugerido = cita.servicio?.precio ?? 0;
    this.comisionPorcentaje = cita.profesional?.comisionPorcentaje ?? 30;
    this.pagoModalVisible = true;
  }

  onPagoRegistrado(cita: any) {
    this.cargarCitas();
  }

  cerrarDetalle(): void {
    this.citaSeleccionada.set(null);
  }

  private citasToEvents(citas: Cita[]): EventInput[] {
    return citas.map(c => ({
      id:    c.id,
      title: c.cliente.nombre,
      start: c.fechaHoraInicio,
      end:   c.fechaHoraFin,
      resourceId: c.profesional.id,
      backgroundColor: c.profesional.colorAgenda ?? undefined,
      borderColor:     c.profesional.colorAgenda ?? undefined,
      classNames: [`fc-cita--${c.estado.toLowerCase()}`],
      extendedProps: {
        estado:    c.estado,
        servicio:  c.servicio.nombre,
        profesional: c.profesional.nombre
      }
    }));
  }

  get nombreUsuario(): string {
    return this.auth.currentUser()?.nombre ?? '';
  }

  get fechaDiaActual(): string {
    return this.fechaActual().toLocaleDateString('es-CO', {
      weekday: 'long', day: 'numeric', month: 'long'
    });
  }

  get esHoy(): boolean {
    const hoy = new Date();
    const actual = this.fechaActual();
    return hoy.toDateString() === actual.toDateString();
  }

  estadoVariant(estado: string): BadgeVariant {
    return estado.toLowerCase() as BadgeVariant;
  }
}
