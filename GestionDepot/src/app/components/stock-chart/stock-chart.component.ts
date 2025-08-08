import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BaseChartDirective } from 'ng2-charts';
// Correction : Importer ChartData pour un typage plus strict
import { ChartData, ChartOptions } from 'chart.js';

@Component({
  selector: 'app-stock-chart',
  standalone: true,
  imports: [CommonModule, BaseChartDirective],
  templateUrl: './stock-chart.component.html',
  styleUrls: ['./stock-chart.component.css']
})
export class StockChartComponent {
  // CORRECTION 1 : Le type est maintenant ChartData<'bar'>
  @Input() chartData: ChartData<'bar'> = {
    labels: [],
    datasets: []
  };

  // CORRECTION 2 : Le type est maintenant la chaîne littérale 'bar'
  public barChartType: 'bar' = 'bar';
  
  public barChartOptions: ChartOptions<'bar'> = {
    responsive: true,
    maintainAspectRatio: false,
    scales: {
      x: {
        grid: {
          display: false // Cache les lignes de la grille verticale
        }
      },
      y: {
        beginAtZero: true,
        grid: {
          color: '#e9ecef' // Couleur discrète pour la grille horizontale
        }
      }
    },
    plugins: {
      legend: {
        display: true,
        position: 'top',
      },
    },
    interaction: {
        intersect: false,
        mode: 'index',
    },
  };
}