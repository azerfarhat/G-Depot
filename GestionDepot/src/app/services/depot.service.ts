// src/app/services/depot.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { DepotDto, DepotCreateRequest } from '../models/depot.model';
import { UtilisateurSimpleDto } from '../models/utilisateur.model';

@Injectable({ providedIn: 'root' })
export class DepotService {

  private apiUrl = 'http://localhost:9090/depot';

  constructor(private http: HttpClient) { }

  private handleError(error: HttpErrorResponse) {
    let errorMessage = 'Une erreur inconnue est survenue !';
    if (error.error instanceof ErrorEvent) {
      errorMessage = `Erreur côté client: ${error.error.message}`;
    } else {
      errorMessage = `Erreur du serveur: ${error.status} - ${error.error || error.statusText}`;
      if (error.error && typeof error.error === 'string') {
        errorMessage = `Erreur: ${error.error}`;
      } else if (error.error && typeof error.error === 'object' && error.error.message) {
        errorMessage = `Erreur: ${error.error.message}`;
      }
    }
    console.error(errorMessage);
    return throwError(() => new Error(errorMessage));
  }

  getAllDepots(): Observable<DepotDto[]> {
    return this.http.get<DepotDto[]>(this.apiUrl).pipe(
      catchError(this.handleError)
    );
  }

  ajouterDepot(depotData: DepotCreateRequest, responsableId: number): Observable<DepotDto> {
    return this.http.post<DepotDto>(`${this.apiUrl}/ajouter_Depot/responsable/${responsableId}`, depotData).pipe(
      catchError(this.handleError)
    );
  }

  getDepotsParResponsable(idResponsable: number): Observable<DepotDto[]> {
    return this.http.get<DepotDto[]>(`${this.apiUrl}/responsable/${idResponsable}`).pipe(
      catchError(this.handleError)
    );
  }

  supprimerDepot(id: number): Observable<string> {
    return this.http.delete(`${this.apiUrl}/delete_Depot/${id}`, { responseType: 'text' }).pipe(
      catchError(this.handleError)
    );
  }

  getDepotById(id: number): Observable<DepotDto> {
    return this.http.get<DepotDto>(`${this.apiUrl}/get_depot/${id}`).pipe(
      catchError(this.handleError)
    );
  }

  modifierTvaDepot(id: number, nouvelleTva: number): Observable<string> {
    return this.http.post(`${this.apiUrl}/tva/${id}`, nouvelleTva, { responseType: 'text' }).pipe(
      catchError(this.handleError)
    );
  }

  updateDepot(id: number, depotData: DepotCreateRequest): Observable<DepotDto> {
    return this.http.put<DepotDto>(`${this.apiUrl}/${id}`, depotData).pipe(
      catchError(this.handleError)
    );
  }

  assignerResponsable(depotId: number, utilisateurId: number): Observable<string> {
    return this.http.post(`${this.apiUrl}/${depotId}/assigner-responsable/${utilisateurId}`, null, { responseType: 'text' }).pipe(
      catchError(this.handleError)
    );
  }

  getAllDepotsForDisplay(): Observable<DepotDto[]> {
    return this.http.get<DepotDto[]>(`${this.apiUrl}/details`).pipe(
      catchError(this.handleError)
    );
  }
}