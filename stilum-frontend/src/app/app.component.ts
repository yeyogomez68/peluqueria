import { Component, OnInit, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ThemeService } from './core/theme/theme.service';

@Component({
  selector: 'st-root',
  standalone: true,
  imports: [RouterOutlet],
  template: `<router-outlet />`
})
export class AppComponent implements OnInit {
  private readonly themeService = inject(ThemeService);

  ngOnInit(): void {
    // ThemeService se inicializa vía inject; el effect() en su constructor
    // aplica el tema guardado tan pronto como el componente raíz arranca.
    // No se necesita lógica adicional aquí.
    void this.themeService;
  }
}
