import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { StTabsComponent, StTab } from '../../shared/ui/tabs/st-tabs.component';
import { StStatCardComponent } from '../../shared/ui/stat-card/st-stat-card.component';
import { StBadgeComponent, BadgeVariant } from '../../shared/ui/badge/st-badge.component';
import { StButtonComponent } from '../../shared/ui/button/st-button.component';
import { MiPortalApiService, MisIngresos } from '../../core/services/mi-portal-api.service';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-profesional-portal',
  standalone: true,
  imports: [CommonModule, FormsModule, StTabsComponent, StStatCardComponent, StBadgeComponent, StButtonComponent],
  template: `
    <div class="st-page">
      <h2 class="st-page__title">Mi Portal</h2>

      <st-tabs [tabs]="tabDefs" [active]="tabActiva" (activeChange)="tabActiva = $event" />

      @if (tabActiva === 'citas') {
        <div class="st-section">
          <div class="st-filter-bar">
            <input type="date" class="st-input" [(ngModel)]="fechaCitasStr" (change)="cargarCitas()" />
          </div>

          <table class="st-table">
            <thead>
              <tr><th>Hora</th><th>Cliente</th><th>Servicio</th><th>Duración</th><th>Estado</th><th class="text-right">Precio</th></tr>
            </thead>
            <tbody>
              @if (loadingCitas) {
                <tr><td colspan="6" class="st-loading-row">Cargando...</td></tr>
              } @else if (citas.length === 0) {
                <tr><td colspan="6" class="st-empty-row">Sin citas para esta fecha</td></tr>
              } @else {
                @for (c of citas; track c.id) {
                  <tr>
                    <td>{{ c.fechaHoraInicio | date:'HH:mm' }}</td>
                    <td>{{ c.cliente?.nombre }}</td>
                    <td>{{ c.servicio?.nombre }}</td>
                    <td>{{ c.duracionMin }} min</td>
                    <td><st-badge [label]="c.estado" [variant]="getBadgeVariant(c.estado)" /></td>
                    <td class="text-right">{{ (c.precioCobrado || c.servicio?.precio) | number:'1.0-0' }} COP</td>
                  </tr>
                }
              }
            </tbody>
          </table>
        </div>
      }

      @if (tabActiva === 'ingresos') {
        <div class="st-section">
          <div class="st-filter-bar">
            <label class="st-label">Desde</label>
            <input type="date" class="st-input" [(ngModel)]="rangoInicioStr" />
            <label class="st-label">Hasta</label>
            <input type="date" class="st-input" [(ngModel)]="rangoFinStr" />
            <st-button label="Consultar" icon="search" (clicked)="cargarIngresos()" />
          </div>

          @if (ingresos) {
            <div class="st-grid-3 mb-lg">
              <st-stat-card label="Citas completadas" icon="calendar-check"
                            [value]="ingresos.totalCitas" />
              <st-stat-card label="Total facturado" icon="dollar"
                            [value]="(ingresos.totalFacturado | number:'1.0-0') + ' COP'"
                            variant="accent" />
              <st-stat-card label="Mi comisión" icon="wallet"
                            [value]="(ingresos.totalComision | number:'1.0-0') + ' COP'"
                            variant="success" />
            </div>

            <table class="st-table">
              <thead>
                <tr><th>Fecha</th><th>Servicio</th><th>Cliente</th><th class="text-right">Precio</th><th class="text-right">Mi comisión</th><th>Pago</th></tr>
              </thead>
              <tbody>
                @for (d of ingresos.detalle; track $index) {
                  <tr>
                    <td>{{ d.fecha | date:'dd/MM/yyyy HH:mm' }}</td>
                    <td>{{ d.servicio }}</td>
                    <td>{{ d.cliente }}</td>
                    <td class="text-right">{{ d.precio | number:'1.0-0' }} COP</td>
                    <td class="text-right color-success font-bold">{{ d.comision | number:'1.0-0' }} COP</td>
                    <td><st-badge [label]="d.metodoPago || 'N/A'" variant="info" /></td>
                  </tr>
                }
              </tbody>
            </table>
          }
        </div>
      }
    </div>
  `,
  styles: [`
    .st-page { padding: 1.5rem; }
    .st-page__title { font-size: var(--text-xl); font-weight: var(--font-semibold); margin: 0 0 1.25rem; }
    .st-section { margin-top: 1.25rem; }
    .st-filter-bar { display: flex; align-items: center; gap: 0.75rem; margin-bottom: 1rem; flex-wrap: wrap; }
    .st-grid-3 { display: grid; grid-template-columns: repeat(3,1fr); gap: 1rem; }
    .mb-lg { margin-bottom: 1.5rem; }
    .st-table { width: 100%; border-collapse: collapse; font-size: var(--text-sm); background: var(--color-bg-surface); border-radius: var(--radius-lg); overflow: hidden; border: 1px solid var(--color-border); }
    .st-table th { padding: 0.6rem 1rem; text-align: left; font-weight: var(--font-semibold); color: var(--color-text-2); font-size: var(--text-xs); text-transform: uppercase; background: var(--color-bg-elevated); }
    .st-table td { padding: 0.75rem 1rem; border-top: 1px solid var(--color-border); }
    .st-loading-row, .st-empty-row { text-align: center; padding: 2rem !important; color: var(--color-text-3); }
    .text-right { text-align: right; }
    .color-success { color: var(--color-success); }
    .font-bold { font-weight: var(--font-semibold); }
    @media (max-width: 768px) { .st-grid-3 { grid-template-columns: 1fr; } }
  `]
})
export class ProfesionalPortalComponent implements OnInit {
  citas: any[] = [];
  ingresos?: MisIngresos;
  tabActiva = 'citas';
  fechaCitasStr = new Date().toISOString().split('T')[0];
  rangoInicioStr = new Date(new Date().setDate(1)).toISOString().split('T')[0];
  rangoFinStr = new Date().toISOString().split('T')[0];
  loadingCitas = false;
  profesionalId = '';

  tabDefs: StTab[] = [
    { label: 'Mis Citas Hoy', value: 'citas', icon: 'calendar' },
    { label: 'Mis Ingresos', value: 'ingresos', icon: 'chart-bar' }
  ];

  constructor(private api: MiPortalApiService, private auth: AuthService) {}

  ngOnInit() {
    const user = this.auth.currentUser();
    this.profesionalId = user?.userId ?? '';
    this.cargarCitas();
  }

  cargarCitas() {
    if (!this.profesionalId) return;
    this.loadingCitas = true;
    this.api.misCitas(this.profesionalId, this.fechaCitasStr).subscribe({
      next: c => { this.citas = c; this.loadingCitas = false; },
      error: () => { this.loadingCitas = false; }
    });
  }

  cargarIngresos() {
    if (!this.profesionalId) return;
    this.api.misIngresos(this.profesionalId, this.rangoInicioStr, this.rangoFinStr)
      .subscribe(i => this.ingresos = i);
  }

  getBadgeVariant(estado: string): BadgeVariant {
    const map: Record<string, BadgeVariant> = {
      PENDIENTE: 'pendiente', CONFIRMADA: 'confirmada', EN_CURSO: 'en_curso',
      COMPLETADA: 'completada', CANCELADA: 'cancelada', NO_SHOW: 'no_show'
    };
    return map[estado] ?? 'default';
  }
}
