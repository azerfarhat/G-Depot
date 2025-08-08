
  import { Injectable, inject } from '@angular/core';
  import { HttpClient, HttpHeaders, HttpErrorResponse } from '@angular/common/http';
  import { Observable, throwError } from 'rxjs';
  import { catchError } from 'rxjs/operators';
  import { AuthService } from './auth.service';

  import { Facture } from '../models/facture/facture.model'; // Modèle détaillé de Facture
  import { FactureCreateChauffeur } from '../models/facture/facture-create-chauffeur.model';
  import { FactureCreateRequest } from '../models/facture/facture-create-request.model';
  import { FactureStatutUpdate } from '../models/facture/facture-statut-update.model';
  import { StatutFacture } from '../models/facture/statut-facture.enum';


  @Injectable({
    providedIn: 'root'
  })
  export class FactureService {
    private http = inject(HttpClient);
    private authService = inject(AuthService);
    private baseUrl = 'http://localhost:9090/api/factures';

    private getAuthHeaders(): HttpHeaders {
      const token = this.authService.getToken();
      if (!token) {
        this.authService.logout();
        throw new Error('Aucun token d\'authentification trouvé. Veuillez vous connecter.');
      }
      return new HttpHeaders({ 'Authorization': `Bearer ${token}` });
    }

    // --- Facture API ---

    // POST /api/factures/chauffeur
    creerFactureChauffeur(payload: FactureCreateChauffeur): Observable<Facture> {
      const headers = this.getAuthHeaders();
      return this.http.post<Facture>(`${this.baseUrl}/chauffeur`, payload, { headers }).pipe(
        catchError(this.handleError)
      );
    }

    // POST /api/factures/commande/{commandeId}
    creerFacturePourCommande(commandeId: number, payload: FactureCreateRequest): Observable<Facture> {
      const headers = this.getAuthHeaders();
      return this.http.post<Facture>(`${this.baseUrl}/commande/${commandeId}`, payload, { headers }).pipe(
        catchError(this.handleError)
      );
    }

    // PATCH /api/factures/{id}/statut
    updateFactureStatut(factureId: number, nouveauStatut: StatutFacture): Observable<Facture> {
      const headers = this.getAuthHeaders();
      const payload: FactureStatutUpdate = { nouveauStatut: nouveauStatut };
      return this.http.patch<Facture>(`${this.baseUrl}/${factureId}/statut`, payload, { headers }).pipe(
        catchError(this.handleError)
      );
    }

    // GET /api/factures
    getToutesFactures(): Observable<Facture[]> {
      const headers = this.getAuthHeaders();
      return this.http.get<Facture[]>(this.baseUrl, { headers }).pipe(
        catchError(this.handleError)
      );
    }

    // GET /api/factures/{id}
    getFactureById(id: number): Observable<Facture> {
      const headers = this.getAuthHeaders();
      return this.http.get<Facture>(`${this.baseUrl}/${id}`, { headers }).pipe(
        catchError(this.handleError)
      );
    }

    // GET /api/factures/commande/{commandeId}
    getFactureByCommandeId(commandeId: number): Observable<Facture> {
      const headers = this.getAuthHeaders();
      return this.http.get<Facture>(`${this.baseUrl}/commande/${commandeId}`, { headers }).pipe(
        catchError(this.handleError)
      );
    }

    // GET /api/factures/bon-de-sortie/{bonDeSortieId}
    getFactureByBonDeSortieId(bonDeSortieId: number): Observable<Facture> {
      const headers = this.getAuthHeaders();
      return this.http.get<Facture>(`${this.baseUrl}/bon-de-sortie/${bonDeSortieId}`, { headers }).pipe(
        catchError(this.handleError)
      );
    }

    // GET /api/factures/client/{clientId}
    getFacturesParClient(clientId: number): Observable<Facture[]> {
      const headers = this.getAuthHeaders();
      return this.http.get<Facture[]>(`${this.baseUrl}/client/${clientId}`, { headers }).pipe(
        catchError(this.handleError)
      );
    }

    // GET /api/factures/chauffeur/{chauffeurId}
    getFacturesParChauffeur(chauffeurId: number): Observable<Facture[]> {
      const headers = this.getAuthHeaders();
      return this.http.get<Facture[]>(`${this.baseUrl}/chauffeur/${chauffeurId}`, { headers }).pipe(
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
    // GET /api/factures/{id}/pdf
    telechargerFacturePdf(id: number): Observable<Blob> {
      const headers = this.getAuthHeaders();
      return this.http.get(`${this.baseUrl}/${id}/pdf`, { headers, responseType: 'blob' });
    }
  }