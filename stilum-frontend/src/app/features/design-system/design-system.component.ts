import { Component, signal, ChangeDetectionStrategy, inject } from '@angular/core';
import { ReactiveFormsModule, FormBuilder } from '@angular/forms';
import {
  StButtonComponent,
  StInputComponent,
  StTextareaComponent,
  StCheckboxComponent,
  StToggleComponent,
  StSelectComponent,
  StSearchInputComponent,
  StBadgeComponent,
  StChipComponent,
  StAlertComponent,
  StSpinnerComponent,
  StSkeletonComponent,
  StAvatarComponent,
  StStatCardComponent,
  StProgressBarComponent,
  StTabsComponent,
  StPaginatorComponent,
  StModalComponent,
  StConfirmDialogComponent,
  StEmptyStateComponent,
  ToastService,
  type StTab,
  type BadgeVariant,
} from '../../shared/ui';

@Component({
  selector: 'app-design-system',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ReactiveFormsModule,
    StButtonComponent, StInputComponent, StTextareaComponent,
    StCheckboxComponent, StToggleComponent, StSelectComponent,
    StSearchInputComponent, StBadgeComponent, StChipComponent,
    StAlertComponent, StSpinnerComponent, StSkeletonComponent,
    StAvatarComponent, StStatCardComponent, StProgressBarComponent,
    StTabsComponent, StPaginatorComponent, StModalComponent,
    StConfirmDialogComponent, StEmptyStateComponent,
  ],
  templateUrl: './design-system.component.html',
  styleUrls: ['./design-system.component.scss'],
})
export class DesignSystemComponent {
  private readonly fb = inject(FormBuilder);
  private readonly toast = inject(ToastService);

  readonly Math = Math;

  // ── Navegación principal ──────────────────────────────────────
  readonly sections: StTab[] = [
    { value: 'foundation', label: 'Fundamentos',  icon: 'palette'      },
    { value: 'buttons',    label: 'Botones',       icon: 'send'         },
    { value: 'forms',      label: 'Formularios',   icon: 'pen-to-square'},
    { value: 'feedback',   label: 'Feedback',      icon: 'bell'         },
    { value: 'data',       label: 'Datos',         icon: 'chart-bar'    },
    { value: 'navigation', label: 'Navegación',    icon: 'bars'         },
    { value: 'overlays',   label: 'Overlays',      icon: 'window-maximize'},
  ];
  readonly activeSection = signal('foundation');

  // ── Formularios de ejemplo ────────────────────────────────────
  readonly demoForm = this.fb.group({
    email:    [''],
    password: [''],
    notas:    [''],
    rol:      [''],
    activo:   [true],
    notifs:   [false],
    search:   [''],
  });

  readonly rolOptions = [
    { label: 'Administrador', value: 'ADMIN' },
    { label: 'Profesional',   value: 'PROF'  },
    { label: 'Recepcionista', value: 'RECEP' },
  ];

  // ── Estado de overlays ────────────────────────────────────────
  readonly showModal   = signal(false);
  readonly showConfirm = signal(false);
  readonly confirmLoad = signal(false);

  // ── Tabs internas ─────────────────────────────────────────────
  readonly innerTabs: StTab[] = [
    { value: 'a', label: 'General',  icon: 'user'  },
    { value: 'b', label: 'Horarios', icon: 'clock' },
    { value: 'c', label: 'Notas',    icon: 'file', badge: 3 },
  ];
  readonly innerActive = signal('a');

  readonly pillTabs: StTab[] = [
    { value: 'day',   label: 'Día'    },
    { value: 'week',  label: 'Semana' },
    { value: 'month', label: 'Mes'    },
  ];
  readonly pillActive = signal('day');

  // ── Paginador ─────────────────────────────────────────────────
  readonly page    = signal(1);
  readonly pageSize = 10;
  readonly total    = 87;

  // ── Progress dinámico ─────────────────────────────────────────
  readonly progress = signal(65);

  // ── Badge variants ────────────────────────────────────────────
  readonly badgeVariants: BadgeVariant[] = [
    'default','success','warning','danger','info',
    'pendiente','confirmada','en_curso','completada','cancelada','no_show',
  ];

  // ── Toast demo ────────────────────────────────────────────────
  fireToast(sev: 'success'|'error'|'warning'|'info'): void {
    const map = {
      success: { summary: 'Guardado', detail: 'Los cambios se aplicaron.' },
      error:   { summary: 'Error',    detail: 'No se pudo completar la acción.' },
      warning: { summary: 'Atención', detail: 'Verifica los datos antes de continuar.' },
      info:    { summary: 'Info',     detail: 'Recuerda guardar tus cambios.' },
    };
    this.toast.add({ severity: sev, ...map[sev] });
  }

  // ── Confirm demo ──────────────────────────────────────────────
  onConfirm(): void {
    this.confirmLoad.set(true);
    setTimeout(() => {
      this.confirmLoad.set(false);
      this.showConfirm.set(false);
      this.toast.add({ severity: 'success', summary: 'Eliminado', detail: 'El elemento fue eliminado.' });
    }, 1500);
  }
}
