import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

// =========================================================
// === NOUVELLES INTERFACES POUR LES DONNÉES DU STOCK ===
// =========================================================

// Correspond au DTO StockRequestDto.java de votre backend
export interface NewStockData {
  produitId: number;
  quantite: number;
  prixVenteHTVA: number;
  prixAchat: number;
  dateExpiration: string; // LocalDate en Java devient string (YYYY-MM-DD) en TS
  codeBarre: string;
  seuilMin: number;
  depotId: number;
  fournisseurid: number; // Assurez-vous que le nom correspond à votre DTO Java (fournisseurid)
}

// Correspond à votre entité Stock (simplifié pour l'affichage)
export interface StockLot {
  id: number;
  codeBarre: string;
  quantiteProduit: number;
  prixVenteTTC: number; // Vous devrez peut-être calculer le TTC côté frontend ou l'ajouter au DTO backend
  createdAt: string; // LocalDateTime en Java
  dateExpiration: string; // LocalDate en Java
  // Ajoutez d'autres champs si vous les affichez dans les détails du stock
}

@Injectable({
  providedIn: 'root'
})
export class StockService {
  private apiUrl = 'http://localhost:9090/stock'; // CIBLE LE CONTRÔLEUR DE STOCK BACKEND

  constructor(private http: HttpClient) { }

  /**
   * Ajoute un nouveau lot de stock.
   * Correspond à POST /stock/ajouter_stock dans votre backend.
   */
  addStock(newStockData: NewStockData): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/ajouter_stock`, newStockData)
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Récupère les détails de stock pour un produit donné.
   * Correspond à GET /stock/produit/{produitId} dans votre backend.
   * REMARQUE : Votre contrôleur renvoie `List<Stock>`, vous devrez adapter l'interface StockLot
   * ou créer un DTO spécifique pour la liste des lots de stock dans le backend si `Stock` est trop gros.
   */
  getStockByProductId(productId: number): Observable<StockLot[]> {
    return this.http.get<StockLot[]>(`${this.apiUrl}/produit/${productId}`)
      .pipe(
        catchError(this.handleError)
      );
  }

  private handleError(error: HttpErrorResponse) {
    let errorMessage = 'Une erreur inconnue est survenue.';
    if (error.error instanceof ErrorEvent) {
      errorMessage = `Erreur côté client: ${error.error.message}`;
    } else {
      console.error(
        `Code d'erreur du backend: ${error.status}, ` +
        `Corps de l'erreur: ${JSON.stringify(error.error)}`);
      if (error.error && typeof error.error === 'object' && error.error.message) {
        errorMessage = error.error.message;
      } else if (typeof error.error === 'string') {
        errorMessage = error.error;
      } else if (error.status) {
        errorMessage = `Erreur (${error.status}): ${error.statusText || 'Serveur introuvable ou erreur.'}`;
      }
    }
    return throwError(() => new Error(errorMessage));
  }
}