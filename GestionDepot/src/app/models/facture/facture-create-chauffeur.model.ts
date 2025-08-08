export interface LigneFactureCreateChauffeur {
  produitId: number;
  quantite: number;
}
export interface FactureCreateChauffeur {
  bonDeSortieId: number;
  lignes: LigneFactureCreateChauffeur[];
}