import { Component, Input } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';

@Component({
  selector: 'app-kpi-card',
  standalone: true,
  imports: [CommonModule, DecimalPipe],
  templateUrl: './kpi-card.component.html',
  styleUrls: ['./kpi-card.component.css']
})
export class KpiCardComponent {
  @Input() title: string = '';
  @Input() value: number | null = 0;
  @Input() iconClass: string = 'fa-solid fa-question-circle';
  @Input() isAlert: boolean = false;
  @Input() isCurrency: boolean = false;

  @Input() change?: number;

  get changeClass() {
    if (this.change === undefined) return '';
    return this.change >= 0 ? 'positive' : 'negative';
  }
}