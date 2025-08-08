// src/app/services/utilisateur.service.ts

import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

// On importe les modèles
import { Utilisateur, UtilisateurSimpleDto } from '../models/utilisateur.model';
import { ChauffeurListDto } from '../models/chauffeur.model';
import { UtilisateurRequestDto } from '../models/utilisateur-request.model';
import { ChauffeurDashboardData } from '../models/chauffeur-dashboard.model';

@Injectable({
  providedIn: 'root'
})
export class UtilisateurService {
  private apiUrl = 'http://localhost:9090/utilisateurs';

  constructor(private http: HttpClient) { }

  getDashboardData(chauffeurId: number): Observable<ChauffeurDashboardData> {
    return this.http.get<ChauffeurDashboardData>(`${this.apiUrl}/${chauffeurId}/dashboard`).pipe(catchError(this.handleError));
  }

  getChauffeurs(): Observable<ChauffeurListDto[]> {
    return this.http.get<ChauffeurListDto[]>(`${this.apiUrl}/chauffeurs`).pipe(catchError(this.handleError));
  }

  /**
   * CORRECTION : Cette méthode retourne maintenant l'objet Utilisateur complet.
   */
  getUtilisateurById(id: number): Observable<Utilisateur> {
    return this.http.get<Utilisateur>(`${this.apiUrl}/Get_User/${id}`).pipe(catchError(this.handleError));
  }

  // --- NOUVELLE MÉTHODE : Pour obtenir la liste de tous les utilisateurs ---
  getAllUsers(): Observable<UtilisateurSimpleDto[]> {
    return this.http.get<UtilisateurSimpleDto[]>(`${this.apiUrl}/List_Utilisateur`).pipe(catchError(this.handleError));
  }
  // ---------------------------------------------------------------------

  ajouterUtilisateur(utilisateur: UtilisateurRequestDto): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/ajouter_utilisateurs`, utilisateur).pipe(catchError(this.handleError));
  }

  /**
   * NOUVELLE MÉTHODE : Pour la mise à jour d'un utilisateur.
   */
  updateUtilisateur(id: number, utilisateur: UtilisateurRequestDto): Observable<any> {
    // Note : Le backend doit avoir un endpoint PUT sur cette URL
    return this.http.put<any>(`${this.apiUrl}/update/${id}`, utilisateur).pipe(catchError(this.handleError));
  }

  deleteChauffeur(id: number): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/${id}`).pipe(catchError(this.handleError));
  }

  getAllResponsables(): Observable<UtilisateurSimpleDto[]> {
      // Cette URL est une supposition. Adaptez-la à l'API qui retourne vos responsables.
      // Par exemple, si vous avez un endpoint spécifique pour les responsables :
      // return this.http.get<UtilisateurSimpleDto[]>(`${this.apiUrl}/responsables`).pipe(
      //   catchError(this.handleError)
      // );
      // Ou si vous filtrez par rôle via un paramètre de requête :
      return this.http.get<UtilisateurSimpleDto[]>(`${this.apiUrl}/List_responsables`).pipe( // Supposons un endpoint /utilisateur/parRole/RESPONSABLE
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
      if (error.error && error.error.message) {
        errorMessage = error.error.message;
      } else if (error.status) {
        errorMessage = `Erreur (${error.status}): ${error.statusText || 'Serveur introuvable ou erreur.'}`;
      }
    }
    return throwError(() => new Error(errorMessage));
  }
}