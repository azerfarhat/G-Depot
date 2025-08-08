import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Commande } from '../models/commande/commande.model';
import { LigneCommandeRequest } from '../models/commande/ligne-commande-request.model';
import { StatutCommande } from '../models/commande/statut-commande.enum';

@Injectable({
  providedIn: 'root'
})
export class CommandeService {
  private apiUrl = '/api/commandes';

  constructor(private http: HttpClient) { }

  creerCommandePourClient(clientId: number): Observable<Commande> {
    return this.http.post<Commande>(`${this.apiUrl}/creer/${clientId}`, {});
  }

  getToutesCommandes(): Observable<Commande[]> {
    return this.http.get<Commande[]>(this.apiUrl);
  }

  getCommandeById(id: number): Observable<Commande> {
    return this.http.get<Commande>(`${this.apiUrl}/${id}`);
  }

  getCommandesParClient(clientId: number): Observable<Commande[]> {
    return this.http.get<Commande[]>(`${this.apiUrl}/client/${clientId}`);
  }

  getCommandesParStatut(statut: StatutCommande): Observable<Commande[]> {
    return this.http.get<Commande[]>(`${this.apiUrl}/statut/${statut}`);
  }

  validerCommande(idCommande: number): Observable<Commande> {
    return this.http.put<Commande>(`${this.apiUrl}/${idCommande}/valider`, {});
  }

  annulerCommande(idCommande: number): Observable<string> {
    // Le backend renvoie une String, donc on s'attend à du texte.
    return this.http.put(`${this.apiUrl}/${idCommande}/annuler`, {}, { responseType: 'text' });
  }

  ajouterLigneACommande(commandeId: number, ligneRequest: LigneCommandeRequest): Observable<Commande> {
    return this.http.post<Commande>(`${this.apiUrl}/${commandeId}/lignes`, ligneRequest);
  }

  mettreAJourQuantiteLigne(commandeId: number, ligneId: number, nouvelleQuantite: number): Observable<Commande> {
    const params = new HttpParams().set('nouvelleQuantite', nouvelleQuantite.toString());
    return this.http.put<Commande>(`${this.apiUrl}/${commandeId}/lignes/${ligneId}`, {}, { params: params });
  }

  supprimerLigneDeCommande(commandeId: number, ligneId: number): Observable<Commande> {
    return this.http.delete<Commande>(`${this.apiUrl}/${commandeId}/lignes/${ligneId}`);
  }
}