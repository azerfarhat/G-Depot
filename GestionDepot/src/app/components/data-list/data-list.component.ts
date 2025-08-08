import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common'; // Assurez-vous que CurrencyPipe est bien importé

@Component({
  selector: 'app-data-list',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="card list-container">
      <h2>{{ title }}</h2>
      <ul>
        <li *ngIf="items.length === 0">Aucune donnée à afficher.</li>
        <li *ngFor="let item of items">
          <span>{{ item.name }}</span>
          
          <span class="muted">{{ item.value }}</span> 
        </li>
      </ul>
    </div>
  `,
  styles: [
    `.card { background: white; border-radius: 12px; padding: 1.5rem; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); } 
    h2 { font-size: 1.25rem; font-weight: 600; margin-bottom: 1rem; } 
    ul { list-style: none; padding: 0; } 
    li { display: flex; justify-content: space-between; padding: 0.75rem 0; border-bottom: 1px solid #e5e7eb; } 
    li:last-child { border: none; } 
    .muted { color: #6b7280; font-weight: 500; }`
  ]
})
export class DataListComponent {
  @Input() title: string = '';
  @Input() items: { name: string, value: string | number }[] = [];

}