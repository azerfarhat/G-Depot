import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, forkJoin } from 'rxjs';
import { AuthService } from './auth.service';

// --- Interfaces pour typer les données (le "contrat" avec l'API) ---
export interface DashboardStats {
  totalProducts: number;
  monthlyEntries: number;
  monthlyExits: number;
  activeAlerts: number;
  monthlyRevenue: number;
  totalOrders: number;
  totalUsers: number;
  changeInEntries?: number;
  changeInExits?: number;
  changeInRevenue?: number;
}

export interface StockMovement {
  month: string;
  entries: number;
  exits: number;
}


// L'interface TopProduct est correcte
export interface TopProduct {
  name: string;
  movements: number;
}

// INTERFACE TopSupplier CORRIGÉE ET SIMPLIFIÉE
export interface TopSupplier {
  supplierName: string; // Nom de la propriété du DTO Java
  productsCount: number; // <-- Reçoit le nombre
}

export interface Alert {
  productName: string;
  message: string;
  type: 'ALERTE' | 'RUPTURE' | 'EXPIRE';
 }



// Interface pour la réponse combinée de forkJoin
export interface AllDashboardData {
  stats: DashboardStats;
  stockMovements: StockMovement[];
  topProducts: TopProduct[];
  topSuppliers: TopSupplier[]; // <-- Utilise la nouvelle interface TopSupplier
  recentAlerts: Alert[];
}

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private http = inject(HttpClient);
  private authService = inject(AuthService);
  private apiUrl = 'http://localhost:9090/api/dashboard'; // URL de base du contrôleur de dashboard

  private getAuthHeaders(): HttpHeaders {
    const token = this.authService.getToken(); 
    if (!token) {
        this.authService.logout();
        throw new Error('Aucun token trouvé ! Déconnexion.');
    }
    return new HttpHeaders({ 'Authorization': `Bearer ${token}` });
  }

  getAllDashboardData(): Observable<AllDashboardData> {
    const headers = this.getAuthHeaders();
    
    return forkJoin({
      stats: this.http.get<DashboardStats>(`${this.apiUrl}/stats`, { headers }),
      stockMovements: this.http.get<StockMovement[]>(`${this.apiUrl}/stock-movements`, { headers }),
      topProducts: this.http.get<TopProduct[]>(`${this.apiUrl}/top-products`, { headers }),
      topSuppliers: this.http.get<TopSupplier[]>(`${this.apiUrl}/top-suppliers`, { headers }), // <-- Appel avec la nouvelle interface
      recentAlerts: this.http.get<Alert[]>(`${this.apiUrl}/alerts/recent`, { headers }),
    });
  }
} 