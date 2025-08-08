import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs'; // 'of' pour les données de test
import { ChauffeurDashboardData } from '../models/chauffeur-dashboard.model';


@Injectable({
  providedIn: 'root'
})
export class ChauffeurService {
  private baseUrl = '/api/chauffeurs';

  constructor(private http: HttpClient) { }

  getDashboardData(chauffeurId: number): Observable<ChauffeurDashboardData> {
    // Vrai appel HTTP à commenter quand le backend sera prêt
    return this.http.get<ChauffeurDashboardData>(`${this.baseUrl}/${chauffeurId}/dashboard`);
    
   
  }
}