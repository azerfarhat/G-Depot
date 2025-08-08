import { StatutBonDeSortie } from './statut-bon-de-sortie.enum';

export interface BonDeSortieListItem {
  id: number;
  numeroBDS: string;
  dateSortie: string;
  statut: StatutBonDeSortie;
  valeurTotaleInitialeTTC: number;
  chauffeurNom?: string;
  chauffeurId?: number; // Pour pouvoir filtrer/récupérer des factures par chauffeur depuis la liste BDS
}