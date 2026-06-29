import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { StButtonComponent } from '../../shared/ui/button/st-button.component';
import { StStatCardComponent } from '../../shared/ui/stat-card/st-stat-card.component';
import { StBadgeComponent } from '../../shared/ui/badge/st-badge.component';
import { ContabilidadApiService, ResumenDia } from '../../core/services/contabilidad-api.service';

@Component({
  selector: 'app-contabilidad',
  standalone: true,
  imports: [CommonModule, FormsModule, StButtonComponent, StStatCardComponent, StBadgeComponent],
  template: `
    <div class="st-page">
      <div class="st-page__header">
        <h2 class="st-page__title">Cierre de Caja</h2>
        <div class="st-page__actions">
          <input type="date" class="st-input" [(ngModel)]="fechaStr" (change)="cargar()" />
          <st-button label="Imprimir" icon="print" variant="ghost" (clicked)="imprimir()" />
        </div>
      </div>

      @if (resumen) {
        <div class="st-grid-3 mb-lg">
          <st-stat-card label="Total Vendido" icon="dollar"
                        [value]="(resumen.totalVendido | number:'1.0-0') + ' COP'"
                        variant="accent" />
          <st-stat-card label="Para el Negocio" icon="chart-bar"
                        [value]="(resumen.totalNegocio | number:'1.0-0') + ' COP'"
                        variant="success" />
          <st-stat-card label="Total Comisiones" icon="users"
                        [value]="(resumen.totalComisiones | number:'1.0-0') + ' COP'"
                        variant="warning" />
        </div>

        <div class="st-grid-2">
          <div class="st-card">
            <div class="st-card__header"><h3 class="st-card__title">Por Profesional</h3></div>
            <div class="st-card__body">
              <table class="st-table">
                <thead>
                  <tr>
                    <th>Profesional</th>
                    <th class="text-center">Citas</th>
                    <th class="text-right">Vendido</th>
                    <th class="text-right">Comisión</th>
                    <th class="text-right">Neto negocio</th>
                  </tr>
                </thead>
                <tbody>
                  @for (row of resumen.porProfesional; track row.nombre) {
                    <tr>
                      <td>{{ row.nombre }}</td>
                      <td class="text-center">{{ row.citas }}</td>
                      <td class="text-right">{{ row.totalVendido | number:'1.0-0' }} COP</td>
                      <td class="text-right color-warning">
                        {{ row.comision | number:'1.0-0' }} COP
                        <small class="color-text-2">({{ row.comisionPorcentaje }}%)</small>
                      </td>
                      <td class="text-right color-success">{{ (row.totalVendido - row.comision) | number:'1.0-0' }} COP</td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
          </div>

          <div class="st-card">
            <div class="st-card__header"><h3 class="st-card__title">Por Método de Pago</h3></div>
            <div class="st-card__body">
              <table class="st-table">
                <thead><tr><th>Método</th><th class="text-center">Citas</th><th class="text-right">Total</th></tr></thead>
                <tbody>
                  @for (row of resumen.porMetodoPago; track row.metodo) {
                    <tr>
                      <td><st-badge [label]="row.metodo" variant="info" /></td>
                      <td class="text-center">{{ row.cantidad }}</td>
                      <td class="text-right">{{ row.total | number:'1.0-0' }} COP</td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
          </div>
        </div>
      } @else {
        <div class="st-empty-state">
          <i class="pi pi-calendar-times st-empty-state__icon"></i>
          <p>Sin datos para esta fecha</p>
        </div>
      }
    </div>
  `,
  styles: [`
    .st-page { padding: 1.5rem; }
    .st-page__header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 1.5rem; flex-wrap: wrap; gap: 0.75rem; }
    .st-page__title { font-size: var(--text-xl); font-weight: var(--font-semibold); margin: 0; }
    .st-page__actions { display: flex; align-items: center; gap: 0.75rem; }
    .st-grid-3 { display: grid; grid-template-columns: repeat(3,1fr); gap: 1rem; }
    .st-grid-2 { display: grid; grid-template-columns: repeat(2,1fr); gap: 1rem; }
    .mb-lg { margin-bottom: 1.5rem; }
    .st-card { background: var(--color-bg-surface); border: 1px solid var(--color-border); border-radius: var(--radius-lg); overflow: hidden; }
    .st-card__header { padding: 1rem 1.25rem; border-bottom: 1px solid var(--color-border); }
    .st-card__title { margin: 0; font-size: var(--text-sm); font-weight: var(--font-semibold); text-transform: uppercase; letter-spacing: var(--tracking-wide); color: var(--color-text-2); }
    .st-card__body { padding: 0; }
    .st-table { width: 100%; border-collapse: collapse; font-size: var(--text-sm); }
    .st-table th { padding: 0.6rem 1rem; text-align: left; font-weight: var(--font-semibold); color: var(--color-text-2); font-size: var(--text-xs); text-transform: uppercase; background: var(--color-bg-elevated); }
    .st-table td { padding: 0.75rem 1rem; border-top: 1px solid var(--color-border); }
    .text-center { text-align: center; }
    .text-right { text-align: right; }
    .color-warning { color: var(--color-warning); }
    .color-success { color: var(--color-success); }
    .color-text-2 { color: var(--color-text-2); }
    .st-empty-state { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 4rem; color: var(--color-text-3); gap: 0.75rem; }
    .st-empty-state__icon { font-size: 2.5rem; opacity: 0.4; }
    @media (max-width: 768px) { .st-grid-3, .st-grid-2 { grid-template-columns: 1fr; } }
  `]
})
export class ContabilidadComponent implements OnInit {
  resumen?: ResumenDia;
  fechaStr = new Date().toISOString().split('T')[0];

  constructor(private api: ContabilidadApiService) {}

  ngOnInit() { this.cargar(); }

  cargar() {
    this.api.resumenDia(this.fechaStr).subscribe(r => this.resumen = r);
  }

  imprimir() { window.print(); }
}
