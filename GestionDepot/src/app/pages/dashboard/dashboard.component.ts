import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ChartData } from 'chart.js';
import { AllDashboardData, DashboardService, TopProduct, TopSupplier } from '../../services/dashboard.service'; // Importe TopSupplier
import { AlertsListComponent } from '../../components/alerts-list/alerts-list.component';

// Import des composants enfants
import { KpiCardComponent } from '../../components/kpi-card/kpi-card.component';
import { StockChartComponent } from '../../components/stock-chart/stock-chart.component';
import { DataListComponent } from '../../components/data-list/data-list.component';


@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, KpiCardComponent, StockChartComponent, DataListComponent, AlertsListComponent ],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  
  private dashboardService = inject(DashboardService);

  dashboardData: AllDashboardData | null = null;
  isLoading = true;
  error: string | null = null;
  
  stockMovementsChartData: ChartData<'bar'> = { labels: [], datasets: [] };
  
  topProductsForList: { name: string, value: string }[] = [];
  topSuppliersForList: { name: string, value: string }[] = []; // value est une chaîne

  ngOnInit(): void {
    this.loadDashboardData();
  }

  loadDashboardData(): void {
    this.isLoading = true;
    this.error = null;
    this.dashboardService.getAllDashboardData().subscribe({
      next: (data) => {
        this.dashboardData = data;
        
        this.prepareChartData(data);
        this.prepareListData(data); // <-- Cette méthode gère le mapping des fournisseurs
        this.isLoading = false;
      },
      error: (err) => { 
        this.error = 'Erreur de chargement.'; 
        this.isLoading = false;
        console.error("Erreur détaillée du dashboard :", err); 
      }
    });
  }

  private prepareChartData(data: AllDashboardData): void {
    this.stockMovementsChartData = {
      labels: data.stockMovements.map(d => d.month),
      datasets: [
        { 
          data: data.stockMovements.map(d => d.entries), 
          label: 'Entrées', 
          backgroundColor: 'rgba(59, 130, 246, 0.7)',
          borderColor: '#3b82f6',
          borderWidth: 1
        },
        { 
          data: data.stockMovements.map(d => d.exits), 
          label: 'Sorties', 
          backgroundColor: 'rgba(239, 68, 68, 0.7)',
          borderColor: '#ef4444',
          borderWidth: 1
        }
      ]
    };
  }

  private prepareListData(data: AllDashboardData) {
    this.topProductsForList = data.topProducts.map(p => ({
      name: p.name,
      value: `${p.movements} mouvements`
    }));
    
    // MAPPING CORRIGÉ : utilise supplierName et totalAmount de la nouvelle interface TopSupplier
    this.topSuppliersForList = data.topSuppliers.map(s => ({
      name: s.supplierName,  // 'supplierName' est le nom de la propriété du DTO Java
      value: `${s.productsCount} Stocks` // <-- Crée la chaîne pour l'affichage
    }));
  }
}