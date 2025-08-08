import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs'; 
import { catchError } from 'rxjs/operators'; 

import { ProduitList } from '../models/produit-list.model';

export interface Produit {
  id?: number;
  nom: string;
  description: string;
  stockMinimum: number;
  categorie?: string;
  strategieStock: string;
}

export interface ProduitUpdateDTO {
  nom: string;
  description: string;
  strategieStock: string;
  prixVenteParDefaut?: number;
}

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private readonly apiUrl = 'http://localhost:9090/product'; // Assurez-vous que le port est correct (9090)

  constructor(private http: HttpClient) { }

  getProductsForList(recherche?: string): Observable<ProduitList[]> {
    let params = new HttpParams();
    if (recherche) {
      params = params.append('recherche', recherche);
    }
    return this.http.get<ProduitList[]>(`${this.apiUrl}/liste`, { params })
      .pipe(
        catchError(this.handleError) 
      );
  }

  ajouterProduit(produit: Produit): Observable<Produit> {
    return this.http.post<Produit>(`${this.apiUrl}/ajouter_produit`, produit)
      .pipe(
        catchError(this.handleError) 
      );
  }

  deleteProduct(id: number): Observable<string> {
    // Note: responseType 'text' car le backend renvoie un String ou Map.of("message",...)
    return this.http.delete(`${this.apiUrl}/delete_product/${id}`, { responseType: 'text' }) 
      .pipe(
        catchError(this.handleError) 
      );
  }

  updateProduct(id: number, data: ProduitUpdateDTO): Observable<Produit> {
    return this.http.put<Produit>(`${this.apiUrl}/update_product/${id}`, data)
      .pipe(
        catchError(this.handleError) 
      );
  }

  /**
   * Méthode privée pour gérer les erreurs HTTP des requêtes.
   * C'est crucial pour que les messages d'erreur soient bien capturés par le composant.
   */
  private handleError(error: HttpErrorResponse) {
    let errorMessage = 'Une erreur inconnue est survenue.';
    if (error.error instanceof ErrorEvent) {
      // Erreur côté client ou réseau
      errorMessage = `Erreur côté client: ${error.error.message}`;
    } else {
      // Erreur côté serveur (ex: 4xx ou 5xx)
      console.error(
        `Code d'erreur du backend: ${error.status}, ` +
        `Corps de l'erreur: ${JSON.stringify(error.error)}`);
      // Extrait le message d'erreur du corps de la réponse si disponible
      if (error.error && typeof error.error === 'object' && error.error.message) {
        errorMessage = error.error.message;
      } else if (typeof error.error === 'string') {
        errorMessage = error.error; // Si le backend renvoie juste un message texte
      } else if (error.status) {
        errorMessage = `Erreur (${error.status}): ${error.statusText || 'Serveur introuvable ou erreur.'}`;
      }
    }
    // Retourne un Observable qui lance une erreur avec le message formaté
    return throwError(() => new Error(errorMessage));
  }
}