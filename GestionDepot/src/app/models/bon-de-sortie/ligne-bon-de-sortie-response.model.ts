import { Produit } from '../produit.model'; // Assurez-vous d'avoir ce modèle si Produit est plus complexe

export interface LigneBonDeSortieResponse {
  id: number;
  produitId: number;
  nomProduit: string;
  quantiteSortie: number;
  quantiteRetournee: number;
  quantiteFacturee: number;
  quantiteDisponiblePourFacturation: number;
  prixUnitaireTTC: number;
}