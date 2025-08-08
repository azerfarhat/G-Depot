// Interface for the summary data returned by /fournisseurs/summary
export interface FournisseurSummary {
  id: number;
  nom: string;
  societe: string; // Used for "Contact" in the UI
  email: string;
  telephone: string;
  totalStocksQuantity: number; // Matches the int field in FournisseurSummaryDto
  totalCommandeValue: number;  // Matches the double field in FournisseurSummaryDto
}

// Interface for the full Fournisseur object (for add/edit forms)
export interface Fournisseur {
  id?: number; // Optional for new entities (when adding)
  nom: string;
  societe: string;
  email: string;
  telephone: string;
  adresse: string;
  pays: string;
  siteWeb: string;
}