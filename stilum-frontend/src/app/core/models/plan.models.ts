export interface Plan {
  id: string;
  nombre: string;
  precioMensual: number;
  maxProfesionales: number;
  maxCitasMes: number;
  whatsappHabilitado: boolean;
  recordatoriosHabilitados: boolean;
  reportesAvanzados: boolean;
  activo: boolean;
}
