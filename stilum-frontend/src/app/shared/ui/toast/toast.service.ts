import { Injectable, signal } from '@angular/core';

export interface ToastMessage {
  id: number;
  severity: 'success' | 'error' | 'warning' | 'info';
  summary: string;
  detail?: string;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  readonly messages = signal<ToastMessage[]>([]);
  private nextId = 0;

  add(msg: Omit<ToastMessage, 'id'>): void {
    const id = this.nextId++;
    this.messages.update(msgs => [...msgs, { ...msg, id }]);
    setTimeout(() => this.remove(id), 4000);
  }

  remove(id: number): void {
    this.messages.update(msgs => msgs.filter(m => m.id !== id));
  }
}
