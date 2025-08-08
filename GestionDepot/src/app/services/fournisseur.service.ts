import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { FournisseurSummary, Fournisseur } from '../models/fournisseur.model';
// import { environment } from '../../environments/environment'; // <-- REMOVE THIS IMPORT

@Injectable({
  providedIn: 'root'
})
export class FournisseurService {
  // Directly define the API URL here
  private apiUrl = 'http://localhost:9090/fournisseurs'; // <-- UPDATED LINE

  constructor(private http: HttpClient) { }

  // Helper to get headers with Authorization token (if you are using JWT)
  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('jwt_token'); // Or from a shared auth service
    // If no token is found, or if backend does not require auth for certain endpoints,
    // this will return headers without the Authorization header.
    // Ensure you have a mechanism to save the JWT token after login if required by your backend.
    if (token) {
      return new HttpHeaders().set('Authorization', `Bearer ${token}`);
    }
    return new HttpHeaders(); // Return empty headers if no token
  }
  getAllFournisseurs(): Observable<Fournisseur[]> {
    return this.http.get<Fournisseur[]>(this.apiUrl, { headers: this.getAuthHeaders() });
  }

  // Get summary data for the list table
  getFournisseursSummary(): Observable<FournisseurSummary[]> {
    return this.http.get<FournisseurSummary[]>(`${this.apiUrl}/summary`, { headers: this.getAuthHeaders() });
  }

  // Get a single fournisseur for editing (full object)
  getFournisseurById(id: number): Observable<Fournisseur> {
    return this.http.get<Fournisseur>(`${this.apiUrl}/get_fournisseur/${id}`, { headers: this.getAuthHeaders() });
  }

  // Add a new fournisseur
  addFournisseur(fournisseur: Fournisseur): Observable<string> {
    return this.http.post(`${this.apiUrl}/ajouter_fournisseurs`, fournisseur, { responseType: 'text', headers: this.getAuthHeaders() });
  }

  // Update an existing fournisseur
  updateFournisseur(id: number, fournisseur: Fournisseur): Observable<Fournisseur> {
    return this.http.put<Fournisseur>(`${this.apiUrl}/update_founisseur/${id}`, fournisseur, { headers: this.getAuthHeaders() });
  }

  // Delete a fournisseur
  deleteFournisseur(id: number): Observable<string> {
    return this.http.delete(`${this.apiUrl}/delete_fournisseur/${id}`, { responseType: 'text', headers: this.getAuthHeaders() });
  }
}