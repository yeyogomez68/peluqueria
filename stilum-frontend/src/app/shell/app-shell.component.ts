import { Component, inject, signal, computed, ChangeDetectionStrategy } from '@angular/core';
import { Router, RouterOutlet, RouterLink, RouterLinkActive, NavigationEnd } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../core/auth/auth.service';
import { ThemeService } from '../core/theme/theme.service';
import { StToastComponent } from '../shared/ui/toast/st-toast.component';
import { isDemoMode, disableDemo } from '../core/demo/demo.interceptor';
import { filter } from 'rxjs';

interface NavItem { label: string; icon: string; route: string; }

@Component({
  selector: 'st-app-shell',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive, StToastComponent],
  templateUrl: './app-shell.component.html',
  styleUrls: ['./app-shell.component.scss']
})
export class AppShellComponent {
  readonly auth         = inject(AuthService);
  readonly themeService = inject(ThemeService);
  private  readonly router = inject(Router);

  readonly collapsed    = signal(false);
  readonly userMenuOpen = signal(false);
  readonly mobileOpen   = signal(false);

  readonly user   = computed(() => this.auth.currentUser());
  readonly isDark = computed(() => this.themeService.isDark);

  readonly navItems = computed<NavItem[]>(() => {
    const rol = this.user()?.rol;
    if (rol === 'SUPER_ADMIN') {
      return [
        { label: 'Tenants', icon: 'pi-building', route: '/admin/tenants' }
      ];
    }
    const base: NavItem[] = [
      { label: 'Dashboard',     icon: 'pi-calendar', route: '/dashboard' },
      { label: 'Profesionales', icon: 'pi-users',    route: '/profesionales' },
      { label: 'Servicios',     icon: 'pi-list',     route: '/servicios' }
    ];
    if (rol === 'ADMIN_TENANT') {
      base.push({ label: 'Usuarios', icon: 'pi-user', route: '/usuarios' });
    }
    return base;
  });

  readonly isDemo = isDemoMode;

  constructor() {
    // Cierra el drawer móvil en cada navegación
    this.router.events
      .pipe(filter(e => e instanceof NavigationEnd))
      .subscribe(() => this.mobileOpen.set(false));
  }

  toggleSidebar(): void { this.collapsed.update(v => !v); }
  toggleMobile(): void  { this.mobileOpen.update(v => !v); }
  closeMobile(): void   { this.mobileOpen.set(false); }
  toggleTheme(): void   { this.themeService.toggle(); }
  logout(): void        { this.userMenuOpen.set(false); this.auth.logout(); }
  exitDemo(): void      { disableDemo(); this.auth.logout(); }

  get userInitials(): string {
    return (this.user()?.nombre ?? '')
      .split(' ').slice(0, 2).map(n => n[0]).join('').toUpperCase();
  }
}
