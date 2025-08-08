export interface ProduitList {
  id: number;
  nom: string;
  categorie: string;
  stockTotal: number;
  stockMinimum: number;
  nombreLots: number;
  dernierPrixVenteHTVA?: number;
  dernierPrixVenteTTC?: number;
  strategieStock: string;
  description: string;
}