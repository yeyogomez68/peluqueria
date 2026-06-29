/**
 * design-tokens.ts — Acceso programático a los tokens CSS en runtime.
 * SK-F-05: getter en vez de objeto estático → siempre refleja el tema activo.
 * RN-DESIGN-006: nunca usar strings hex hardcodeados en TypeScript.
 */

function cssVar(name: string): string {
  if (typeof getComputedStyle === 'undefined') return '';
  return getComputedStyle(document.documentElement).getPropertyValue(name).trim();
}

export const DesignTokens = {
  get brand() {
    return {
      gold:      cssVar('--color-brand-gold'),
      goldLight: cssVar('--color-brand-gold-light'),
      goldDark:  cssVar('--color-brand-gold-dark'),
      dark:      cssVar('--color-brand-dark'),
    };
  },

  get text() {
    return {
      primary:  cssVar('--color-text-1'),
      muted:    cssVar('--color-text-2'),
      onGold:   cssVar('--color-text-on-gold'),
      inverse:  cssVar('--color-text-inverse'),
    };
  },

  get bg() {
    return {
      page:     cssVar('--color-bg-page'),
      surface:  cssVar('--color-bg-surface'),
      card:     cssVar('--color-bg-card'),
      elevated: cssVar('--color-bg-elevated'),
    };
  },

  /** Tokens para colorear eventos del calendario según estado de cita. */
  get cita() {
    return {
      pendiente:  { bg: cssVar('--cita-pendiente-bg'),  text: cssVar('--cita-pendiente-text') },
      confirmada: { bg: cssVar('--cita-confirmada-bg'), text: cssVar('--cita-confirmada-text') },
      enCurso:    { bg: cssVar('--cita-en-curso-bg'),   text: cssVar('--cita-en-curso-text') },
      completada: { bg: cssVar('--cita-completada-bg'), text: cssVar('--cita-completada-text') },
      cancelada:  { bg: cssVar('--cita-cancelada-bg'),  text: cssVar('--cita-cancelada-text') },
      noShow:     { bg: cssVar('--cita-no-show-bg'),    text: cssVar('--cita-no-show-text') },
    };
  },
};
