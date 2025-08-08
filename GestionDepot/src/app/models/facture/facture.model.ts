import { Produit } from '../produit.model';
import { StatutFacture } from './statut-facture.enum';
import { UtilisateurSimple } from '..//utilisateur.model';

export interface LigneFacture {
  id: number;
  quantite: number;
  prixUnitaireTTC: number;
  totalLigneTTC: number;
  produit: Produit;
}

export interface FactureSimple {
  id: number;
  numeroFacture: string;
  dateFacturation: string;
  totalTTC: number;
  statut: StatutFacture;
}

export interface Facture {
  id: number;
  numeroFacture: string;
  dateFacturation: string;
  dateEcheance: string;
  statut: StatutFacture;
  totalHT: number;
  montantTVA: number;
  totalTTC: number;
  commandeId?: number;
  client?: UtilisateurSimple;
  bonDeSortieId?: number;
  chauffeur?: UtilisateurSimple;
  // Lignes de facture si vous voulez les afficher dans le détail de la facture
  // lignes?: LigneFactureResponse[];
}