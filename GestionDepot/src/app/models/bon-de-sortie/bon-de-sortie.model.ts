import { Produit } from '../produit.model';
import { StatutBonDeSortie } from './statut-bon-de-sortie.enum';
import { UtilisateurSimple } from '..//utilisateur.model';
import { LigneBonDeSortieResponse } from './ligne-bon-de-sortie-response.model';
import { FactureSimple } from '../facture/facture.model';

export interface Chauffeur {
    id: number;
    nom: string;
    email: string;
}

export interface LigneBonDeSortie {
    id: number;
    quantiteSortie: number;
    quantiteRetournee: number;
    prixUnitaireTTC: number;
    produit: Produit;
}

export interface BonDeSortie {
  id: number;
  numeroBDS: string;
  dateSortie: string;
  statut: StatutBonDeSortie;
  valeurTotaleInitialeTTC: number;
  chauffeur?: UtilisateurSimple;
  commandeOrigineId?: number;
  lignes: LigneBonDeSortieResponse[];
  factures: FactureSimple[]; // Les factures associées à ce Bon de Sortie
}
export interface BonDeSortieListItem {
    id: number;
    numeroBDS: string;
    chauffeurNom: string;
    valeurTotaleInitialeTTC: number;
    statut: string;
}