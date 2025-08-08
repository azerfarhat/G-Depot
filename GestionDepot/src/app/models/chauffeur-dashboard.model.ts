import { Chauffeur } from "./bon-de-sortie/bon-de-sortie.model";

export interface HistoriqueBds {
    numeroBDS: string;
    dateSortie: string;
    valeurInitiale: number;
    valeurVendue: number;
    valeurRetournee: number;
    marge: number;
    statut: string;
}

export interface ChauffeurStats { // Renommé
    totalPrix: number; // Corrigé
    totalVendu: number;
    totalRetourne: number;
    margeBeneficiaire: number;
    tauxDeVente: number;
}

export interface ChauffeurDashboardData {
    chauffeur: Chauffeur;
    stats: ChauffeurStats; // Renommé
    historiqueBons: HistoriqueBds[];
}