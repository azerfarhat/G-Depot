import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthService } from './auth.service';

import { BonDeSortie } from '../models/bon-de-sortie/bon-de-sortie.model';
import { BonDeSortieListItem } from '../models/bon-de-sortie/bon-de-sortie-list-item.model';
import { RetourProduitPayload } from '../models/bon-de-sortie/retour-produit-payload.model';
import { VerificationBDS } from '../models/bon-de-sortie/verification-bds.model';

@Injectable({
  providedIn: 'root'
})
export class BonDeSortieService {
  private http = inject(HttpClient);
  private authService = inject(AuthService);
  private baseUrl = 'http://localhost:9090/api/bons-de-sortie';

  private getAuthHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    if (!token) {
      this.authService.logout();
      throw new Error('Aucun token d\'authentification trouvé. Veuillez vous connecter.');
    }
    return new HttpHeaders({ 'Authorization': `Bearer ${token}` });
  }

  // --- Bon De Sortie API ---

  // GET /api/bons-de-sortie
  getBonsDeSortie(): Observable<BonDeSortieListItem[]> {
    const headers = this.getAuthHeaders();
    return this.http.get<BonDeSortieListItem[]>(this.baseUrl, { headers }).pipe(
      catchError(this.handleError)
    );
  }

  // GET /api/bons-de-sortie/{id}/details
  getBonDeSortieById(bdsId: number): Observable<BonDeSortie> {
    const headers = this.getAuthHeaders();
    return this.http.get<BonDeSortie>(`${this.baseUrl}/${bdsId}/details`, { headers }).pipe(
      catchError(this.handleError)
    );
  }

  // POST /api/bons-de-sortie/creer/{commandeId}/{chauffeurId}
  creerBonDeSortie(commandeId: number, chauffeurId: number): Observable<BonDeSortieListItem> {
    const headers = this.getAuthHeaders();
    // Le corps de la requête est vide car les IDs sont dans l'URL
    return this.http.post<BonDeSortieListItem>(`${this.baseUrl}/creer/${commandeId}/${chauffeurId}`, {}, { headers }).pipe(
      catchError(this.handleError)
    );
  }

  // PATCH /api/bons-de-sortie/{id}/retours
  enregistrerRetours(bdsId: number, retours: RetourProduitPayload[]): Observable<any> {
    const headers = this.getAuthHeaders();
    return this.http.patch(`${this.baseUrl}/${bdsId}/retours`, retours, { headers, responseType: 'text' }).pipe(
      catchError(this.handleError)
    );
  }

  // GET /api/bons-de-sortie/{id}/verification
  getVerificationDetails(bdsId: number): Observable<VerificationBDS> {
    const headers = this.getAuthHeaders();
    return this.http.get<VerificationBDS>(`${this.baseUrl}/${bdsId}/verification`, { headers }).pipe(
      catchError(this.handleError)
    );
  }
// GET /api/bons-de-sortie/{id}/produit
  getProduitsParBonDeSortie(bdsId: number): Observable<any[]> {
    const headers = this.getAuthHeaders();
    return this.http.get<any[]>(`${this.baseUrl}/${bdsId}/produits`, { headers }).pipe(
      catchError(this.handleError)
    );
  }

  // --- Gestion d'erreurs générique ---
  private handleError(error: HttpErrorResponse) {
    let errorMessage = 'Une erreur inconnue est survenue.';
    if (error.error instanceof ErrorEvent) {
      errorMessage = `Erreur côté client: ${error.error.message}`;
    } else {
      console.error(
        `Code d'erreur du backend: ${error.status}, ` +
        `Corps de l'erreur: ${JSON.stringify(error.error)}`);
      if (error.error && typeof error.error === 'string') {
        errorMessage = error.error;
      } else if (error.error && typeof error.error === 'object' && error.error.message) {
        errorMessage = error.error.message;
      } else if (error.status) {
        errorMessage = `Erreur (${error.status}): ${error.statusText || 'Serveur introuvable ou erreur.'}`;
      }
    }
    return throwError(() => new Error(errorMessage));
  }
}