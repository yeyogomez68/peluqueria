import {
  Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { StModalComponent } from '../../shared/ui/modal/st-modal.component';
import { StButtonComponent } from '../../shared/ui/button/st-button.component';
import { StInputComponent } from '../../shared/ui/input/st-input.component';
import { StSelectComponent, SelectOption } from '../../shared/ui/select/st-select.component';
import { CitaApiService } from '../../core/services/cita-api.service';

@Component({
  selector: 'app-registrar-pago-modal',
  standalone: true,
  imports: [CommonModule, FormsModule, StModalComponent, StButtonComponent, StInputComponent, StSelectComponent],
  template: `
    <st-modal header="Registrar Pago" [visible]="visible" (visibleChange)="cancelar()" width="420px">
      <div class="st-form-body">
        <st-input label="Precio cobrado (COP)" type="number" [ngModel]="precioCobrado"
                  (ngModelChange)="precioCobrado = +$event" placeholder="0" />

        <st-select label="Método de pago" [options]="metodos" [(ngModel)]="metodoPago"
                   placeholder="Seleccionar método" />

        @if (precioCobrado > 0 && comisionPorcentaje > 0) {
          <div class="st-info-box">
            <span class="st-info-box__label">Comisión profesional ({{ comisionPorcentaje }}%)</span>
            <span class="st-info-box__value">{{ (precioCobrado * comisionPorcentaje / 100) | number:'1.0-0' }} COP</span>
          </div>
        }
      </div>

      <div class="st-modal-footer">
        <st-button label="Cancelar" variant="ghost" (clicked)="cancelar()" />
        <st-button label="Registrar pago" icon="check" [loading]="loading"
                   [disabled]="!precioCobrado || !metodoPago" (clicked)="confirmar()" />
      </div>
    </st-modal>
  `,
  styles: [`
    .st-form-body { display: flex; flex-direction: column; gap: 1rem; padding-bottom: 0.5rem; }
    .st-info-box {
      display: flex; justify-content: space-between; align-items: center;
      padding: 0.75rem 1rem; border-radius: var(--radius-md);
      background: var(--color-bg-elevated); border: 1px solid var(--color-border);
    }
    .st-info-box__label { color: var(--color-text-2); font-size: var(--text-sm); }
    .st-info-box__value { font-weight: var(--font-semibold); color: var(--color-primary); }
    .st-modal-footer { display: flex; justify-content: flex-end; gap: 0.5rem; padding-top: 1rem; border-top: 1px solid var(--color-border); }
  `]
})
export class RegistrarPagoModalComponent implements OnChanges {
  @Input() citaId!: string;
  @Input() precioSugerido = 0;
  @Input() comisionPorcentaje = 30;
  @Input() visible = false;
  @Output() visibleChange = new EventEmitter<boolean>();
  @Output() pagado = new EventEmitter<any>();

  precioCobrado = 0;
  metodoPago = '';
  loading = false;

  metodos: SelectOption[] = [
    { label: 'Efectivo', value: 'EFECTIVO' },
    { label: 'Nequi', value: 'NEQUI' },
    { label: 'Daviplata', value: 'DAVIPLATA' },
    { label: 'Tarjeta', value: 'TARJETA' },
    { label: 'Transferencia', value: 'TRANSFERENCIA' }
  ];

  constructor(private citaApi: CitaApiService) {}

  ngOnChanges(changes: SimpleChanges) {
    if (changes['precioSugerido']) this.precioCobrado = this.precioSugerido;
  }

  confirmar() {
    this.loading = true;
    this.citaApi.registrarPago(this.citaId, {
      precioCobrado: this.precioCobrado,
      metodoPago: this.metodoPago
    }).subscribe({
      next: (cita: any) => { this.pagado.emit(cita); this.cerrar(); },
      error: () => { this.loading = false; }
    });
  }

  cancelar() { this.cerrar(); }

  private cerrar() {
    this.loading = false;
    this.visibleChange.emit(false);
  }
}
