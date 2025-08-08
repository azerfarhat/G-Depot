export interface LigneCommandeResponse {
  id: number;
  quantite: number;
  prixVenteTotalLigneTTC: number;
  produitId: number;
  nomProduit: string;
}