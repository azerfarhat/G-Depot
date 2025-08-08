import { UtilisateurSimple } from '../../models/utilisateur.model';
import { LigneCommandeResponse } from './ligne-commande-response.model';
import { StatutCommande } from './statut-commande.enum';

export interface Commande {
  id: number;
  dateCommande: string; // LocalDateTime from Spring is usually mapped to string in Angular (ISO 8601)
  statutCommande: StatutCommande; // Utiliser l'enum que nous avons créé
  totaleCommandeTTC: number;
  client: UtilisateurSimple;
  lignes: LigneCommandeResponse[];
}