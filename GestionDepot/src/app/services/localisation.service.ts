import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Localisation {
  id?: number;
  lat: number;
  lng: number;
  type: string;
  client?: { id: number; nom?: string };
  depot?: { id: number; nom?: string };
}

@Injectable({ providedIn: 'root' })
export class LocalisationService {
  private apiUrl = 'http://localhost:9090/localisations';

  constructor(private http: HttpClient) {}

  getAll(): Observable<Localisation[]> {
    return this.http.get<Localisation[]>(this.apiUrl);
  }

  add(localisation: Localisation): Observable<Localisation> {
    return this.http.post<Localisation>(this.apiUrl, localisation);
  }

  delete(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }
}
